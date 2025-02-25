package de.jobst.resulter.application;

import de.jobst.resulter.adapter.driver.web.dto.CupScoreListDto;
import de.jobst.resulter.application.config.SpringSecurityAuditorAware;
import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.application.port.CupScoreListRepository;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.aggregations.*;
import de.jobst.resulter.domain.scoring.CupTypeCalculationStrategy;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CupService {

    private final CupRepository cupRepository;
    private final OrganisationRepository organisationRepository;
    private final OrganisationService organisationService;
    private final RaceService raceService;
    private final ResultListService resultListService;
    private final EventService eventService;
    private final CupScoreListRepository cupScoreListRepository;
    private final SpringSecurityAuditorAware springSecurityAuditorAware;

    public CupService(CupRepository cupRepository,
                      OrganisationRepository organisationRepository,
                      OrganisationService organisationService,
                      RaceService raceService,
                      ResultListService resultListService,
                      EventService eventService,
                      CupScoreListRepository cupScoreListRepository,
                      SpringSecurityAuditorAware springSecurityAuditorAware) {
        this.cupRepository = cupRepository;
        this.organisationRepository = organisationRepository;
        this.organisationService = organisationService;
        this.raceService = raceService;
        this.resultListService = resultListService;
        this.eventService = eventService;
        this.cupScoreListRepository = cupScoreListRepository;
        this.springSecurityAuditorAware = springSecurityAuditorAware;
    }

    public List<Cup> findAll() {
        return cupRepository.findAll();
    }

    public Cup findOrCreate(Cup cup) {
        return cupRepository.findOrCreate(cup);
    }

    public Cup getById(CupId cupId) {
        return cupRepository.findById(cupId).orElseThrow(ResourceNotFoundException::new);
    }

    public Cup updateCup(CupId id, CupName name, CupType type, Year year, Collection<EventId> eventIds) {
        var events = eventService.findAllById(eventIds);
        return cupRepository.save(getById(id).update(name, type, year,
            events.stream().map(Event::getId).toList()));
    }

    public Cup createCup(String name, CupType type, Year year, Collection<EventId> eventIds) {
        Cup cup = Cup.of(CupId.empty().value(), name, type, year, eventIds);
        return cupRepository.save(cup);
    }

    public void deleteCup(CupId cupId) {
        Cup cup = getById(cupId);
        cupRepository.deleteCup(cup);
    }

    public Page<Cup> findAll(@Nullable String filterString, @NonNull Pageable pageable) {
        return cupRepository.findAll(filterString, pageable);
    }

    public CupDetailed getCupDetailed(CupId cupId) {
        Cup cup = getById(cupId);
        List<EventId> eventIds = cup.getEventIds().stream().toList();
        List<Race> races = raceService.findAllByEventIds(eventIds);
        List<EventResultList> eventResultLists = eventIds.stream()
            .map(eventId -> new EventResultLists(eventService.getById(eventId), resultListService.findByEventId(eventId)))
            .flatMap(rl2 -> rl2.resultLists().stream().map(rl -> new EventResultList(rl2.event(), rl)))
            .sorted()
            .toList();
        List<List<CupScoreList>> cupScoreLists = eventResultLists.stream()
            .map(r -> resultListService.getCupScoreLists(r.resultList().getId(), cupId).stream().toList())
            .toList();

        var strategy = cup.getCupTypeCalculationStrategy(null);

        List<Event> events = eventService.getByIds(cup.getEventIds());
        ClassResultAggregationResult classResultAggregationResult = cup.getType().isGroupedByOrganisation() ?
                                                                    null :
                                                                    calculateClassResultGroupedSums(events,
                                                                        eventResultLists,
                                                                        races,
                                                                        cupScoreLists,
                                                                        strategy);

        return new CupDetailed(cup,
            cup.getType().isGroupedByOrganisation() ?
            calculateOrganisationGroupedSums(events, eventResultLists, races, cupScoreLists) :
            classResultAggregationResult.eventRacesCupScores(),
            cup.getType().isGroupedByOrganisation() ?
            List.of() :
            Objects.requireNonNull(classResultAggregationResult).aggregatedPersonScoresList());
    }

    private Map<ClassResultShortName, List<PersonWithScore>> aggregatePersonScoresGroupedByClass(List<RaceClassResultGroupedCupScore> raceClassResultGroupedCupScores,
                                                                                                 CupTypeCalculationStrategy strategy) {

        int racesSize = raceClassResultGroupedCupScores.stream()
            .map(x -> x.race().getId())
            .collect(Collectors.toUnmodifiableSet())
            .size();

        int bestOfRacesCount = strategy.getBestOfRacesCount(racesSize);

        // Sammeln aller Ergebnisse nach PersonId und ClassResultShortName
        Map<ClassPersonKey, List<PersonWithScore>> scoresByClassAndPerson = raceClassResultGroupedCupScores.stream()
            .flatMap(x -> x.classResultScores().stream())
            .flatMap(x -> x.personWithScores().stream())
            .collect(Collectors.groupingBy(x -> new ClassPersonKey(strategy.harmonizeClassResultShortName(x.classResultShortName()),
                x.id()), Collectors.mapping(x -> x, Collectors.toList())));

        // Aggregieren der besten Ergebnisse
        Map<ClassResultShortName, List<PersonWithScore>> groupedAndSortedScores = scoresByClassAndPerson.entrySet()
            .stream()
            .collect(Collectors.groupingBy(entry -> entry.getKey().classResultShortName(), Collectors.mapping(entry -> {
                ClassPersonKey key = entry.getKey();
                List<PersonWithScore> scores = entry.getValue();

                double totalScore = scores.stream()
                    .sorted(Comparator.comparingDouble(PersonWithScore::score).reversed()) // Absteigend sortieren
                    .limit(bestOfRacesCount)
                    .mapToDouble(PersonWithScore::score)
                    .sum();
                return new PersonWithScore(key.personId(), totalScore, key.classResultShortName());
            }, Collectors.toList())));

        groupedAndSortedScores.replaceAll((classShortName, personScores) -> personScores.stream()
            .sorted(Comparator.comparingDouble(PersonWithScore::score).reversed().thenComparing(PersonWithScore::id))
            .toList());

        return groupedAndSortedScores.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    private List<CupScore> getCupScoresForRace(Race race,
                                               List<EventResultList> eventResultLists,
                                               List<List<CupScoreList>> cupScoreLists) {
        var resultListsForRace = eventResultLists.stream()
            .filter(resultList -> resultList.resultList().getRaceId().equals(race.getId()))
            .toList();

        return resultListsForRace.stream()
            .findFirst()
            .flatMap(resultList -> findMainCupScoreList(resultList.resultList(), cupScoreLists))
            .map(CupScoreList::getCupScores)
            .orElse(List.of()); // Leere Liste, wenn keine CupScores vorhanden sind
    }

    private ClassResultAggregationResult calculateClassResultGroupedSums(List<Event> events,
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
            aggregatePersonScoresGroupedByClass(allClassResultScores, strategy);

        // Erzeuge die EventRacesCupScores für die Detailergebnisse der Races
        List<EventRacesCupScore> eventRacesCupScores = events.stream()
            .map(event -> new EventRacesCupScore(event,
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

        return new ClassResultAggregationResult(eventRacesCupScores,
            aggregatedPersonScores.entrySet()
                .stream()
                .map(it -> new AggregatedPersonScores(it.getKey(), it.getValue()))
                .sorted(Comparator.comparing(AggregatedPersonScores::classResultShortName))
                .toList());
    }

    private RaceClassResultGroupedCupScore processRaceForClassResults(Race race,
                                                                      Event event,
                                                                      List<EventResultList> eventResultLists,
                                                                      List<List<CupScoreList>> cupScoreLists,
                                                                      CupTypeCalculationStrategy strategy) {

        var resultListsForRace = eventResultLists.stream()
            .filter(resultList -> resultList.event().getId().equals(event.getId()) &&
                                  resultList.resultList().getRaceId().equals(race.getId()))
            .toList();

        var mainCupScoreList = resultListsForRace.stream()
            .findFirst()
            .flatMap(resultList -> findMainCupScoreList(resultList.resultList(), cupScoreLists));

        return mainCupScoreList.map(cupScoreList -> {
            var classResultScores = cupScoreList.getCupScores()
                .stream()
                .collect(Collectors.groupingBy(cupScore -> strategy.harmonizeClassResultShortName(cupScore.classResultShortName())))
                .entrySet()
                .stream()
                .map(entry -> {
                    var classResultShortName = entry.getKey();
                    var personWithScores = entry.getValue()
                        .stream()
                        .map(score -> new PersonWithScore(score.personId(), score.score(), classResultShortName))
                        .sorted(Comparator.comparingDouble(PersonWithScore::score).reversed())
                        .toList();

                    return new ClassResultScores(classResultShortName, personWithScores);
                })
                .sorted(Comparator.comparing(ClassResultScores::classResultShortName))
                .toList();

            return new RaceClassResultGroupedCupScore(race, classResultScores);
        }).orElseGet(() -> new RaceClassResultGroupedCupScore(race, List.of()));
    }

    private Optional<CupScoreList> findMainCupScoreList(ResultList resultList, List<List<CupScoreList>> cupScoreLists) {
        return cupScoreLists.stream()
            .flatMap(Collection::stream)
            .filter(cupScoreList -> cupScoreList.getResultListId().equals(resultList.getId()))
            .findFirst();
    }

    private List<EventRacesCupScore> calculateOrganisationGroupedSums(List<Event> events,
                                                                      List<EventResultList> eventResultLists,
                                                                      List<Race> races,
                                                                      List<List<CupScoreList>> cupScoreLists) {
        return events.stream().map(event -> {
            var eventRaces = races.stream().filter(race -> race.getEventId().equals(event.getId())).map(race -> {
                var resultListsForRace = eventResultLists.stream()
                    .filter(eventResultList -> eventResultList.event().getId().equals(event.getId()) &&
                                               eventResultList.resultList().getRaceId().equals(race.getId()))
                    .toList();
                var mainResultList = resultListsForRace.stream().findFirst();
                Optional<Optional<CupScoreList>> mainCupScoreList = mainResultList.map(x -> cupScoreLists.stream()
                    .flatMap(Collection::stream)
                    .filter(cupScoreList -> cupScoreList.getResultListId().equals(x.resultList().getId()))
                    .findFirst());

                if (mainCupScoreList.isPresent() && mainCupScoreList.get().isPresent()) {
                    var cupScoreList = mainCupScoreList.get().get();
                    var organisations = organisationService.findAllById(mainResultList.get()
                        .resultList()
                        .getReferencedOrganisationIds());
                    return new RaceOrganisationGroupedCupScore(race, organisations.stream().map(organisation -> {
                        List<CupScore> relevantCupScores = cupScoreList.getCupScores()
                            .stream()
                            .filter(cupScore -> cupScore.organisationId().equals(organisation.getId()))
                            .sorted()
                            .toList();
                        return new OrganisationScore(organisation,
                            relevantCupScores.stream().map(CupScore::score).reduce(0.0, Double::sum),
                            relevantCupScores.stream()
                                .map(x -> new PersonWithScore(x.personId(), x.score(), x.classResultShortName()))
                                .sorted()
                                .toList());
                    }).sorted().toList());
                } else {
                    return new RaceOrganisationGroupedCupScore(race, List.of());
                }
            }).sorted().toList();
            return new EventRacesCupScore(event, eventRaces, List.of());
        }).toList();
    }

    @Transactional
    public List<CupScoreListDto> calculateScore(CupId id) {
        Cup cup = getById(id);
        Collection<Event> events = eventService.getByIds(cup.getEventIds());
        Collection<ResultList> resultLists = events.stream().flatMap(event -> {
            Collection<ResultList> resultListsByEvent = resultListService.findByEventId(event.getId());
            return resultListsByEvent.stream();
        }).filter(x -> x.getRaceNumber().value() > 0).toList();

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
        cupScoreListRepository.deleteAllByDomainKey(cupScoreLists.stream()
            .map(CupScoreList::getDomainKey)
            .collect(Collectors.toSet()));
        return cupScoreListRepository.saveAll(cupScoreLists).stream().map(CupScoreListDto::from).toList();
    }

    public record ClassResultAggregationResult(List<EventRacesCupScore> eventRacesCupScores,
                                               List<AggregatedPersonScores> aggregatedPersonScoresList) {}

    private record ClassPersonKey(ClassResultShortName classResultShortName, PersonId personId) {}
}
