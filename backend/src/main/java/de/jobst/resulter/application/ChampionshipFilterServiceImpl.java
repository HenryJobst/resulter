package de.jobst.resulter.application;

import de.jobst.resulter.application.port.ChampionshipFilterService;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.application.port.RaceRepository;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.springapp.config.SpringSecurityAuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChampionshipFilterServiceImpl implements ChampionshipFilterService {

    private final ResultListRepository resultListRepository;
    private final OrganisationRepository organisationRepository;
    private final RaceRepository raceRepository;
    private final SpringSecurityAuditorAware auditorAware;

    public ChampionshipFilterServiceImpl(
            ResultListRepository resultListRepository,
            OrganisationRepository organisationRepository,
            RaceRepository raceRepository,
            SpringSecurityAuditorAware auditorAware) {
        this.resultListRepository = resultListRepository;
        this.organisationRepository = organisationRepository;
        this.raceRepository = raceRepository;
        this.auditorAware = auditorAware;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> findClassShortNames(EventId eventId) {
        return new java.util.TreeSet<>(resultListRepository.findClassShortNamesByEventId(eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMultipleRaces(EventId eventId) {
        return resultListRepository.countNonZeroRacesByEventId(eventId) > 1;
    }

    @Override
    @Transactional
    public void applyChampionshipCleanup(EventId eventId, OrganisationId baseOrgId, Set<String> excludeClassShortNames) {
        Collection<ResultList> resultLists = resultListRepository.findByEventId(eventId);

        Map<OrganisationId, Organisation> orgTree = loadOrgTree(resultLists, baseOrgId);
        Organisation baseOrg = findBaseOrg(orgTree, baseOrgId);

        for (ResultList resultList : resultLists) {
            applyCleanupToResultList(resultList, baseOrg, orgTree, excludeClassShortNames);
            resultListRepository.update(resultList);
        }
    }

    @Override
    @Transactional
    public List<ResultList> addChampionshipRanking(EventId eventId, OrganisationId baseOrgId, Set<String> excludeClassShortNames) {
        Collection<ResultList> allResultLists = resultListRepository.findByEventId(eventId);

        Map<OrganisationId, Organisation> orgTree = loadOrgTree(allResultLists, baseOrgId);
        Organisation baseOrg = findBaseOrg(orgTree, baseOrgId);

        // Filter to source result lists (exclude any with raceNumber == 0)
        List<ResultList> sourceResultLists = allResultLists.stream()
                .filter(rl -> !isRace0(rl))
                .collect(Collectors.toList());

        if (sourceResultLists.isEmpty()) {
            return Collections.emptyList();
        }

        // Merge ClassResults by classResultShortName across all source lists
        Map<ClassResultShortName, List<PersonResult>> mergedByClass = new LinkedHashMap<>();
        Map<ClassResultShortName, ClassResult> classMetaByShortName = new LinkedHashMap<>();

        for (ResultList rl : sourceResultLists) {
            if (rl.getClassResults() == null) continue;
            for (ClassResult cr : rl.getClassResults()) {
                ClassResultShortName shortName = cr.classResultShortName();
                if (excludeClassShortNames.contains(shortName.value())) continue;
                mergedByClass.computeIfAbsent(shortName, k -> new ArrayList<>())
                        .addAll(cr.personResults().value());
                classMetaByShortName.putIfAbsent(shortName, cr);
            }
        }

        // Build new ClassResults with championship ranking
        List<ClassResult> championshipClassResults = new ArrayList<>();
        for (Map.Entry<ClassResultShortName, List<PersonResult>> entry : mergedByClass.entrySet()) {
            ClassResultShortName shortName = entry.getKey();
            List<PersonResult> allPersonResults = entry.getValue();
            ClassResult meta = classMetaByShortName.get(shortName);

            // Split into eligible and non-eligible
            List<PersonResult> eligible = new ArrayList<>();
            List<PersonResult> nonEligibleWithTime = new ArrayList<>();
            List<PersonResult> nonEligibleWithoutTime = new ArrayList<>();
            for (PersonResult pr : allPersonResults) {
                if (isEligible(pr, baseOrg, orgTree)) {
                    eligible.add(pr);
                } else if (bestRuntime(pr) < Double.MAX_VALUE) {
                    nonEligibleWithTime.add(pr);
                } else {
                    nonEligibleWithoutTime.add(pr);
                }
            }

            // Sort eligible by best OK runtime ascending, with personId as tiebreaker
            eligible = eligible.stream()
                    .sorted(Comparator.comparingDouble(this::bestRuntime)
                            .thenComparingLong(pr -> pr.personId().value()))
                    .collect(Collectors.toList());
            // Sort non-eligible-with-time by runtime ascending
            nonEligibleWithTime = nonEligibleWithTime.stream()
                    .sorted(Comparator.comparingDouble(this::bestRuntime)
                            .thenComparingLong(pr -> pr.personId().value()))
                    .collect(Collectors.toList());
            // Sort non-eligible-without-time by personId
            nonEligibleWithoutTime = nonEligibleWithoutTime.stream()
                    .sorted(Comparator.comparingLong(pr -> pr.personId().value()))
                    .collect(Collectors.toList());

            List<PersonResult> rankedPersonResults = new ArrayList<>();
            int position = 1;

            // Eligible: Olympic ranking on OK results
            double lastOkRuntime = Double.NaN;
            int lastOkPos = 1;
            for (int i = 0; i < eligible.size(); i++) {
                PersonResult pr = eligible.get(i);
                double best = bestRuntime(pr);
                boolean hasOkResult = best < Double.MAX_VALUE;
                int assignedPos;
                if (hasOkResult) {
                    if (i == 0 || best != lastOkRuntime) {
                        lastOkPos = position;
                        lastOkRuntime = best;
                    }
                    assignedPos = lastOkPos;
                } else {
                    assignedPos = position;
                }
                PersonRaceResult newRaceResult = new PersonRaceResult(
                        shortName,
                        pr.personId(),
                        DateTime.empty(),
                        DateTime.empty(),
                        hasOkResult ? PunchTime.of(best) : PunchTime.of(null),
                        Position.of((long) assignedPos),
                        hasOkResult ? ResultStatus.OK : bestSourceStatus(pr),
                        RaceNumber.of((byte) 0),
                        null);
                rankedPersonResults.add(PersonResult.of(
                        shortName, pr.personId(), pr.organisationId(), List.of(newRaceResult)));
                position++;
            }

            // Non-eligible with time: NOT_COMPETING, sequential positions for DB ordering
            for (PersonResult pr : nonEligibleWithTime) {
                double best = bestRuntime(pr);
                PersonRaceResult newRaceResult = new PersonRaceResult(
                        shortName,
                        pr.personId(),
                        DateTime.empty(),
                        DateTime.empty(),
                        PunchTime.of(best),
                        Position.of((long) position),
                        ResultStatus.NOT_COMPETING,
                        RaceNumber.of((byte) 0),
                        null);
                rankedPersonResults.add(PersonResult.of(
                        shortName, pr.personId(), pr.organisationId(), List.of(newRaceResult)));
                position++;
            }

            // Non-eligible without valid time: original non-OK status, sequential positions
            for (PersonResult pr : nonEligibleWithoutTime) {
                PersonRaceResult newRaceResult = new PersonRaceResult(
                        shortName,
                        pr.personId(),
                        DateTime.empty(),
                        DateTime.empty(),
                        PunchTime.of(null),
                        Position.of((long) position),
                        bestSourceStatus(pr),
                        RaceNumber.of((byte) 0),
                        null);
                rankedPersonResults.add(PersonResult.of(
                        shortName, pr.personId(), pr.organisationId(), List.of(newRaceResult)));
                position++;
            }

            ClassResult championshipClassResult = new ClassResult(
                    meta.classResultName(),
                    shortName,
                    meta.gender(),
                    PersonResults.of(rankedPersonResults),
                    meta.courseId());
            championshipClassResults.add(championshipClassResult);
        }

        // Find or create Race with raceNumber=0
        Race race0 = raceRepository.findOrCreate(Race.of(eventId, (byte) 0));

        // Determine creator
        String creator = auditorAware.getCurrentAuditor()
                .orElse(SpringSecurityAuditorAware.UNKNOWN);

        // Reuse existing Race-0 result list ID if present, so a re-run replaces instead of duplicates
        Optional<ResultList> existingRace0 = allResultLists.stream()
                .filter(this::isRace0)
                .findFirst();

        ResultListId resultListId = existingRace0
                .map(ResultList::getId)
                .orElse(ResultListId.empty());

        ResultList newResultList = new ResultList(
                resultListId,
                eventId,
                race0.getId(),
                creator,
                ZonedDateTime.now(),
                existingRace0.map(ResultList::getStatus).orElse(null),
                championshipClassResults);

        ResultList saved = resultListRepository.save(newResultList);
        return List.of(saved);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Map<OrganisationId, Organisation> loadOrgTree(
            Collection<ResultList> resultLists, OrganisationId baseOrgId) {
        Set<OrganisationId> orgIds = new HashSet<>();
        orgIds.add(baseOrgId);
        for (ResultList rl : resultLists) {
            if (rl.getClassResults() != null) {
                orgIds.addAll(rl.getReferencedOrganisationIds());
            }
        }
        return organisationRepository.loadOrganisationTree(orgIds);
    }

    private Organisation findBaseOrg(Map<OrganisationId, Organisation> orgTree, OrganisationId baseOrgId) {
        Organisation baseOrg = orgTree.get(baseOrgId);
        if (baseOrg == null) {
            throw new IllegalArgumentException(
                    "Base organisation with id " + baseOrgId + " not found in the organisation tree.");
        }
        return baseOrg;
    }

    private void applyCleanupToResultList(
            ResultList resultList, Organisation baseOrg, Map<OrganisationId, Organisation> orgTree,
            Set<String> excludeClassShortNames) {
        if (resultList.getClassResults() == null) return;

        List<ClassResult> updatedClassResults = new ArrayList<>();
        for (ClassResult cr : resultList.getClassResults()) {
            if (excludeClassShortNames.contains(cr.classResultShortName().value())) {
                continue;
            }

            // Step 1: mark non-eligible PersonRaceResults as NOT_COMPETING (only when source is OK)
            List<PersonResult> markedPersonResults = new ArrayList<>();
            for (PersonResult pr : cr.personResults().value()) {
                if (isEligible(pr, baseOrg, orgTree)) {
                    markedPersonResults.add(pr);
                } else {
                    List<PersonRaceResult> updated = pr.personRaceResults().value().stream()
                            .map(prr -> new PersonRaceResult(
                                    prr.getClassResultShortName(),
                                    prr.getPersonId(),
                                    prr.getStartTime(),
                                    prr.getFinishTime(),
                                    prr.getRuntime(),
                                    prr.getPosition(),
                                    ResultStatus.OK.equals(prr.getState())
                                            ? ResultStatus.NOT_COMPETING
                                            : prr.getState(),
                                    prr.getRaceNumber(),
                                    prr.getSplitTimeListId()))
                            .collect(Collectors.toList());
                    markedPersonResults.add(PersonResult.of(
                            pr.classResultShortName(), pr.personId(), pr.organisationId(), updated));
                }
            }

            // Step 2: reorder positions per raceNumber:
            //   1. OK (eligible) sorted by runtime asc
            //   2. NOT_COMPETING with valid runtime sorted by runtime asc
            //   3. everything else (MissingPunch, DNS, DNF, …) sorted by personId
            List<PersonResult> reorderedPersonResults =
                    reorderPositions(markedPersonResults, cr.classResultShortName());

            updatedClassResults.add(new ClassResult(
                    cr.classResultName(),
                    cr.classResultShortName(),
                    cr.gender(),
                    PersonResults.of(reorderedPersonResults),
                    cr.courseId()));
        }
        resultList.setClassResults(updatedClassResults);
    }

    /**
     * Reassigns positions within a class for every race number.
     * Order: OK by runtime → NOT_COMPETING by runtime → everything else by personId.
     */
    private List<PersonResult> reorderPositions(
            List<PersonResult> personResults, ClassResultShortName shortName) {

        // Collect all raceNumbers present
        Set<RaceNumber> raceNumbers = personResults.stream()
                .flatMap(pr -> pr.personRaceResults().value().stream())
                .map(PersonRaceResult::getRaceNumber)
                .collect(Collectors.toSet());

        // Build a lookup: personId → mutable map of raceNumber → PersonRaceResult
        Map<PersonId, Map<RaceNumber, PersonRaceResult>> prrByPerson = new LinkedHashMap<>();
        for (PersonResult pr : personResults) {
            Map<RaceNumber, PersonRaceResult> byRace = new LinkedHashMap<>();
            for (PersonRaceResult prr : pr.personRaceResults().value()) {
                byRace.put(prr.getRaceNumber(), prr);
            }
            prrByPerson.put(pr.personId(), byRace);
        }

        // Use the first (or only) raceNumber to determine the overall display order.
        // For each raceNumber: assign positions to OK results, null for everyone else.
        RaceNumber primaryRace = raceNumbers.stream()
                .min(Comparator.comparingInt(rn -> rn.value() & 0xFF))
                .orElseThrow();

        Map<PersonId, Map<RaceNumber, PersonRaceResult>> updatedPrr = new LinkedHashMap<>(prrByPerson);
        List<PersonResult> sortedOrder = null;

        for (RaceNumber raceNumber : raceNumbers) {
            List<PersonResult> forRace = personResults.stream()
                    .filter(pr -> pr.personRaceResults().value().stream()
                            .anyMatch(prr -> prr.getRaceNumber().equals(raceNumber)))
                    .toList();

            List<PersonResult> group1 = forRace.stream()
                    .filter(pr -> statusForRace(pr, raceNumber) == ResultStatus.OK)
                    .sorted(Comparator.comparingDouble((PersonResult pr) -> runtimeForRace(pr, raceNumber))
                            .thenComparingLong(pr -> pr.personId().value()))
                    .toList();

            List<PersonResult> group2 = forRace.stream()
                    .filter(pr -> statusForRace(pr, raceNumber) == ResultStatus.NOT_COMPETING
                            && runtimeForRace(pr, raceNumber) < Double.MAX_VALUE)
                    .sorted(Comparator.comparingDouble((PersonResult pr) -> runtimeForRace(pr, raceNumber))
                            .thenComparingLong(pr -> pr.personId().value()))
                    .toList();

            // group3: everything not in group1 or group2
            // (includes NOT_COMPETING without valid runtime, MissingPunch, DNF, etc.)
            Set<PersonId> inGroup1or2 = java.util.stream.Stream.concat(group1.stream(), group2.stream())
                    .map(PersonResult::personId)
                    .collect(Collectors.toSet());
            List<PersonResult> group3 = forRace.stream()
                    .filter(pr -> !inGroup1or2.contains(pr.personId()))
                    .sorted(Comparator.comparingDouble((PersonResult pr) -> runtimeForRace(pr, raceNumber))
                            .thenComparingLong(pr -> pr.personId().value()))
                    .toList();

            // Assign positions with Olympic ranking: tied runtimes share the same position
            int pos = 1;
            double lastRuntime = Double.NaN;
            int lastPos = 1;
            for (int i = 0; i < group1.size(); i++) {
                PersonResult pr = group1.get(i);
                double rt = runtimeForRace(pr, raceNumber);
                if (i == 0 || rt != lastRuntime) {
                    lastPos = pos;
                    lastRuntime = rt;
                }
                PersonRaceResult prr = updatedPrr.get(pr.personId()).get(raceNumber);
                updatedPrr.get(pr.personId()).put(raceNumber, withPosition(prr, lastPos));
                pos++;
            }
            // group2 and group3 get sequential positions for correct DB ordering,
            // but the UI only displays positions for OK results.
            for (PersonResult pr : group2) {
                PersonRaceResult prr = updatedPrr.get(pr.personId()).get(raceNumber);
                updatedPrr.get(pr.personId()).put(raceNumber, withPosition(prr, pos++));
            }
            for (PersonResult pr : group3) {
                PersonRaceResult prr = updatedPrr.get(pr.personId()).get(raceNumber);
                updatedPrr.get(pr.personId()).put(raceNumber, withPosition(prr, pos++));
            }

            // Capture the display order from the primary race
            if (raceNumber.equals(primaryRace)) {
                List<PersonResult> ordered = new ArrayList<>(group1);
                ordered.addAll(group2);
                ordered.addAll(group3);
                sortedOrder = ordered;
            }
        }

        // Return PersonResults in sorted group order (group1 → group2 → group3 by runtime)
        final List<PersonResult> finalOrder = sortedOrder != null ? sortedOrder : personResults;
        return finalOrder.stream().map(pr -> {
            List<PersonRaceResult> newPrrs = new ArrayList<>(updatedPrr.get(pr.personId()).values());
            return PersonResult.of(pr.classResultShortName(), pr.personId(), pr.organisationId(), newPrrs);
        }).toList();
    }

    private ResultStatus statusForRace(PersonResult pr, RaceNumber raceNumber) {
        return pr.personRaceResults().value().stream()
                .filter(prr -> prr.getRaceNumber().equals(raceNumber))
                .map(PersonRaceResult::getState)
                .findFirst()
                .orElse(null);
    }

    private double runtimeForRace(PersonResult pr, RaceNumber raceNumber) {
        return pr.personRaceResults().value().stream()
                .filter(prr -> prr.getRaceNumber().equals(raceNumber))
                .map(prr -> prr.getRuntime() != null && prr.getRuntime().value() != null
                        ? prr.getRuntime().value()
                        : Double.MAX_VALUE)
                .findFirst()
                .orElse(Double.MAX_VALUE);
    }

    private PersonRaceResult withPosition(PersonRaceResult prr, int position) {
        return new PersonRaceResult(
                prr.getClassResultShortName(),
                prr.getPersonId(),
                prr.getStartTime(),
                prr.getFinishTime(),
                prr.getRuntime(),
                Position.of((long) position),
                prr.getState(),
                prr.getRaceNumber(),
                prr.getSplitTimeListId());
    }

    private PersonRaceResult withNullPosition(PersonRaceResult prr) {
        return new PersonRaceResult(
                prr.getClassResultShortName(),
                prr.getPersonId(),
                prr.getStartTime(),
                prr.getFinishTime(),
                prr.getRuntime(),
                Position.of(null),
                prr.getState(),
                prr.getRaceNumber(),
                prr.getSplitTimeListId());
    }

    private boolean isEligible(
            PersonResult pr, Organisation baseOrg, Map<OrganisationId, Organisation> orgTree) {
        OrganisationId orgId = pr.organisationId();
        if (orgId == null) return false;
        return baseOrg.containsOrganisationWithId(orgId, orgTree);
    }

    /**
     * Returns the non-OK status of the person's first source PersonRaceResult,
     * used when no OK result exists to preserve the original status (e.g. MissingPunch, DidNotFinish).
     */
    private ResultStatus bestSourceStatus(PersonResult pr) {
        return pr.personRaceResults().value().stream()
                .map(PersonRaceResult::getState)
                .filter(s -> s != null && !ResultStatus.OK.equals(s))
                .findFirst()
                .orElse(ResultStatus.MISSING_PUNCH);
    }

    /**
     * Returns the best (minimum) runtime for the person regardless of status, or Double.MAX_VALUE if none.
     * This handles the case where a previous cleanup has already changed OK → NOT_COMPETING
     * but preserved the original runtime.
     */
    private double bestRuntime(PersonResult pr) {
        return pr.personRaceResults().value().stream()
                .map(prr -> prr.getRuntime() != null && prr.getRuntime().value() != null
                        ? prr.getRuntime().value()
                        : Double.MAX_VALUE)
                .min(Double::compareTo)
                .orElse(Double.MAX_VALUE);
    }

    /**
     * Returns true if the ResultList represents race 0 (championship ranking list).
     * getRaceNumber() throws if there are no PersonRaceResults, so we wrap it.
     */
    private boolean isRace0(ResultList resultList) {
        if (resultList.getClassResults() == null || resultList.getClassResults().isEmpty()) return false;
        try {
            RaceNumber rn = resultList.getRaceNumber();
            return rn.value() != null && rn.value() == 0;
        } catch (NoSuchElementException e) {
            // ResultList has no PersonRaceResults — cannot be a race-0 list
            return false;
        }
    }
}
