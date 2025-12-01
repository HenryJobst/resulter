package de.jobst.resulter.adapter.driven.jdbc;

import com.turkraft.springfilter.converter.FilterStringConverter;
import com.turkraft.springfilter.parser.node.FilterNode;
import com.turkraft.springfilter.transformer.FilterNodeTransformer;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformResult;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformer;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.domain.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.*;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
@Slf4j
public class EventRepositoryDataJdbcAdapter implements EventRepository {

    private final EventJdbcRepository eventJdbcRepository;
    private final PersonJdbcRepository personJdbcRepository;
    private final OrganisationJdbcRepository organisationJdbcRepository;
    private final CountryJdbcRepository countryJdbcRepository;
    private final EventCertificateJdbcRepository eventCertificateJdbcRepository;
    private final MediaFileJdbcRepository mediaFileJdbcRepository;
    private final FilterStringConverter filterStringConverter;
    private final FilterNodeTransformer<MappingFilterNodeTransformResult> filterNodeTransformer;

    public EventRepositoryDataJdbcAdapter(
            EventJdbcRepository eventJdbcRepository,
            PersonJdbcRepository personJdbcRepository,
            OrganisationJdbcRepository organisationJdbcRepository,
            CountryJdbcRepository countryJdbcRepository,
            EventCertificateJdbcRepository eventCertificateJdbcRepository,
            MediaFileJdbcRepository mediaFileJdbcRepository,
            FilterStringConverter filterStringConverter) {
        this.eventJdbcRepository = eventJdbcRepository;
        this.personJdbcRepository = personJdbcRepository;
        this.organisationJdbcRepository = organisationJdbcRepository;
        this.countryJdbcRepository = countryJdbcRepository;
        this.eventCertificateJdbcRepository = eventCertificateJdbcRepository;
        this.mediaFileJdbcRepository = mediaFileJdbcRepository;
        this.filterStringConverter = filterStringConverter;
        this.filterNodeTransformer = new MappingFilterNodeTransformer(new DefaultConversionService());
    }

    @Transactional
    public DboResolver<EventId, EventDbo> getIdResolver() {
        return (EventId id) -> findDboById(id).orElseThrow();
    }

    @NonNull
    private Function<Long, Event> getEventResolver() {
        return id -> {
            Event event = EventDbo.asEvent(eventJdbcRepository.findById(id).orElseThrow(), getOrganisationResolver());
            event.withCertificate(getPrimaryEventCertificateResolver(event));
            return event;
        };
    }

    @NonNull
    private Function<Long, Organisation> getOrganisationResolver() {
        return id -> organisationJdbcRepository
                .findById(id)
                .orElseThrow()
                .asOrganisation(getOrganisationResolver(), getCountryResolver());
    }

    @NonNull
    private Function<Long, MediaFile> getMediaFileResolver() {
        return id -> mediaFileJdbcRepository.findById(id).orElseThrow().asMediaFile();
    }

    @NonNull
    private Function<Long, EventCertificate> getEventCertificateResolver() {
        return id -> EventCertificateDbo.asEventCertificate(
                eventCertificateJdbcRepository.findById(id).orElseThrow(), getMediaFileResolver());
    }

    @NonNull
    private Function<Long, Country> getCountryResolver() {
        return id -> countryJdbcRepository.findById(id).orElseThrow().asCountry();
    }

    private Function<EventId, EventCertificate> getPrimaryEventCertificateResolver(@NonNull Event event) {
        return id -> {
            Optional<EventCertificateDbo> optionalEventCertificateDbo =
                    eventCertificateJdbcRepository.findByEventAndPrimary(AggregateReference.to(id.value()), true);
            return optionalEventCertificateDbo
                    .map(eventCertificateDbo ->
                            EventCertificateDbo.asEventCertificate(eventCertificateDbo, getMediaFileResolver()))
                    .orElse(null);
        };
    }

    @NonNull
    @Override
    @Transactional
    public Event save(@NonNull Event event) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setEventDboResolver(getIdResolver());
        dboResolvers.setPersonDboResolver(
                id -> personJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setOrganisationDboResolver(
                id -> organisationJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setCountryDboResolver(
                id -> countryJdbcRepository.findById(id.value()).orElseThrow());
        EventDbo eventEntity = EventDbo.from(event, dboResolvers);
        EventDbo savedEventEntity = eventJdbcRepository.save(eventEntity);
        Event savedEvent = EventDbo.asEvent(savedEventEntity, getOrganisationResolver());
        savedEvent.withCertificate(getPrimaryEventCertificateResolver(savedEvent));
        return savedEvent;
    }

    @Override
    public void deleteEvent(Event event) {
        eventJdbcRepository.deleteById(event.getId().value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findAll() {
        Collection<EventDbo> resultList = eventJdbcRepository.findAll();
        return EventDbo.asEvents(resultList, getOrganisationResolver());
    }

    @Transactional(readOnly = true)
    public Optional<EventDbo> findDboById(EventId eventId) {
        return eventJdbcRepository.findById(eventId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findById(EventId eventId) {
        return findDboById(eventId).map(x -> {
            Event event = EventDbo.asEvent(x, getOrganisationResolver());
            event.withCertificate(getPrimaryEventCertificateResolver(event));
            return event;
        });
    }

    @Override
    @Transactional
    public Event findOrCreate(Event event) {
        Optional<EventDbo> optionalEventDbo =
                eventJdbcRepository.findByName(event.getName().value());
        if (optionalEventDbo.isEmpty()) {
            return save(event);
        }
        Event savedEvent = EventDbo.asEvent(optionalEventDbo.get(), getOrganisationResolver());
        savedEvent.withCertificate(getPrimaryEventCertificateResolver(savedEvent));
        return savedEvent;
    }

    @Override
    public Page<Event> findAll(String filter, @NonNull Pageable pageable) {
        Page<EventDbo> page;
        if (filter != null) {
            EventDbo eventDbo = new EventDbo();
            AtomicReference<ExampleMatcher> matcher = new AtomicReference<>(ExampleMatcher.matching()
                    .withIgnorePaths("organisations", "startTime", "endTime", "state", "eventCertificates"));
            // .withIncludeNullValues().withStringMatcher(ExampleMatcher.StringMatcher.ENDING);
            FilterNode filterNode = filterStringConverter.convert(filter);
            log.info("FilterNode: {}", filterNode);
            MappingFilterNodeTransformResult transformResult = filterNodeTransformer.transform(filterNode);
            transformResult.filterMap().forEach((key, value) -> {
                if (key.equals("name")) {
                    String unquotedValue = value.value().replace("'", "");
                    eventDbo.setName(unquotedValue);
                    matcher.set(matcher.get().withMatcher("name", m -> m.stringMatcher(value.matcher())));
                }
            });

            page = eventJdbcRepository.findAll(
                    Example.of(eventDbo, matcher.get()),
                    FilterAndSortConverter.mapOrderProperties(pageable, EventDbo::mapOrdersDomainToDbo));

        } else {
            page = eventJdbcRepository.findAll(
                    FilterAndSortConverter.mapOrderProperties(pageable, EventDbo::mapOrdersDomainToDbo));
        }
        return new PageImpl<>(
                page.stream()
                        .map(x -> {
                            Event event = EventDbo.asEvent(x, getOrganisationResolver());
                            event.withCertificate(getPrimaryEventCertificateResolver(event));
                            return event;
                        })
                        .toList(),
                FilterAndSortConverter.mapOrderProperties(page.getPageable(), EventDbo::mapOrdersDboToDomain),
                page.getTotalElements());
    }

    @Override
    public List<Event> findAllById(Collection<EventId> eventIds) {
        Iterable<EventDbo> events = eventJdbcRepository.findAllById(
                eventIds.stream().map(EventId::value).collect(Collectors.toSet()));
        return StreamSupport.stream(events.spliterator(), false)
                .map(x -> EventDbo.asEvent(x, getOrganisationResolver()))
                .collect(Collectors.toList());
    }
}
