package de.jobst.resulter.adapter.driven.jdbc;

import com.turkraft.springfilter.converter.FilterStringConverter;
import com.turkraft.springfilter.parser.node.FilterNode;
import com.turkraft.springfilter.transformer.FilterNodeTransformer;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformResult;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformer;
import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.domain.*;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.*;
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
        return CupDbo.asCup(savedCupEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cup> findAll() {
        // Load cups without MappedCollection to avoid N+1 queries
        List<CupDbo> cupDbos = cupJdbcRepository.findAllCupsWithoutEvents();

        if (cupDbos.isEmpty()) {
            return List.of();
        }

        // Batch load all cup-event mappings in single query
        List<Long> cupIds = cupDbos.stream()
                .map(CupDbo::getId)
                .filter(java.util.Objects::nonNull)
                .toList();

        java.util.Map<Long, java.util.Set<CupEventDbo>> cupEventMap =
                cupJdbcRepository.findEventsByCupIds(cupIds).stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                ce -> ce.getCupId().getId(), java.util.stream.Collectors.toSet()));

        // Populate events in CupDbo objects
        cupDbos.forEach(cup -> {
            java.util.Set<CupEventDbo> events = cupEventMap.getOrDefault(cup.getId(), java.util.Collections.emptySet());
            cup.setEvents(events);
        });

        return CupDbo.asCups(cupDbos);
    }

    @Transactional(readOnly = true)
    public Optional<CupDbo> findDboById(CupId cupId) {
        return cupJdbcRepository.findById(cupId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cup> findById(CupId cupId) {
        return findDboById(cupId).map(CupDbo::asCup);
    }

    @Override
    @Transactional
    public Cup findOrCreate(Cup cup) {
        Optional<CupDbo> cupEntity = cupJdbcRepository.findByName(cup.getName().value());
        if (cupEntity.isEmpty()) {
            return save(cup);
        }
        CupDbo entity = cupEntity.get();
        return CupDbo.asCup(entity);
    }

    @Override
    public void deleteCup(Cup cup) {
        cupJdbcRepository.deleteById(cup.getId().value());
    }

    @Override
    @Transactional
    public List<Cup> findByEvent(EventId eventId) {
        List<CupDbo> cups = cupJdbcRepository.findByEventId(eventId.value());
        return CupDbo.asCups(cups);
    }

    @Override
    public Page<Cup> findAll(@Nullable String filter, Pageable pageable) {
        Page<CupDbo> page;
        String nameFilter = null;
        ExampleMatcher.StringMatcher nameMatcher = ExampleMatcher.StringMatcher.CONTAINING;
        Integer yearFilter = null;
        Long idFilter = null;

        if (filter != null) {
            // Parse filter to extract filters and matchers
            FilterNode filterNode = filterStringConverter.convert(filter);
            log.info("FilterNode: {}", filterNode);
            MappingFilterNodeTransformResult transformResult = filterNodeTransformer.transform(filterNode);

            // Extract filters if present
            if (transformResult.filterMap().containsKey("name")) {
                nameFilter = transformResult.filterMap().get("name").value().replace("'", "");
                nameMatcher = transformResult.filterMap().get("name").matcher();
            }
            if (transformResult.filterMap().containsKey("year")) {
                yearFilter = Integer.parseInt(
                        transformResult.filterMap().get("year").value().replace("'", ""));
            }
            if (transformResult.filterMap().containsKey("id")) {
                idFilter = Long.parseLong(
                        transformResult.filterMap().get("id").value().replace("'", ""));
            }
        }

        // Use optimized query without MappedCollection loading (for both filtered and unfiltered)
        page = cupJdbcRepository.findAllWithoutEvents(
                nameFilter,
                nameMatcher,
                yearFilter,
                idFilter,
                FilterAndSortConverter.mapOrderProperties(pageable, CupDbo::mapOrdersDomainToDbo));

        List<CupDbo> cupDbos = page.getContent();

        if (cupDbos.isEmpty()) {
            return new PageImpl<>(java.util.Collections.emptyList(), pageable, 0);
        }

        // Batch load cup-event mappings for all queries
        List<Long> cupIds = cupDbos.stream()
                .map(CupDbo::getId)
                .filter(java.util.Objects::nonNull)
                .toList();

        java.util.Map<Long, java.util.Set<CupEventDbo>> cupEventMap =
                cupJdbcRepository.findEventsByCupIds(cupIds).stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                ce -> ce.getCupId().getId(), java.util.stream.Collectors.toSet()));

        // Populate events in CupDbo objects
        cupDbos.forEach(cup -> {
            java.util.Set<CupEventDbo> events = cupEventMap.getOrDefault(cup.getId(), java.util.Collections.emptySet());
            cup.setEvents(events);
        });

        return new PageImpl<>(
                cupDbos.stream().map(CupDbo::asCup).toList(),
                FilterAndSortConverter.mapOrderProperties(page.getPageable(), CupDbo::mapOrdersDboToDomain),
                page.getTotalElements());
    }
}
