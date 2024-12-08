package de.jobst.resulter.application;

import de.jobst.resulter.adapter.driver.web.dto.CupScoreListDto;
import de.jobst.resulter.application.config.SpringSecurityAuditorAware;
import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.application.port.CupScoreListRepository;
import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.domain.*;
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
    private final EventRepository eventRepository;
    private final CupScoreListRepository cupScoreListRepository;
    private final SpringSecurityAuditorAware springSecurityAuditorAware;

    public CupService(CupRepository cupRepository,
                      OrganisationRepository organisationRepository,
                      OrganisationService organisationService,
                      RaceService raceService,
                      ResultListService resultListService,
                      EventRepository eventRepository,
                      CupScoreListRepository cupScoreListRepository,
                      SpringSecurityAuditorAware springSecurityAuditorAware) {
        this.cupRepository = cupRepository;
        this.organisationRepository = organisationRepository;
        this.organisationService = organisationService;
        this.raceService = raceService;
        this.resultListService = resultListService;
        this.eventRepository = eventRepository;
        this.cupScoreListRepository = cupScoreListRepository;
        this.springSecurityAuditorAware = springSecurityAuditorAware;
    }

    public List<Cup> findAll() {
        return cupRepository.findAll();
    }

    public Cup findOrCreate(Cup cup) {
        return cupRepository.findOrCreate(cup);
    }

    public Optional<Cup> findById(CupId cupId) {
        return cupRepository.findById(cupId);
    }


    public Cup updateCup(CupId id, CupName name, CupType type, Year year, Collection<EventId> eventIds) {

        Optional<Cup> optionalCup = findById(id);
        if (optionalCup.isEmpty()) {
            return null;
        }
        Cup cup = optionalCup.get();
        var events = eventRepository.findAllById(eventIds);
        cup.update(name, type, year, events);
        return cupRepository.save(cup);
    }

    public Cup createCup(String name, CupType type, Year year, Collection<EventId> eventIds) {
        var events = eventRepository.findAllById(eventIds);
        Cup cup = Cup.of(CupId.empty().value(), name, type, year, events);
        return cupRepository.save(cup);
    }

    public boolean deleteCup(CupId cupId) {
        Optional<Cup> optionalCup = findById(cupId);
        if (optionalCup.isEmpty()) {
            return false;
        }
        Cup cup = optionalCup.get();
        cupRepository.deleteCup(cup);
        return true;
    }

    public Page<Cup> findAll(@Nullable String filterString, @NonNull Pageable pageable) {
        return cupRepository.findAll(filterString, pageable);
    }

    public Optional<CupDetailed> getCupDetailed(CupId cupId) {
        return Optional.ofNullable(cupRepository.findById(cupId)).map(x -> {
            Cup cup = x.orElseThrow();
            List<Event> events = cup.getEvents().stream().toList();
            List<Race> races = raceService.findAllByEvents(events);
            List<EventResultList> eventResultLists = events.stream()
                .map(event -> new EventResultLists(event, resultListService.findByEventId(event.getId())))
                .flatMap(rl2 -> rl2.resultLists().stream().map(rl -> new EventResultList(rl2.event(), rl)))
                .sorted()
                .toList();
            List<List<CupScoreList>> cupScoreLists = eventResultLists.stream()
                .map(r -> resultListService.getCupScoreLists(r.resultList().getId(), cupId).stream().toList())
                .toList();

            ClassResultAggregationResult classResultAggregationResult =
                calculateClassResultGroupedSums(events, eventResultLists, races, cupScoreLists);
            return new CupDetailed(cup,
                cup.getType().isGroupedByOrganisation() ?
                calculateOrganisationGroupedSums(events, eventResultLists, races, cupScoreLists) :
                classResultAggregationResult.eventRacesCupScores(),
                classResultAggregationResult.aggregatedPersonScores());
        });
    }

    private Map<ClassResultShortName, List<PersonWithScore>> aggregatePersonScoresGroupedByClass(List<RaceClassResultGroupedCupScore> raceClassResultGroupedCupScores) {

        int racesSize = raceClassResultGroupedCupScores.stream()
            .map(x -> x.race().getId())
            .collect(Collectors.toUnmodifiableSet())
            .size();

        // Top n/2+1 Ergebnisse auswählen
        int bestOfRacesCount = (racesSize / 2) + 1;

        // Sammeln aller Ergebnisse nach PersonId und ClassResultShortName
        Map<ClassPersonKey, List<PersonWithScore>> scoresByClassAndPerson = raceClassResultGroupedCupScores.stream()
            .flatMap(x -> x.classResultScores().stream())
            .flatMap(x -> x.personWithScores().stream())
            .collect(Collectors.groupingBy(x -> new ClassPersonKey(x.classResultShortName(), x.id()),
                Collectors.mapping(x -> x, Collectors.toList())));

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
            .sorted(Comparator.comparingDouble(PersonWithScore::score).reversed()
                .thenComparing(PersonWithScore::id))
            .toList());

        return groupedAndSortedScores.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a,
                LinkedHashMap::new));
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
                                                                         List<List<CupScoreList>> cupScoreLists) {

        List<RaceClassResultGroupedCupScore> allClassResultScores = events.stream()
            .flatMap(event -> races.stream()
                .filter(race -> race.getEventId().equals(event.getId()))
                .map(race -> processRaceForClassResults(race, event, eventResultLists, cupScoreLists))
                .sorted())
            .toList();

        Map<ClassResultShortName, List<PersonWithScore>> aggregatedPersonScores =
            aggregatePersonScoresGroupedByClass(allClassResultScores);

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

        return new ClassResultAggregationResult(eventRacesCupScores, aggregatedPersonScores);
    }

    private RaceClassResultGroupedCupScore processRaceForClassResults(Race race,
                                                                      Event event,
                                                                      List<EventResultList> eventResultLists,
                                                                      List<List<CupScoreList>> cupScoreLists) {

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
                .collect(Collectors.groupingBy(CupScore::classResultShortName))
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
                    return new RaceOrganisationGroupedCupScore(race, null);
                }
            }).sorted().toList();
            return new EventRacesCupScore(event, eventRaces, List.of());
        }).toList();
    }

    @Transactional
    public List<CupScoreListDto> calculateScore(CupId id) {
        Optional<Cup> cupOptional = findById(id);
        if (cupOptional.isEmpty()) {
            // no cup for this id
            return List.of();
        }
        Cup cup = cupOptional.get();
        Collection<Event> events = cup.getEvents();
        Collection<ResultList> resultLists = events.stream().flatMap(event -> {
            Collection<ResultList> resultListsByEvent = resultListService.findByEventId(event.getId());
            return resultListsByEvent.stream();
        }).toList();

        Set<OrganisationId> referencedOrganisationIds = resultLists.stream()
            .flatMap(resultList -> resultList.getReferencedOrganisationIds().stream())
            .collect(Collectors.toUnmodifiableSet());

        Map<OrganisationId, Organisation> organisationById =
            organisationRepository.loadOrganisationTree(referencedOrganisationIds);
        String creator = springSecurityAuditorAware.getCurrentAuditor().orElse(SpringSecurityAuditorAware.UNKNOWN);
        ZonedDateTime now = ZonedDateTime.now();

        List<CupScoreList> cupScoreLists = resultLists.stream()
            .map(resultList -> resultList.calculate(cup, organisationById, creator, now))
            .collect(Collectors.toList());
        cupScoreListRepository.deleteAllByDomainKey(cupScoreLists.stream()
            .map(CupScoreList::getDomainKey)
            .collect(Collectors.toSet()));
        return cupScoreListRepository.saveAll(cupScoreLists).stream().map(CupScoreListDto::from).toList();
    }

    public record ClassResultAggregationResult(List<EventRacesCupScore> eventRacesCupScores,
                                               Map<ClassResultShortName, List<PersonWithScore>> aggregatedPersonScores) {}

    private record ClassPersonKey(ClassResultShortName classResultShortName, PersonId personId) {}
}
