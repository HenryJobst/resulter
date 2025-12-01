package de.jobst.resulter.adapter.driven.jdbc;

import com.turkraft.springfilter.converter.FilterStringConverter;
import com.turkraft.springfilter.parser.node.FilterNode;
import com.turkraft.springfilter.transformer.FilterNodeTransformer;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformResult;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformer;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.application.port.EventCertificateRepository;
import de.jobst.resulter.domain.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.*;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
@Slf4j
public class EventCertificateRepositoryDataJdbcAdapter implements EventCertificateRepository {

    private final EventCertificateJdbcRepository eventCertificateJdbcRepository;
    private final PersonJdbcRepository personJdbcRepository;
    private final OrganisationJdbcRepository organisationJdbcRepository;
    private final CountryJdbcRepository countryJdbcRepository;

    private final FilterStringConverter filterStringConverter;
    private final FilterNodeTransformer<MappingFilterNodeTransformResult> filterNodeTransformer;

    public EventCertificateRepositoryDataJdbcAdapter(
            EventCertificateJdbcRepository eventCertificateJdbcRepository,
            PersonJdbcRepository personJdbcRepository,
            OrganisationJdbcRepository organisationJdbcRepository,
            CountryJdbcRepository countryJdbcRepository, FilterStringConverter filterStringConverter) {
        this.eventCertificateJdbcRepository = eventCertificateJdbcRepository;
        this.personJdbcRepository = personJdbcRepository;
        this.organisationJdbcRepository = organisationJdbcRepository;
        this.countryJdbcRepository = countryJdbcRepository;
        this.filterStringConverter = filterStringConverter;
        this.filterNodeTransformer = new MappingFilterNodeTransformer(new DefaultConversionService());
    }

    @Transactional
    public DboResolver<EventCertificateId, EventCertificateDbo> getIdResolver() {
        return (EventCertificateId id) -> findDboById(id).orElseThrow();
    }

    @Override
    @Transactional
    public EventCertificate save(EventCertificate eventCertificate) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setEventCertificateDboResolver(getIdResolver());
        dboResolvers.setPersonDboResolver(
                id -> personJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setOrganisationDboResolver(
                id -> organisationJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setCountryDboResolver(
                id -> countryJdbcRepository.findById(id.value()).orElseThrow());
        EventCertificateDbo eventCertificateEntity = EventCertificateDbo.from(eventCertificate, dboResolvers);
        EventCertificateDbo savedEventCertificateEntity = eventCertificateJdbcRepository.save(eventCertificateEntity);
        return EventCertificateDbo.asEventCertificate(savedEventCertificateEntity);
    }

    @Override
    public void delete(EventCertificate eventCertificate) {
        eventCertificateJdbcRepository.deleteById(eventCertificate.getId().value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventCertificate> findAll() {
        Collection<EventCertificateDbo> resultList = eventCertificateJdbcRepository.findAll();
        return EventCertificateDbo.asEventCertificates(resultList);
    }

    @Transactional(readOnly = true)
    public Optional<EventCertificateDbo> findDboById(EventCertificateId eventCertificateId) {
        return eventCertificateJdbcRepository.findById(eventCertificateId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventCertificate> findById(EventCertificateId eventCertificateId) {
        return findDboById(eventCertificateId)
                .map(EventCertificateDbo::asEventCertificate);
    }

    @Override
    public Page<EventCertificate> findAll(@Nullable String filter, Pageable pageable) {
        Page<EventCertificateDbo> page;
        if (filter != null) {
            EventCertificateDbo eventCertificateDbo = new EventCertificateDbo();
            eventCertificateDbo.setPrimary(null);
            AtomicReference<ExampleMatcher> matcher = new AtomicReference<>(
                    ExampleMatcher.matching()
                    // .withIgnorePaths("")
                    );
            FilterNode filterNode = filterStringConverter.convert(filter);
            log.info("FilterNode: {}", filterNode);
            MappingFilterNodeTransformResult transformResult = filterNodeTransformer.transform(filterNode);
            transformResult.filterMap().forEach((key, value) -> {
                String unquotedValue = value.value().replace("'", "");
                switch (key) {
                    case "name" -> {
                        eventCertificateDbo.setName(unquotedValue);
                        matcher.set(matcher.get().withMatcher("name", m -> m.stringMatcher(value.matcher())));
                    }
                    case "id" -> {
                        eventCertificateDbo.setId(Long.parseLong(unquotedValue));
                        matcher.set(matcher.get().withMatcher("id", ExampleMatcher.GenericPropertyMatcher::exact));
                    }
                }
            });

            page = eventCertificateJdbcRepository.findAll(
                    Example.of(eventCertificateDbo, matcher.get()),
                    FilterAndSortConverter.mapOrderProperties(pageable, EventCertificateDbo::mapOrdersDomainToDbo));

        } else {
            page = eventCertificateJdbcRepository.findAll(
                    FilterAndSortConverter.mapOrderProperties(pageable, EventCertificateDbo::mapOrdersDomainToDbo));
        }
        return new PageImpl<>(
                page.stream()
                        .map(EventCertificateDbo::asEventCertificate)
                        .toList(),
                FilterAndSortConverter.mapOrderProperties(page.getPageable(), PersonDbo::mapOrdersDboToDomain),
                page.getTotalElements());
    }

    @Override
    public List<EventCertificate> findAllByEvent(EventId id) {
        return eventCertificateJdbcRepository.findAllByEvent(AggregateReference.to(id.value())).stream()
                .map(EventCertificateDbo::asEventCertificate)
                .toList();
    }

    @Override
    public void saveAll(List<EventCertificate> eventCertificates) {
        DboResolvers dboResolvers = new DboResolvers();
        dboResolvers.setEventCertificateDboResolver(
                x -> eventCertificateJdbcRepository.findById(x.value()).orElseThrow());
        List<EventCertificateDbo> eventCertificateDbos = eventCertificates.stream()
                .map(x -> EventCertificateDbo.from(x, dboResolvers))
                .toList();
        eventCertificateJdbcRepository.saveAll(eventCertificateDbos);
    }

    @Override
    public void deleteAllByEventId(EventId eventId) {
        eventCertificateJdbcRepository.deleteAll(
                eventCertificateJdbcRepository.findAllByEvent(AggregateReference.to(eventId.value())));
    }
}
