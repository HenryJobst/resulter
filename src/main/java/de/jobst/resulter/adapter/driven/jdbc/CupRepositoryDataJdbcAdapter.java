package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.adapter.driver.web.FilterAndSortConverter;
import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.domain.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class CupRepositoryDataJdbcAdapter implements CupRepository {

    private final CupJdbcRepository cupJdbcRepository;
    private final EventJdbcRepository eventJdbcRepository;
    private final CountryJdbcRepository countryJdbcRepository;
    private final OrganisationJdbcRepository organisationJdbcRepository;

    public CupRepositoryDataJdbcAdapter(CupJdbcRepository cupJdbcRepository,
                                        EventJdbcRepository eventJdbcRepository,
                                        CountryJdbcRepository countryJdbcRepository,
                                        OrganisationJdbcRepository organisationJdbcRepository) {
        this.cupJdbcRepository = cupJdbcRepository;
        this.eventJdbcRepository = eventJdbcRepository;
        this.countryJdbcRepository = countryJdbcRepository;
        this.organisationJdbcRepository = organisationJdbcRepository;
    }

    @NonNull
    private Function<Long, Event> getEventResolver() {
        return id -> eventJdbcRepository.findById(id).orElseThrow().asEvent(getOrganisationResolver());
    }

    @NonNull
    private Function<Long, Country> getCountryResolver() {
        return id -> countryJdbcRepository.findById(id).orElseThrow().asCountry();
    }


    @NonNull
    private Function<Long, Organisation> getOrganisationResolver() {
        return id -> organisationJdbcRepository.findById(id)
            .orElseThrow()
            .asOrganisation(getOrganisationResolver(), getCountryResolver());
    }

    @Transactional
    public DboResolver<CupId, CupDbo> getIdResolver() {
        return (CupId id) -> findDboById(id).orElse(null);
    }

    @Override
    @Transactional
    public Cup save(Cup cup) {

        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setCupDboDboResolver(id -> cupJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setEventDboResolver(id -> eventJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setCountryDboResolver(id -> countryJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setOrganisationDboResolver(id -> organisationJdbcRepository.findById(id.value()).orElseThrow());

        CupDbo cupDbo = CupDbo.from(cup, dboResolvers);
        CupDbo savedCupEntity = cupJdbcRepository.save(cupDbo);
        return CupDbo.asCup(savedCupEntity, getEventResolver());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cup> findAll() {
        Iterable<CupDbo> resultList = this.cupJdbcRepository.findAll();
        return CupDbo.asCups(resultList,
            getEventResolver());
    }

    @Transactional(readOnly = true)
    public Optional<CupDbo> findDboById(CupId cupId) {
        return cupJdbcRepository.findById(cupId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cup> findById(CupId cupId) {
        return findDboById(cupId).map((CupDbo cupDbo) -> CupDbo.asCup(cupDbo, getEventResolver()));
    }

    @Override
    @Transactional
    public Cup findOrCreate(Cup cup) {
        Optional<CupDbo> cupEntity = cupJdbcRepository.findByName(cup.getName().value());
        if (cupEntity.isEmpty()) {
            return save(cup);
        }
        CupDbo entity = cupEntity.get();
        return CupDbo.asCup(entity, getEventResolver());
    }

    @Override
    public void deleteCup(Cup cup) {
        cupJdbcRepository.deleteById(cup.getId().value());
    }

    @Override
    @Transactional
    public List<Cup> findByEvent(EventId eventId) {
        List<CupDbo> cups = cupJdbcRepository.findByEventId(eventId.value());
        return CupDbo.asCups(cups, getEventResolver());
    }

    @Override
    public Page<Cup> findAll(@Nullable String filterString, @NonNull Pageable pageable) {
        Page<CupDbo> page = cupJdbcRepository.findAll(FilterAndSortConverter.mapOrderProperties(pageable,
            CupDbo::mapOrdersDboToDomain));
        return new PageImpl<>(page.stream().map((CupDbo cupDbo) -> CupDbo.asCup(cupDbo, getEventResolver())).toList(),
            FilterAndSortConverter.mapOrderProperties(page.getPageable(), CupDbo::mapOrdersDomainToDbo),
            page.getTotalElements());
    }
}
