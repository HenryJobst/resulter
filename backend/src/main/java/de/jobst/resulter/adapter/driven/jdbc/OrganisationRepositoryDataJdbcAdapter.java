package de.jobst.resulter.adapter.driven.jdbc;

import com.turkraft.springfilter.converter.FilterStringConverter;
import com.turkraft.springfilter.parser.node.FilterNode;
import com.turkraft.springfilter.transformer.FilterNodeTransformer;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformResult;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformer;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
@Slf4j
public class OrganisationRepositoryDataJdbcAdapter implements OrganisationRepository {

    private final JdbcClient jdbcClient;
    private final OrganisationJdbcRepository organisationJdbcRepository;
    private final CountryJdbcRepository countryJdbcRepository;
    private final FilterStringConverter filterStringConverter;
    private final FilterNodeTransformer<MappingFilterNodeTransformResult> filterNodeTransformer;

    public OrganisationRepositoryDataJdbcAdapter(
            JdbcClient jdbcClient,
            OrganisationJdbcRepository organisationJdbcRepository,
            CountryJdbcRepository countryJdbcRepository,
            FilterStringConverter filterStringConverter) {
        this.jdbcClient = jdbcClient;
        this.organisationJdbcRepository = organisationJdbcRepository;
        this.countryJdbcRepository = countryJdbcRepository;
        this.filterStringConverter = filterStringConverter;
        this.filterNodeTransformer = new MappingFilterNodeTransformer(new DefaultConversionService());
    }

    private static String getCteQuery() {
        return """
               WITH RECURSIVE parent_organisations AS (
               -- Start mit den untergeordneten Organisationen
               SELECT id FROM organisation WHERE id IN (:idSet)
               UNION ALL
               -- Rekursiver Schritt: Finde die übergeordneten Organisationen
               SELECT o.id FROM organisation o
                                INNER JOIN organisation_organisation oo ON o.id = oo.parent_organisation_id
                                INNER JOIN parent_organisations so ON oo.organisation_id = so.id
               )
               SELECT DISTINCT id FROM parent_organisations
               """;
    }

    @Override
    @Transactional
    public Organisation save(Organisation organisation) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setOrganisationDboResolver(
                id -> organisationJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setCountryDboResolver(
                id -> countryJdbcRepository.findById(id.value()).orElseThrow());
        OrganisationDbo organisationDbo = OrganisationDbo.from(organisation, dboResolvers);
        OrganisationDbo savedOrganisationEntity = organisationJdbcRepository.save(organisationDbo);
        return savedOrganisationEntity.asOrganisation(getOrganisationResolver(), getCountryResolver());
    }

    private Function<Long, Organisation> getOrganisationResolver() {
        return id -> organisationJdbcRepository
                .findById(id)
                .orElseThrow()
                .asOrganisation(getOrganisationResolver(), getCountryResolver());
    }

    private Function<Long, Country> getCountryResolver() {
        return id -> countryJdbcRepository.findById(id).orElseThrow().asCountry();
    }

    private Map<Long, Country> batchLoadCountries(Collection<OrganisationDbo> organisationDbos) {
        Set<Long> countryIds = organisationDbos.stream()
                .map(OrganisationDbo::getCountry)
                .filter(Objects::nonNull)
                .map(ref -> ref.getId())
                .collect(Collectors.toSet());
        if (countryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return countryJdbcRepository.findAllByIdIn(countryIds).stream()
                .map(CountryDbo::asCountry)
                .collect(Collectors.toMap(c -> c.getId().value(), c -> c));
    }

    @Override
    @Transactional
    public List<Organisation> findAll() {
        // Load organisations without MappedCollection to avoid N+1 queries
        List<OrganisationDbo> organisationDbos =
                organisationJdbcRepository.findAllOrganisationsWithoutChildOrganisations();

        if (organisationDbos.isEmpty()) {
            return Collections.emptyList();
        }

        // Batch load all organisation-organisation mappings in single query
        List<Long> organisationIds = organisationDbos.stream()
                .map(OrganisationDbo::getId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, Set<OrganisationOrganisationDbo>> orgChildOrgMap =
                organisationJdbcRepository.findChildOrganisationsByOrganisationIds(organisationIds).stream()
                        .collect(Collectors.groupingBy(
                                oo -> oo.getParentOrganisationId().getId(), Collectors.toSet()));

        // Populate childOrganisations in OrganisationDbo objects
        organisationDbos.forEach(org -> {
            Set<OrganisationOrganisationDbo> childOrgs =
                    orgChildOrgMap.getOrDefault(org.getId(), Collections.emptySet());
            org.setChildOrganisations(childOrgs);
        });

        // Batch load country entities and convert to domain
        Map<Long, Country> countryMap = batchLoadCountries(organisationDbos);
        return organisationDbos.stream()
                .map(x -> x.asOrganisation(Collections.emptyMap(), countryMap))
                .toList();
    }

    @Override
    @Transactional
    public Optional<Organisation> findById(OrganisationId organisationId) {
        Optional<OrganisationDbo> organisationEntity = organisationJdbcRepository.findById(organisationId.value());
        return organisationEntity.map(x -> x.asOrganisation(getOrganisationResolver(), getCountryResolver()));
    }

    @Override
    @Transactional
    public Organisation findOrCreate(Organisation organisation) {
        Optional<OrganisationDbo> organisationEntity =
                organisationJdbcRepository.findByName(organisation.getName().value());
        if (organisationEntity.isEmpty()) {
            return save(organisation);
        }
        OrganisationDbo entity = organisationEntity.get();
        return entity.asOrganisation(getOrganisationResolver(), getCountryResolver());
    }

    @Override
    @Transactional
    public Collection<Organisation> findOrCreate(Collection<Organisation> organisations) {
        return organisations.stream().map(this::findOrCreate).toList();
    }

    @Override
    public void deleteOrganisation(Organisation organisation) {
        organisationJdbcRepository.deleteById(organisation.getId().value());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<OrganisationId, Organisation> findAllById(Set<OrganisationId> idSet) {
        if (idSet.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> ids = idSet.stream().map(OrganisationId::value).toList();

        // Load organisations without MappedCollection to avoid N+1 queries
        List<OrganisationDbo> organisationDbos = organisationJdbcRepository.findAllByIdWithoutChildOrganisations(ids);

        if (organisationDbos.isEmpty()) {
            return Collections.emptyMap();
        }

        // Batch load all organisation-organisation mappings in single query
        Map<Long, Set<OrganisationOrganisationDbo>> orgChildOrgMap =
                organisationJdbcRepository.findChildOrganisationsByOrganisationIds(ids).stream()
                        .collect(Collectors.groupingBy(
                                oo -> oo.getParentOrganisationId().getId(), Collectors.toSet()));

        // Populate childOrganisations in OrganisationDbo objects
        organisationDbos.forEach(org -> {
            Set<OrganisationOrganisationDbo> childOrgs =
                    orgChildOrgMap.getOrDefault(org.getId(), Collections.emptySet());
            org.setChildOrganisations(childOrgs);
        });

        // Batch load country entities and convert to domain
        Map<Long, Country> countryMap = batchLoadCountries(organisationDbos);
        return organisationDbos.stream()
                .map(x -> x.asOrganisation(Collections.emptyMap(), countryMap))
                .collect(Collectors.toMap(Organisation::getId, x -> x));
    }

    @Override
    @Transactional
    public Map<OrganisationId, Organisation> loadOrganisationTree(Set<OrganisationId> idSet) {
        List<Long> idValues = idSet.stream().map(OrganisationId::value).toList();

        List<@Nullable Long> resultList = jdbcClient
                .sql(getCteQuery())
                .param("idSet", idValues)
                .query(Long.class)
                .list();

        Set<OrganisationId> organisationIdSet = resultList.stream()
                .filter(Objects::nonNull)
                .map(OrganisationId::of)
                .collect(Collectors.toSet());

        return findAllById(organisationIdSet);
    }

    @Override
    public Page<Organisation> findAll(@Nullable String filter, Pageable pageable) {
        Page<OrganisationDbo> page;
        String nameFilter = null;
        ExampleMatcher.StringMatcher nameMatcher = ExampleMatcher.StringMatcher.CONTAINING;
        String shortNameFilter = null;
        ExampleMatcher.StringMatcher shortNameMatcher = ExampleMatcher.StringMatcher.CONTAINING;
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
            if (transformResult.filterMap().containsKey("shortName")) {
                shortNameFilter =
                        transformResult.filterMap().get("shortName").value().replace("'", "");
                shortNameMatcher = transformResult.filterMap().get("shortName").matcher();
            }
            if (transformResult.filterMap().containsKey("id")) {
                idFilter = Long.parseLong(
                        transformResult.filterMap().get("id").value().replace("'", ""));
            }
        }

        // Use optimized query without MappedCollection loading (for both filtered and unfiltered)
        page = organisationJdbcRepository.findAllWithoutChildOrganisations(
                nameFilter,
                nameMatcher,
                shortNameFilter,
                shortNameMatcher,
                idFilter,
                FilterAndSortConverter.mapOrderProperties(pageable, OrganisationDbo::mapOrdersDomainToDbo));

        List<OrganisationDbo> organisationDbos = page.getContent();

        if (organisationDbos.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // Batch load organisation-organisation mappings for all queries
        List<Long> organisationIds = organisationDbos.stream()
                .map(OrganisationDbo::getId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, Set<OrganisationOrganisationDbo>> orgChildOrgMap =
                organisationJdbcRepository.findChildOrganisationsByOrganisationIds(organisationIds).stream()
                        .collect(Collectors.groupingBy(
                                oo -> oo.getParentOrganisationId().getId(), Collectors.toSet()));

        // Populate childOrganisations in OrganisationDbo objects
        organisationDbos.forEach(org -> {
            Set<OrganisationOrganisationDbo> childOrgs =
                    orgChildOrgMap.getOrDefault(org.getId(), Collections.emptySet());
            org.setChildOrganisations(childOrgs);
        });

        // Batch load country entities
        Map<Long, Country> countryMap = batchLoadCountries(organisationDbos);

        return new PageImpl<>(
                organisationDbos.stream()
                        .map(x -> x.asOrganisation(Collections.emptyMap(), countryMap))
                        .toList(),
                FilterAndSortConverter.mapOrderProperties(page.getPageable(), OrganisationDbo::mapOrdersDboToDomain),
                page.getTotalElements());
    }

    @Override
    public List<Organisation> findByIds(Collection<OrganisationId> childOrganisationIds) {
        if (childOrganisationIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids =
                childOrganisationIds.stream().map(OrganisationId::value).toList();

        // Load organisations without MappedCollection to avoid N+1 queries
        List<OrganisationDbo> organisationDbos = organisationJdbcRepository.findAllByIdWithoutChildOrganisations(ids);

        if (organisationDbos.isEmpty()) {
            return Collections.emptyList();
        }

        // Batch load all organisation-organisation mappings in single query
        Map<Long, Set<OrganisationOrganisationDbo>> orgChildOrgMap =
                organisationJdbcRepository.findChildOrganisationsByOrganisationIds(ids).stream()
                        .collect(Collectors.groupingBy(
                                oo -> oo.getParentOrganisationId().getId(), Collectors.toSet()));

        // Populate childOrganisations in OrganisationDbo objects
        organisationDbos.forEach(org -> {
            Set<OrganisationOrganisationDbo> childOrgs =
                    orgChildOrgMap.getOrDefault(org.getId(), Collections.emptySet());
            org.setChildOrganisations(childOrgs);
        });

        // Batch load country entities and convert to domain
        Map<Long, Country> countryMap = batchLoadCountries(organisationDbos);
        return organisationDbos.stream()
                .map(x -> x.asOrganisation(Collections.emptyMap(), countryMap))
                .toList();
    }
}
