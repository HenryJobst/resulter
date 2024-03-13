package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.adapter.driver.web.FilterAndSortConverter;
import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.domain.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class EventRepositoryDataJdbcAdapter implements EventRepository {

    private final EventJdbcRepository eventJdbcRepository;
    private final PersonJdbcRepository personJdbcRepository;
    private final OrganisationJdbcRepository organisationJdbcRepository;
    private final CountryJdbcRepository countryJdbcRepository;
    private final EventCertificateJdbcRepository eventCertificateJdbcRepository;
    private final MediaFileJdbcRepository mediaFileJdbcRepository;

    public EventRepositoryDataJdbcAdapter(EventJdbcRepository eventJdbcRepository,
                                          PersonJdbcRepository personJdbcRepository,
                                          OrganisationJdbcRepository organisationJdbcRepository,
                                          CountryJdbcRepository countryJdbcRepository,
                                          EventCertificateJdbcRepository eventCertificateJdbcRepository,
                                          MediaFileJdbcRepository mediaFileJdbcRepository) {
        this.eventJdbcRepository = eventJdbcRepository;
        this.personJdbcRepository = personJdbcRepository;
        this.organisationJdbcRepository = organisationJdbcRepository;
        this.countryJdbcRepository = countryJdbcRepository;
        this.eventCertificateJdbcRepository = eventCertificateJdbcRepository;
        this.mediaFileJdbcRepository = mediaFileJdbcRepository;
    }

    @Transactional
    public DboResolver<EventId, EventDbo> getIdResolver() {
        return (EventId id) -> findDboById(id).orElseThrow();
    }

    @NonNull
    private Function<Long, Event> getEventResolver() {
        return id -> EventDbo.asEvent(eventJdbcRepository.findById(id).orElseThrow(),
            getOrganisationResolver(),
            getEventCertificateResolver());
    }

    @NonNull
    private Function<Long, Organisation> getOrganisationResolver() {
        return id -> organisationJdbcRepository.findById(id)
            .orElseThrow()
            .asOrganisation(getOrganisationResolver(), getCountryResolver());
    }

    @NonNull
    private Function<Long, MediaFile> getMediaFileResolver() {
        return id -> mediaFileJdbcRepository.findById(id).orElseThrow().asMediaFile();
    }

    @NonNull
    private Function<Long, EventCertificate> getEventCertificateResolver() {
        return id -> EventCertificateDbo.asEventCertificate(eventCertificateJdbcRepository.findById(id).orElseThrow(),
            getEventResolver(),
            getMediaFileResolver());
    }

    @NonNull
    private Function<Long, Country> getCountryResolver() {
        return id -> countryJdbcRepository.findById(id).orElseThrow().asCountry();
    }

    @Override
    @Transactional
    public Event save(Event event) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setEventDboResolver(getIdResolver());
        dboResolvers.setPersonDboResolver(id -> personJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setOrganisationDboResolver(id -> organisationJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setCountryDboResolver(id -> countryJdbcRepository.findById(id.value()).orElseThrow());
        EventDbo eventEntity = EventDbo.from(event, dboResolvers);
        EventDbo savedEventEntity = eventJdbcRepository.save(eventEntity);
        return EventDbo.asEvent(savedEventEntity, getOrganisationResolver(), getEventCertificateResolver());
    }

    @Override
    public void deleteEvent(Event event) {
        eventJdbcRepository.deleteById(event.getId().value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findAll() {
        Collection<EventDbo> resultList = eventJdbcRepository.findAll();
        return EventDbo.asEvents(resultList, getOrganisationResolver(), getEventCertificateResolver());
    }

    @Transactional(readOnly = true)
    public Optional<EventDbo> findDboById(EventId eventId) {
        return eventJdbcRepository.findById(eventId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findById(EventId eventId) {
        return findDboById(eventId).map(x -> EventDbo.asEvent(x,
            getOrganisationResolver(),
            getEventCertificateResolver()));
    }

    @Override
    @Transactional
    public Event findOrCreate(Event event) {
        Optional<EventDbo> optionalEventDbo = eventJdbcRepository.findByName(event.getName().value());
        if (optionalEventDbo.isEmpty()) {
            return save(event);
        }
        return EventDbo.asEvent(optionalEventDbo.get(), getOrganisationResolver(), getEventCertificateResolver());
    }

    @Override
    public Page<Event> findAll(String filter, @NonNull Pageable pageable) {
        Page<EventDbo> page = eventJdbcRepository.findAll(FilterAndSortConverter.mapOrderProperties(pageable,
            EventDbo::mapOrdersDomainToDbo));
        return new PageImpl<>(page.stream()
            .map(x -> EventDbo.asEvent(x, getOrganisationResolver(), getEventCertificateResolver()))
            .toList(),
            FilterAndSortConverter.mapOrderProperties(page.getPageable(), EventDbo::mapOrdersDboToDomain),
            page.getTotalElements());
    }
}
