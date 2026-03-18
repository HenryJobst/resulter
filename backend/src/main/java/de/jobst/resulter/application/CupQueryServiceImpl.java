package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.application.port.CupBatchResult;
import de.jobst.resulter.application.port.CupDetailedBatchResult;
import de.jobst.resulter.application.port.CupQueryService;
import de.jobst.resulter.application.port.CupService;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.aggregations.CupDetailed;
import de.jobst.resulter.domain.aggregations.EventRacesCupScore;
import de.jobst.resulter.domain.aggregations.OrganisationScore;
import de.jobst.resulter.domain.aggregations.OrganisationStatistics;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CupQueryServiceImpl implements CupQueryService {

    private final CupService cupService;
    private final EventService eventService;
    private final OrganisationService organisationService;
    private final EventCertificateService eventCertificateService;
    private final CountryService countryService;
    private final ResultListService resultListService;

    public CupQueryServiceImpl(
            CupService cupService,
            EventService eventService,
            OrganisationService organisationService,
            EventCertificateService eventCertificateService,
            CountryService countryService,
            ResultListService resultListService) {
        this.cupService = cupService;
        this.eventService = eventService;
        this.organisationService = organisationService;
        this.eventCertificateService = eventCertificateService;
        this.countryService = countryService;
        this.resultListService = resultListService;
    }

    @Override
    public CupBatchResult findAll() {
        List<Cup> cups = cupService.findAll();
        return buildCupBatchResult(cups, cups.size(), Pageable.unpaged());
    }

    @Override
    public CupBatchResult findAll(String filter, Pageable pageable) {
        Page<Cup> page = cupService.findAll(filter, pageable);
        return buildCupBatchResult(page.getContent(), page.getTotalElements(), page.getPageable());
    }

    @Override
    public Optional<CupBatchResult> findById(Long id) {
        return cupService.findById(CupId.of(id))
                .map(cup -> buildCupBatchResult(List.of(cup), 1, Pageable.unpaged()));
    }

    @Override
    public Optional<CupDetailedBatchResult> findCupDetailed(Long id) {
        try {
            CupDetailed cupDetailed = cupService.getCupDetailed(CupId.of(id));
            return Optional.of(buildCupDetailedBatchResult(cupDetailed));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }

    private CupBatchResult buildCupBatchResult(List<Cup> cups, long totalElements, Pageable pageable) {
        List<EventId> allEventIds =
                cups.stream().flatMap(cup -> cup.getEventIds().stream()).distinct().toList();
        Map<EventId, Event> eventMap =
                eventService.findAllById(allEventIds).stream().collect(Collectors.toMap(Event::getId, e -> e));
        return new CupBatchResult(cups, totalElements, pageable, eventMap);
    }

    private CupDetailedBatchResult buildCupDetailedBatchResult(CupDetailed cupDetailed) {
        // Events for the cup's event key list
        Map<EventId, Event> eventMap = eventService
                .findAllById(cupDetailed.getEventIds().stream().toList())
                .stream()
                .collect(Collectors.toMap(Event::getId, e -> e));

        // Events from the EventRacesCupScore (for hasSplitTimes, organisations, certificates)
        List<Event> eventsFromCupScore = cupDetailed.getEventRacesCupScore().stream()
                .map(EventRacesCupScore::event)
                .distinct()
                .toList();

        // hasSplitTimes
        Map<Long, Boolean> hasSplitTimesMap = batchHasSplitTimes(eventsFromCupScore);

        // Organisations
        Set<OrganisationId> orgIds = eventsFromCupScore.stream()
                .flatMap(e -> e.getOrganisationIds().stream())
                .collect(Collectors.toSet());
        Map<OrganisationId, Organisation> organisationMap = organisationService.findAllByIdAsMap(orgIds);

        // EventCertificates
        Set<EventCertificateId> certIds = eventsFromCupScore.stream()
                .map(Event::getCertificate)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toSet());
        Map<EventCertificateId, EventCertificate> certificateMap =
                eventCertificateService.findAllByIdAsMap(certIds);

        // Countries and child organisations (for OrganisationScore and CupStatistics)
        List<Organisation> allOrganisations = new ArrayList<>(organisationMap.values());
        // Also include organisations from cup statistics and organisation scores
        allOrganisations.addAll(cupDetailed.getOverallOrganisationScores().stream()
                .map(OrganisationScore::organisation)
                .toList());
        allOrganisations.addAll(cupDetailed.getCupStatistics().organisationStatistics().stream()
                .map(OrganisationStatistics::organisation)
                .toList());

        List<Organisation> uniqueOrganisations = allOrganisations.stream().distinct().toList();

        Map<CountryId, Country> countryMap = countryService.batchLoadForOrganisations(uniqueOrganisations);
        Map<OrganisationId, Organisation> childOrganisationMap =
                organisationService.batchLoadChildOrganisations(uniqueOrganisations);

        return new CupDetailedBatchResult(
                cupDetailed,
                eventMap,
                hasSplitTimesMap,
                organisationMap,
                certificateMap,
                countryMap,
                childOrganisationMap);
    }

    Map<Long, Boolean> batchHasSplitTimes(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        Set<EventId> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        Map<EventId, List<ResultList>> resultListsByEvent = resultListService.findAllByEventIds(eventIds);
        Set<ResultListId> allResultListIds = resultListsByEvent.values().stream()
                .flatMap(List::stream)
                .map(ResultList::getId)
                .collect(Collectors.toSet());
        Set<ResultListId> resultListIdsWithSplitTimes =
                resultListService.findResultListIdsWithSplitTimes(allResultListIds);
        return events.stream()
                .collect(Collectors.toMap(
                        event -> event.getId().value(),
                        event -> resultListsByEvent.getOrDefault(event.getId(), List.of()).stream()
                                .anyMatch(rl -> resultListIdsWithSplitTimes.contains(rl.getId()))));
    }
}
