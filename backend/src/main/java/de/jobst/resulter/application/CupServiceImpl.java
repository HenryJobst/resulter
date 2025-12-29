package de.jobst.resulter.application;

import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.aggregations.*;
import de.jobst.resulter.domain.scoring.CupTypeCalculationStrategy;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import de.jobst.resulter.springapp.config.SpringSecurityAuditorAware;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CupServiceImpl implements CupService {

    private final CupRepository cupRepository;
    private final OrganisationRepository organisationRepository;
    private final OrganisationService organisationService;
    private final RaceService raceService;
    private final ResultListService resultListService;
    private final EventService eventService;
    private final CupScoreListRepository cupScoreListRepository;
    private final SpringSecurityAuditorAware springSecurityAuditorAware;
    private final PersonRepository personRepository;

    public CupServiceImpl(
            CupRepository cupRepository,
            OrganisationRepository organisationRepository,
            OrganisationService organisationService,
            RaceService raceService,
            ResultListService resultListService,
            EventService eventService,
            CupScoreListRepository cupScoreListRepository,
            SpringSecurityAuditorAware springSecurityAuditorAware,
            PersonRepository personRepository) {
        this.cupRepository = cupRepository;
        this.organisationRepository = organisationRepository;
        this.organisationService = organisationService;
        this.raceService = raceService;
        this.resultListService = resultListService;
        this.eventService = eventService;
        this.cupScoreListRepository = cupScoreListRepository;
        this.springSecurityAuditorAware = springSecurityAuditorAware;
        this.personRepository = personRepository;
    }

    @Override
    public List<Cup> findAll() {
        return cupRepository.findAll();
    }

    @Override
    public Cup findOrCreate(Cup cup) {
        return cupRepository.findOrCreate(cup);
    }

    @Override
    public Cup getById(CupId cupId) {
        return cupRepository.findById(cupId).orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Cup updateCup(CupId id, CupName name, CupType type, Year year, Collection<EventId> eventIds) {
        var events = eventService.findAllById(eventIds);
        return cupRepository.save(getById(id)
                .update(name, type, year, events.stream().map(Event::getId).toList()));
    }

    @Override
    public Cup createCup(String name, CupType type, Year year, Collection<EventId> eventIds) {
        Cup cup = Cup.of(CupId.empty().value(), name, type, year, eventIds);
        return cupRepository.save(cup);
    }

    @Override
    public void deleteCup(CupId cupId) {
        Cup cup = getById(cupId);
        cupRepository.deleteCup(cup);
    }

    @Override
    public Page<Cup> findAll(@Nullable String filterString, Pageable pageable) {
        return cupRepository.findAll(filterString, pageable);
    }

    @Override
    public CupDetailed getCupDetailed(CupId cupId) {
        Cup cup = getById(cupId);
        List<EventId> eventIds = cup.getEventIds().stream().toList();
        List<Race> races = raceService.findAllByEventIds(eventIds);
        List<EventResultList> eventResultLists = eventIds.stream()
                .map(eventId ->
                        new EventResultLists(eventService.getById(eventId), resultListService.findByEventId(eventId)))
                .flatMap(rl2 -> rl2.resultLists().stream().map(rl -> new EventResultList(rl2.event(), rl)))
                .sorted()
                .toList();
        List<List<CupScoreList>> cupScoreLists = eventResultLists.stream()
                .map(r -> resultListService.getCupScoreLists(r.resultList().getId(), cupId).stream()
                        .toList())
                .toList();

        // Load organization tree for NOR strategy validation
        Set<OrganisationId> referencedOrganisationIds = eventResultLists.stream()
                .map(EventResultList::resultList)
                .flatMap(resultList -> resultList.getReferencedOrganisationIds().stream())
                .collect(Collectors.toUnmodifiableSet());

        Map<OrganisationId, Organisation> organisationById =
                organisationRepository.loadOrganisationTree(referencedOrganisationIds);

        var strategy = cup.getCupTypeCalculationStrategy(organisationById);

        List<Event> events =
                eventService.getByIds(cup.getEventIds()).stream().sorted().toList();
        ClassResultAggregationResult classResultAggregationResult =
                cup.getType().isGroupedByOrganisation()
                        ? null
                        : calculateClassResultGroupedSums(events, eventResultLists, races, cupScoreLists, strategy);

        // Collect all unique person IDs from the cup scores
        Set<PersonId> allPersonIds = new HashSet<>();

        // Collect from aggregatedPersonScores (for class-grouped cups)
        if (!cup.getType().isGroupedByOrganisation() && classResultAggregationResult != null) {
            classResultAggregationResult.aggregatedPersonScoresList().stream()
                    .flatMap(aps -> aps.personWithScoreList().stream())
                    .map(PersonWithScore::id)
                    .forEach(allPersonIds::add);
        }

        // Collect from eventRacesCupScores (for organization-grouped cups)
        eventResultLists.stream()
                .flatMap(erl -> cupScoreLists.stream()
                        .flatMap(Collection::stream)
                        .filter(csl -> csl.getResultListId()
                                .equals(erl.resultList().getId()))
                        .flatMap(csl -> csl.getCupScores().stream())
                        .map(CupScore::personId))
                .forEach(allPersonIds::add);

        // Load all persons in bulk
        Map<PersonId, Person> personsById = personRepository.findAllById(allPersonIds);

        // Calculate statistics including ALL starts (OK and non-OK results)
        CupStatistics cupStatistics = calculateCupStatistics(cup, eventResultLists, strategy, organisationById);

        return new CupDetailed(
                cup,
                cup.getType().isGroupedByOrganisation()
                        ? calculateOrganisationGroupedSums(events, eventResultLists, races, cupScoreLists, strategy)
                        : classResultAggregationResult.eventRacesCupScores(),
                cup.getType().isGroupedByOrganisation()
                        ? List.of()
                        : Objects.requireNonNull(classResultAggregationResult).aggregatedPersonScoresList(),
                personsById,
                cupStatistics);
    }

    private Map<ClassResultShortName, List<PersonWithScore>> aggregatePersonScoresGroupedByClass(
            List<RaceClassResultGroupedCupScore> raceClassResultGroupedCupScores,
            CupTypeCalculationStrategy strategy,
            int eventsSize) {

        int bestOfRacesCount = strategy.getBestOfRacesCount(eventsSize);

        // Sammeln aller Ergebnisse nach PersonId und ClassResultShortName
        Map<ClassPersonKey, List<PersonWithScore>> scoresByClassAndPerson = raceClassResultGroupedCupScores.stream()
                .flatMap(x -> x.classResultScores().stream())
                .flatMap(x -> x.personWithScores().stream())
                .collect(Collectors.groupingBy(
                        x -> new ClassPersonKey(
                                strategy.harmonizeClassResultShortName(x.classResultShortName()), x.id()),
                        Collectors.mapping(x -> x, Collectors.toList())));

        // Aggregieren der besten Ergebnisse
        Map<ClassResultShortName, List<PersonWithScore>> groupedAndSortedScores =
                scoresByClassAndPerson.entrySet().stream()
                        .collect(Collectors.groupingBy(
                                entry -> entry.getKey().classResultShortName(),
                                Collectors.mapping(
                                        entry -> {
                                            ClassPersonKey key = entry.getKey();
                                            List<PersonWithScore> scores = entry.getValue();

                                            double totalScore = scores.stream()
                                                    .sorted(Comparator.comparingDouble(PersonWithScore::score)
                                                            .reversed()) // Absteigend sortieren
                                                    .limit(bestOfRacesCount)
                                                    .mapToDouble(PersonWithScore::score)
                                                    .sum();
                                            return new PersonWithScore(
                                                    key.personId(), totalScore, key.classResultShortName());
                                        },
                                        Collectors.toList())));

        groupedAndSortedScores.replaceAll((classShortName, personScores) -> personScores.stream()
                .sorted(Comparator.comparingDouble(PersonWithScore::score)
                        .reversed()
                        .thenComparing(PersonWithScore::id))
                .toList());

        return groupedAndSortedScores.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    private List<CupScore> getCupScoresForRace(
            Race race, List<EventResultList> eventResultLists, List<List<CupScoreList>> cupScoreLists) {
        var resultListsForRace = eventResultLists.stream()
                .filter(resultList -> resultList.resultList().getRaceId().equals(race.getId()))
                .toList();

        return resultListsForRace.stream()
                .findFirst()
                .flatMap(resultList -> findMainCupScoreList(resultList.resultList(), cupScoreLists))
                .map(CupScoreList::getCupScores)
                .orElse(List.of()); // Leere Liste, wenn keine CupScores vorhanden sind
    }

    public record ClassResultAggregationResult(
            List<EventRacesCupScore> eventRacesCupScores, List<AggregatedPersonScores> aggregatedPersonScoresList) {}

    private ClassResultAggregationResult calculateClassResultGroupedSums(
            List<Event> events,
            List<EventResultList> eventResultLists,
            List<Race> races,
            List<List<CupScoreList>> cupScoreLists,
            CupTypeCalculationStrategy strategy) {

        List<RaceClassResultGroupedCupScore> allClassResultScores = events.stream()
                .flatMap(event -> races.stream()
                        .filter(race -> race.getEventId().equals(event.getId()))
                        .map(race -> processRaceForClassResults(race, event, eventResultLists, cupScoreLists, strategy))
                        .sorted())
                .toList();

        Map<ClassResultShortName, List<PersonWithScore>> aggregatedPersonScores =
                aggregatePersonScoresGroupedByClass(allClassResultScores, strategy, events.size());

        // Erzeuge die EventRacesCupScores für die Detailergebnisse der Races
        List<EventRacesCupScore> eventRacesCupScores = events.stream()
                .map(event -> new EventRacesCupScore(
                        event,
                        List.of(),
                        races.stream()
                                .filter(race -> race.getEventId().equals(event.getId()))
                                .map(race -> allClassResultScores.stream()
                                        .filter(score -> score.race().equals(race))
                                        .findFirst()
                                        .orElse(new RaceClassResultGroupedCupScore(race, List.of())))
                                .sorted()
                                .toList()))
                .toList();

        return new ClassResultAggregationResult(
                eventRacesCupScores,
                aggregatedPersonScores.entrySet().stream()
                        .map(it -> new AggregatedPersonScores(it.getKey(), it.getValue()))
                        .sorted(Comparator.comparing(AggregatedPersonScores::classResultShortName))
                        .toList());
    }

    private RaceClassResultGroupedCupScore processRaceForClassResults(
            Race race,
            Event event,
            List<EventResultList> eventResultLists,
            List<List<CupScoreList>> cupScoreLists,
            CupTypeCalculationStrategy strategy) {

        var resultListsForRace = eventResultLists.stream()
                .filter(resultList -> resultList.event().getId().equals(event.getId())
                        && resultList.resultList().getRaceId().equals(race.getId()))
                .toList();

        var mainCupScoreList = resultListsForRace.stream()
                .findFirst()
                .flatMap(resultList -> findMainCupScoreList(resultList.resultList(), cupScoreLists));

        return mainCupScoreList
                .map(cupScoreList -> {
                    var classResultScores = cupScoreList.getCupScores().stream()
                            .collect(Collectors.groupingBy(cupScore ->
                                    strategy.harmonizeClassResultShortName(cupScore.classResultShortName())))
                            .entrySet()
                            .stream()
                            .map(entry -> {
                                var classResultShortName = entry.getKey();
                                var personWithScores = entry.getValue().stream()
                                        .map(score -> new PersonWithScore(
                                                score.personId(), score.score(), classResultShortName))
                                        .sorted(Comparator.comparingDouble(PersonWithScore::score)
                                                .reversed())
                                        .toList();

                                return new ClassResultScores(classResultShortName, personWithScores);
                            })
                            .sorted(Comparator.comparing(ClassResultScores::classResultShortName))
                            .toList();

                    return new RaceClassResultGroupedCupScore(race, classResultScores);
                })
                .orElseGet(() -> new RaceClassResultGroupedCupScore(race, List.of()));
    }

    private Optional<CupScoreList> findMainCupScoreList(ResultList resultList, List<List<CupScoreList>> cupScoreLists) {
        return cupScoreLists.stream()
                .flatMap(Collection::stream)
                .filter(cupScoreList -> cupScoreList.getResultListId().equals(resultList.getId()))
                .findFirst();
    }

    private List<EventRacesCupScore> calculateOrganisationGroupedSums(
        List<Event> events,
        List<EventResultList> eventResultLists,
        List<Race> races,
        List<List<CupScoreList>> cupScoreLists,
        CupTypeCalculationStrategy strategy) {
        return events.stream()
                .map(event -> {
                    var eventRaces = races.stream()
                            .filter(race -> race.getEventId().equals(event.getId()))
                            .map(race -> {
                                var resultListsForRace = eventResultLists.stream()
                                        .filter(eventResultList ->
                                                eventResultList.event().getId().equals(event.getId())
                                                        && eventResultList
                                                                .resultList()
                                                                .getRaceId()
                                                                .equals(race.getId()))
                                        .toList();
                                var mainResultList = resultListsForRace.stream().findFirst();
                                Optional<Optional<CupScoreList>> mainCupScoreList =
                                        mainResultList.map(x -> cupScoreLists.stream()
                                                .flatMap(Collection::stream)
                                                .filter(cupScoreList -> cupScoreList
                                                        .getResultListId()
                                                        .equals(x.resultList().getId()))
                                                .findFirst());

                                if (mainCupScoreList.isPresent()
                                        && mainCupScoreList.get().isPresent()) {
                                    var cupScoreList = mainCupScoreList.get().get();
                                    var organisations = organisationService.findAllById(
                                            mainResultList.get().resultList().getReferencedOrganisationIds());
                                    return new RaceOrganisationGroupedCupScore(
                                            race,
                                            organisations.stream()
                                                    .filter(strategy::valid)
                                                    .map(organisation -> {
                                                        List<CupScore> relevantCupScores =
                                                                cupScoreList.getCupScores().stream()
                                                                        .filter(cupScore -> cupScore.organisationId()
                                                                                .equals(organisation.getId()))
                                                                        .sorted()
                                                                        .toList();
                                                        return new OrganisationScore(
                                                                organisation,
                                                                relevantCupScores.stream()
                                                                        .map(CupScore::score)
                                                                        .reduce(0.0, Double::sum),
                                                                relevantCupScores.stream()
                                                                        .map(x -> new PersonWithScore(
                                                                                x.personId(),
                                                                                x.score(),
                                                                                x.classResultShortName()))
                                                                        .sorted()
                                                                        .toList());
                                                    })
                                                    .sorted()
                                                    .toList());
                                } else {
                                    return new RaceOrganisationGroupedCupScore(race, List.of());
                                }
                            })
                            .sorted()
                            .toList();
                    return new EventRacesCupScore(event, eventRaces, List.of());
                })
                .toList();
    }

    @Transactional
    @Override
    public List<CupScoreList> calculateScore(CupId id) {
        Cup cup = getById(id);
        Collection<Event> events = eventService.getByIds(cup.getEventIds());
        Collection<ResultList> resultLists = events.stream()
                .flatMap(event -> {
                    Collection<ResultList> resultListsByEvent = resultListService.findByEventId(event.getId());
                    return resultListsByEvent.stream();
                })
                .filter(x -> x.getRaceNumber().value() > 0)
                .toList();

        Set<OrganisationId> referencedOrganisationIds = resultLists.stream()
                .flatMap(resultList -> resultList.getReferencedOrganisationIds().stream())
                .collect(Collectors.toUnmodifiableSet());

        Map<OrganisationId, Organisation> organisationById =
                organisationRepository.loadOrganisationTree(referencedOrganisationIds);
        String creator = springSecurityAuditorAware.getCurrentAuditor().orElse(SpringSecurityAuditorAware.UNKNOWN);
        ZonedDateTime now = ZonedDateTime.now();

        CupTypeCalculationStrategy cupTypeCalculationStrategy = cup.getCupTypeCalculationStrategy(organisationById);

        List<CupScoreList> cupScoreLists = resultLists.stream()
                .map(resultList -> resultList.calculate(cup, creator, now, cupTypeCalculationStrategy))
                .collect(Collectors.toList());
        cupScoreListRepository.deleteAllByDomainKey(
                cupScoreLists.stream().map(CupScoreList::getDomainKey).collect(Collectors.toSet()));
        return cupScoreListRepository.saveAll(cupScoreLists);
    }

    /**
     * Calculate cup statistics including ALL starts (OK and non-OK results)
     * This differs from scoring calculations which only use OK results
     */
    private CupStatistics calculateCupStatistics(
            Cup cup,
            List<EventResultList> eventResultLists,
            CupTypeCalculationStrategy strategy,
            Map<OrganisationId, Organisation> organisationById) {

        // Collect ALL PersonRaceResults (not just OK status)
        List<PersonRaceResultWithOrg> allPersonRaceResults = eventResultLists.stream()
                .flatMap(erl -> {
                    if (erl.resultList().getClassResults() == null) {
                        return java.util.stream.Stream.empty();
                    }
                    return erl.resultList().getClassResults().stream()
                            .filter(strategy::valid)  // Only valid classes
                            .flatMap(classResult -> classResult.personResults().value().stream()
                                    .filter(strategy::valid)  // Only valid persons (org membership)
                                    .flatMap(personResult -> personResult.personRaceResults().value().stream()
                                            .map(prr -> new PersonRaceResultWithOrg(
                                                    prr,
                                                    personResult.personId(),
                                                    personResult.organisationId()
                                            ))
                                    )
                            );
                })
                .toList();

        // Count unique persons
        Set<PersonId> uniquePersons = allPersonRaceResults.stream()
                .map(PersonRaceResultWithOrg::personId)
                .collect(Collectors.toSet());

        // Count unique organizations
        Set<OrganisationId> uniqueOrganisations = allPersonRaceResults.stream()
                .map(PersonRaceResultWithOrg::organisationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Count total starts and non-scoring starts
        int totalStarts = allPersonRaceResults.size();
        int nonScoringStarts = (int) allPersonRaceResults.stream()
                .filter(prr -> !prr.personRaceResult().getState().equals(ResultStatus.OK))
                .count();

        // Calculate overall statistics
        CupOverallStatistics overallStats = CupOverallStatistics.of(
                uniquePersons.size(),
                uniqueOrganisations.size(),
                totalStarts,
                nonScoringStarts
        );

        // Calculate per-organization statistics
        List<OrganisationStatistics> orgStats = uniqueOrganisations.stream()
                .map(orgId -> {
                    Organisation org = organisationById.get(orgId);
                    if (org == null) {
                        return null;
                    }

                    List<PersonRaceResultWithOrg> orgResults = allPersonRaceResults.stream()
                            .filter(prr -> orgId.equals(prr.organisationId()))
                            .toList();

                    Set<PersonId> orgPersons = orgResults.stream()
                            .map(PersonRaceResultWithOrg::personId)
                            .collect(Collectors.toSet());

                    int orgTotalStarts = orgResults.size();
                    int orgNonScoringStarts = (int) orgResults.stream()
                            .filter(prr -> !prr.personRaceResult().getState().equals(ResultStatus.OK))
                            .count();

                    return OrganisationStatistics.of(
                            org,
                            orgPersons.size(),
                            orgTotalStarts,
                            orgNonScoringStarts
                    );
                })
                .filter(Objects::nonNull)
                .sorted()  // Sort by runner count descending
                .toList();

        return new CupStatistics(overallStats, orgStats);
    }

    // Helper record for internal use in calculateCupStatistics
    private record PersonRaceResultWithOrg(
            PersonRaceResult personRaceResult,
            PersonId personId,
            OrganisationId organisationId
    ) {}

    private record ClassPersonKey(ClassResultShortName classResultShortName, PersonId personId) {}
}
