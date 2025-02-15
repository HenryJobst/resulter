package de.jobst.resulter.adapter.driven.jdbc;

import com.turkraft.springfilter.converter.FilterStringConverter;
import com.turkraft.springfilter.parser.node.FilterNode;
import com.turkraft.springfilter.transformer.FilterNodeTransformer;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformResult;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformer;
import de.jobst.resulter.adapter.driver.web.FilterAndSortConverter;
import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.domain.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
@Slf4j
public class CupRepositoryDataJdbcAdapter implements CupRepository {

    private final CupJdbcRepository cupJdbcRepository;
    private final EventJdbcRepository eventJdbcRepository;
    private final CountryJdbcRepository countryJdbcRepository;
    private final OrganisationJdbcRepository organisationJdbcRepository;

    private final FilterStringConverter filterStringConverter;
    private final FilterNodeTransformer<MappingFilterNodeTransformResult> filterNodeTransformer;

    public CupRepositoryDataJdbcAdapter(
            CupJdbcRepository cupJdbcRepository,
            EventJdbcRepository eventJdbcRepository,
            CountryJdbcRepository countryJdbcRepository,
            OrganisationJdbcRepository organisationJdbcRepository,
            FilterStringConverter filterStringConverter) {
        this.cupJdbcRepository = cupJdbcRepository;
        this.eventJdbcRepository = eventJdbcRepository;
        this.countryJdbcRepository = countryJdbcRepository;
        this.organisationJdbcRepository = organisationJdbcRepository;
        this.filterStringConverter = filterStringConverter;
        this.filterNodeTransformer = new MappingFilterNodeTransformer(new DefaultConversionService());
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
        return id -> organisationJdbcRepository
                .findById(id)
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
        dboResolvers.setCupDboDboResolver(
                id -> cupJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setEventDboResolver(
                id -> eventJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setCountryDboResolver(
                id -> countryJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setOrganisationDboResolver(
                id -> organisationJdbcRepository.findById(id.value()).orElseThrow());

        CupDbo cupDbo = CupDbo.from(cup, dboResolvers);
        CupDbo savedCupEntity = cupJdbcRepository.save(cupDbo);
        return CupDbo.asCup(savedCupEntity, getEventResolver());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cup> findAll() {
        Iterable<CupDbo> resultList = this.cupJdbcRepository.findAll();
        return CupDbo.asCups(resultList, getEventResolver());
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
    public Page<Cup> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        Page<CupDbo> page;
        if (filter != null) {
            CupDbo cupDbo = new CupDbo();
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
                        cupDbo.setName(unquotedValue);
                        matcher.set(matcher.get().withMatcher("name", m -> m.stringMatcher(value.matcher())));
                    }
                    case "year" -> {
                        cupDbo.setYear(Integer.parseInt(unquotedValue));
                        matcher.set(matcher.get().withMatcher("year", ExampleMatcher.GenericPropertyMatcher::exact));
                    }
                    case "id" -> {
                        cupDbo.setId(Long.parseLong(unquotedValue));
                        matcher.set(matcher.get().withMatcher("id", ExampleMatcher.GenericPropertyMatcher::exact));
                    }
                }
            });

            page = cupJdbcRepository.findAll(
                    Example.of(cupDbo, matcher.get()),
                    FilterAndSortConverter.mapOrderProperties(pageable, CupDbo::mapOrdersDomainToDbo));

        } else {
            page = cupJdbcRepository.findAll(
                    FilterAndSortConverter.mapOrderProperties(pageable, CupDbo::mapOrdersDomainToDbo));
        }
        return new PageImpl<>(
                page.stream().map(x -> CupDbo.asCup(x, getEventResolver())).toList(),
                FilterAndSortConverter.mapOrderProperties(page.getPageable(), PersonDbo::mapOrdersDboToDomain),
                page.getTotalElements());
    }
}
