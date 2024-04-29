package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.adapter.driver.web.FilterAndSortConverter;
import de.jobst.resulter.application.port.EventCertificateStatRepository;
import de.jobst.resulter.domain.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class EventCertificateStatRepositoryDataJdbcAdapter implements EventCertificateStatRepository {

    private final EventCertificateStatJdbcRepository eventCertificateStatJdbcRepository;
    private final PersonJdbcRepository personJdbcRepository;
    private final OrganisationJdbcRepository organisationJdbcRepository;
    private final CountryJdbcRepository countryJdbcRepository;
    private final EventJdbcRepository eventJdbcRepository;

    public EventCertificateStatRepositoryDataJdbcAdapter(EventCertificateStatJdbcRepository eventCertificateStatJdbcRepository,
                                                         PersonJdbcRepository personJdbcRepository,
                                                         OrganisationJdbcRepository organisationJdbcRepository,
                                                         CountryJdbcRepository countryJdbcRepository,
                                                         EventJdbcRepository eventJdbcRepository) {
        this.eventCertificateStatJdbcRepository = eventCertificateStatJdbcRepository;
        this.personJdbcRepository = personJdbcRepository;
        this.organisationJdbcRepository = organisationJdbcRepository;
        this.countryJdbcRepository = countryJdbcRepository;
        this.eventJdbcRepository = eventJdbcRepository;
    }

    @Transactional
    public DboResolver<EventCertificateStatId, EventCertificateStatDbo> getIdResolver() {
        return (EventCertificateStatId id) -> findDboById(id).orElseThrow();
    }

    @NonNull
    private Function<Long, Organisation> getOrganisationResolver() {
        return id -> organisationJdbcRepository.findById(id)
            .orElseThrow()
            .asOrganisation(getOrganisationResolver(), getCountryResolver());
    }

    @NonNull
    private Function<Long, EventCertificateStat> getEventCertificateStatResolver() {
        return id -> EventCertificateStatDbo.asEventCertificateStat(eventCertificateStatJdbcRepository.findById(id)
            .orElseThrow(), getEventResolver(), getPersonResolver());
    }

    @NonNull
    private Function<Long, Country> getCountryResolver() {
        return id -> countryJdbcRepository.findById(id).orElseThrow().asCountry();
    }

    @NotNull
    private Function<Long, Event> getEventResolver() {
        return id -> EventDbo.asEvent(eventJdbcRepository.findById(id).orElseThrow(), getOrganisationResolver());
    }

    @NotNull
    private Function<Long, Person> getPersonResolver() {
        return id -> personJdbcRepository.findById(id).orElseThrow().asPerson();
    }

    @Override
    @Transactional
    public EventCertificateStat save(EventCertificateStat eventCertificateStat) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setEventCertificateStatDboResolver(getIdResolver());
        dboResolvers.setPersonDboResolver(id -> personJdbcRepository.findById(id.value()).orElseThrow());
        EventCertificateStatDbo eventCertificateStatEntity =
            EventCertificateStatDbo.from(eventCertificateStat, dboResolvers);
        EventCertificateStatDbo savedEventCertificateStatEntity =
            eventCertificateStatJdbcRepository.save(eventCertificateStatEntity);
        return EventCertificateStatDbo.asEventCertificateStat(savedEventCertificateStatEntity,
            getEventResolver(),
            getPersonResolver());
    }

    @Override
    public void delete(EventCertificateStat eventCertificateStat) {
        eventCertificateStatJdbcRepository.deleteById(eventCertificateStat.getId().value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventCertificateStat> findAll() {
        Collection<EventCertificateStatDbo> resultList = eventCertificateStatJdbcRepository.findAll();
        return EventCertificateStatDbo.asEventCertificateStats(resultList, getEventResolver(), getPersonResolver());
    }

    @Transactional(readOnly = true)
    public Optional<EventCertificateStatDbo> findDboById(EventCertificateStatId eventCertificateStatId) {
        return eventCertificateStatJdbcRepository.findById(eventCertificateStatId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventCertificateStat> findById(EventCertificateStatId eventCertificateStatId) {
        return findDboById(eventCertificateStatId).map(x -> EventCertificateStatDbo.asEventCertificateStat(x,
            getEventResolver(),
            getPersonResolver()));
    }

    @Override
    public Page<EventCertificateStat> findAll(String filter, @NonNull Pageable pageable) {
        Page<EventCertificateStatDbo> page =
            eventCertificateStatJdbcRepository.findAll(FilterAndSortConverter.mapOrderProperties(pageable,
                EventCertificateStatDbo::mapOrdersDomainToDbo));
        return new PageImpl<>(page.stream()
            .map(x -> EventCertificateStatDbo.asEventCertificateStat(x, getEventResolver(), getPersonResolver()))
            .toList(),
            FilterAndSortConverter.mapOrderProperties(page.getPageable(), EventCertificateDbo::mapOrdersDboToDomain),
            page.getTotalElements());
    }

    @Override
    public List<EventCertificateStat> findAllByEvent(EventId id) {
        return eventCertificateStatJdbcRepository.findAllByEvent(AggregateReference.to(id.value()))
            .stream()
            .map(x -> EventCertificateStatDbo.asEventCertificateStat(x, getEventResolver(), getPersonResolver()))
            .toList();
    }

    @Override
    public void saveAll(List<EventCertificateStat> eventCertificateStats) {
        DboResolvers dboResolvers = new DboResolvers();
        dboResolvers.setEventCertificateStatDboResolver(x -> eventCertificateStatJdbcRepository.findById(x.value())
            .orElseThrow());
        List<EventCertificateStatDbo> eventCertificateStatDbos =
            eventCertificateStats.stream().map(x -> EventCertificateStatDbo.from(x, dboResolvers)).toList();
        eventCertificateStatJdbcRepository.saveAll(eventCertificateStatDbos);
    }
}
