package de.jobst.resulter.application;

import de.jobst.resulter.adapter.driver.web.dto.CupDetailDto;
import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.application.port.CupScoreListRepository;
import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class CupService {

    private final CupRepository cupRepository;
    private final EventService eventService;
    private final OrganisationService organisationService;
    private final RaceService raceService;
    private final ResultListService resultListService;
    private final EventRepository eventRepository;
    private final ResultListRepository resultListRepository;
    private final CupScoreListRepository cupScoreListRepository;

    public CupService(CupRepository cupRepository,
                      EventService eventService,
                      OrganisationService organisationService,
                      RaceService raceService,
                      ResultListService resultListService,
                      EventRepository eventRepository,
                      ResultListRepository resultListRepository,
                      CupScoreListRepository cupScoreListRepository) {
        this.cupRepository = cupRepository;
        this.eventService = eventService;
        this.organisationService = organisationService;
        this.raceService = raceService;
        this.resultListService = resultListService;
        this.eventRepository = eventRepository;
        this.resultListRepository = resultListRepository;
        this.cupScoreListRepository = cupScoreListRepository;
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


    public Cup updateCup(CupId id, CupName name, CupType type, Collection<EventId> eventIds) {

        Optional<Cup> optionalCup = findById(id);
        if (optionalCup.isEmpty()) {
            return null;
        }
        Cup cup = optionalCup.get();
        cup.update(name, type, eventIds);
        return cupRepository.save(cup);
    }

    public Cup createCup(String name, CupType type, Collection<EventId> eventIds) {
        Cup cup = Cup.of(CupId.empty().value(), name, type, eventIds);
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

    public Optional<CupDetailDto> getCupDetailed(CupId cupId) {
        return Optional.ofNullable(cupRepository.findById(cupId)).map(x -> {
            List<Event> events = eventRepository.findAllById(x.orElseThrow().getEventIds());
            List<Race> races = raceService.findAllByEvents(events);
            List<EventResultList> eventResultLists = events.stream()
                .map(event -> new EventResultLists(event, resultListService.findByEventId(event.getId())))
                .flatMap(rl2 -> rl2.resultLists().stream().map(rl -> new EventResultList(rl2.event(), rl)))
                .toList();
            List<List<CupScoreList>> cupScoreLists =
                eventResultLists.stream().map(r -> resultListService.getCupScoreLists(r.resultList().getId())).toList();
            calculateSums(events, eventResultLists, races, cupScoreLists);
            return CupDetailDto.from(x.orElseThrow());
        });
    }

private void calculateSums(List<Event> events,
                               List<EventResultList> eventResultLists,
                               List<Race> races,
                               List<List<CupScoreList>> cupScoreLists) {
        /*
        events.forEach(event -> {
            var eventRaces = races.stream().filter(race -> race.getEventId().equals(event.getId()));
            eventRaces.forEach(race -> {
                var resultListsForRace = eventResultLists.stream()
                    .filter(eventResultList -> eventResultList.event().getId().equals(event.getId()) &&
                                               eventResultList.resultList().getRaceId().equals(race.getId()))
                    .toList();
                var mainResultList = resultListsForRace.stream().findFirst();
                Optional<Optional<CupScoreList>> mainCupScoreList = mainResultList.map(x -> cupScoreLists.stream()
                    .flatMap(Collection::stream)
                    .filter(cupScoreList -> cupScoreList.getResultListId().equals(x.resultList.getId()))
                    .findFirst());

                if (mainCupScoreList.isPresent() && mainCupScoreList.get().isPresent()) {
                    var cupScoreList = mainCupScoreList.get().get();
                    var organisations = organisationService.findAllById(mainResultList.get()
                        .resultList()
                        .getReferencedOrganisationIds());
                    organisations.forEach(organisation -> {
                        var cupScores = cupScoreList.getCupScores()
                            .stream()
                            .filter(cupScore -> cupScore.getOrganisationId().equals(organisation))
                            .toList();

                    });
                });
            }
        });

         */


    }

    record EventResultList(Event event, ResultList resultList) {}

    record EventResultLists(Event event, Collection<ResultList> resultLists) {};
}
