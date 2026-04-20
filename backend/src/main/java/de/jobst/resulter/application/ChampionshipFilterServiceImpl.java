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
    @Transactional
    public void applyChampionshipCleanup(EventId eventId, OrganisationId baseOrgId) {
        Collection<ResultList> resultLists = resultListRepository.findByEventId(eventId);

        Map<OrganisationId, Organisation> orgTree = loadOrgTree(resultLists, baseOrgId);
        Organisation baseOrg = findBaseOrg(orgTree, baseOrgId);

        for (ResultList resultList : resultLists) {
            applyCleanupToResultList(resultList, baseOrg, orgTree);
            resultListRepository.update(resultList);
        }
    }

    @Override
    @Transactional
    public List<ResultList> addChampionshipRanking(EventId eventId, OrganisationId baseOrgId) {
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
            List<PersonResult> nonEligible = new ArrayList<>();
            for (PersonResult pr : allPersonResults) {
                if (isEligible(pr, baseOrg, orgTree)) {
                    eligible.add(pr);
                } else {
                    nonEligible.add(pr);
                }
            }

            // Sort eligible by best OK runtime ascending, with personId as tiebreaker
            eligible = eligible.stream()
                    .sorted(Comparator.comparingDouble(this::bestOkRuntime)
                            .thenComparingLong(pr -> pr.personId().value()))
                    .collect(Collectors.toList());
            // Sort non-eligible by best OK runtime ascending, with personId as tiebreaker
            nonEligible = nonEligible.stream()
                    .sorted(Comparator.comparingDouble(this::bestOkRuntime)
                            .thenComparingLong(pr -> pr.personId().value()))
                    .collect(Collectors.toList());

            List<PersonResult> rankedPersonResults = new ArrayList<>();
            int position = 1;

            // Eligible: positions 1..n, state=OK (or source state if no OK result)
            for (PersonResult pr : eligible) {
                double best = bestOkRuntime(pr);
                boolean hasOkResult = best < Double.MAX_VALUE;
                PersonRaceResult newRaceResult = new PersonRaceResult(
                        shortName,
                        pr.personId(),
                        DateTime.empty(),
                        DateTime.empty(),
                        hasOkResult ? PunchTime.of(best) : PunchTime.of(null),
                        Position.of((long) position),
                        hasOkResult ? ResultStatus.OK : bestSourceStatus(pr),
                        RaceNumber.of((byte) 0),
                        null);
                rankedPersonResults.add(PersonResult.of(
                        shortName, pr.personId(), pr.organisationId(), List.of(newRaceResult)));
                position++;
            }

            // Non-eligible: positions n+1..m, state=NOT_COMPETING (only if source result is OK)
            for (PersonResult pr : nonEligible) {
                double best = bestOkRuntime(pr);
                boolean hasOkResult = best < Double.MAX_VALUE;
                PersonRaceResult newRaceResult = new PersonRaceResult(
                        shortName,
                        pr.personId(),
                        DateTime.empty(),
                        DateTime.empty(),
                        hasOkResult ? PunchTime.of(best) : PunchTime.of(null),
                        Position.of((long) position),
                        hasOkResult ? ResultStatus.NOT_COMPETING : bestSourceStatus(pr),
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

        // Build new ResultList
        ResultList newResultList = new ResultList(
                ResultListId.empty(),
                eventId,
                race0.getId(),
                creator,
                ZonedDateTime.now(),
                null,
                championshipClassResults);

        ResultList saved = resultListRepository.findOrCreate(newResultList);
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
            ResultList resultList, Organisation baseOrg, Map<OrganisationId, Organisation> orgTree) {
        if (resultList.getClassResults() == null) return;

        List<ClassResult> updatedClassResults = new ArrayList<>();
        for (ClassResult cr : resultList.getClassResults()) {
            List<PersonResult> updatedPersonResults = new ArrayList<>();
            for (PersonResult pr : cr.personResults().value()) {
                if (isEligible(pr, baseOrg, orgTree)) {
                    updatedPersonResults.add(pr);
                } else {
                    // Replace all PersonRaceResults with NOT_COMPETING copies
                    List<PersonRaceResult> notCompetingResults = pr.personRaceResults().value().stream()
                            .map(prr -> new PersonRaceResult(
                                    prr.getClassResultShortName(),
                                    prr.getPersonId(),
                                    prr.getStartTime(),
                                    prr.getFinishTime(),
                                    prr.getRuntime(),
                                    prr.getPosition(),
                                    ResultStatus.NOT_COMPETING,
                                    prr.getRaceNumber(),
                                    prr.getSplitTimeListId()))
                            .collect(Collectors.toList());
                    updatedPersonResults.add(PersonResult.of(
                            pr.classResultShortName(), pr.personId(), pr.organisationId(), notCompetingResults));
                }
            }
            ClassResult updatedClassResult = new ClassResult(
                    cr.classResultName(),
                    cr.classResultShortName(),
                    cr.gender(),
                    PersonResults.of(updatedPersonResults),
                    cr.courseId());
            updatedClassResults.add(updatedClassResult);
        }
        resultList.setClassResults(updatedClassResults);
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
     * Returns the best (minimum) OK runtime for the person, or Double.MAX_VALUE if none.
     */
    private double bestOkRuntime(PersonResult pr) {
        return pr.personRaceResults().value().stream()
                .filter(prr -> ResultStatus.OK.equals(prr.getState()))
                .map(prr -> prr.getRuntime().value())
                .filter(Objects::nonNull)
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
