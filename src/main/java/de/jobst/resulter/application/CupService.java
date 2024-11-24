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

    public CupService(CupRepository cupRepository, OrganisationRepository organisationRepository,
                      OrganisationService organisationService,
                      RaceService raceService,
                      ResultListService resultListService,
                      EventRepository eventRepository, CupScoreListRepository cupScoreListRepository,
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
            List<Event> events = x.orElseThrow().getEvents().stream().toList();
            List<Race> races = raceService.findAllByEvents(events);
            List<EventResultList> eventResultLists = events.stream()
                .map(event -> new EventResultLists(event, resultListService.findByEventId(event.getId())))
                .flatMap(rl2 -> rl2.resultLists().stream().map(rl -> new EventResultList(rl2.event(), rl)))
                .toList();
            List<List<CupScoreList>> cupScoreLists = eventResultLists.stream()
                .map(r -> resultListService.getCupScoreLists(r.resultList().getId(), cupId).stream().toList())
                .toList();
            var eventRacesCupScore = calculateSums(events, eventResultLists, races, cupScoreLists);

            return new CupDetailed(x.orElseThrow(), eventRacesCupScore);
        });
    }

    private List<EventRacesCupScore> calculateSums(List<Event> events,
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
                    return new RaceCupScore(race, organisations.stream().map(organisation -> {
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
                    return new RaceCupScore(race, null);
                }
            }).sorted().toList();
            return new EventRacesCupScore(event, eventRaces);
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

        Set<OrganisationId> referencedOrganisationIds =
            resultLists.stream().flatMap(resultList -> resultList.getReferencedOrganisationIds().stream()).collect(
                Collectors.toUnmodifiableSet());

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
}
