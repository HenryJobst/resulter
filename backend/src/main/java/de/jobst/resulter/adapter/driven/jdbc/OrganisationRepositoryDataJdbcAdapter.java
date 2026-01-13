package de.jobst.resulter.adapter.driven.jdbc;

import com.turkraft.springfilter.converter.FilterStringConverter;
import com.turkraft.springfilter.parser.node.FilterNode;
import com.turkraft.springfilter.transformer.FilterNodeTransformer;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformResult;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformer;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
@Slf4j
public class OrganisationRepositoryDataJdbcAdapter implements OrganisationRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final OrganisationJdbcRepository organisationJdbcRepository;
    private final CountryJdbcRepository countryJdbcRepository;

    private final FilterStringConverter filterStringConverter;
    private final FilterNodeTransformer<MappingFilterNodeTransformResult> filterNodeTransformer;

    public OrganisationRepositoryDataJdbcAdapter(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            OrganisationJdbcRepository organisationJdbcRepository,
            CountryJdbcRepository countryJdbcRepository,
            FilterStringConverter filterStringConverter) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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
        Collection<OrganisationDbo> organisationDbos = organisationJdbcRepository.findAll();
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
        return StreamSupport.stream(
                        organisationJdbcRepository
                                .findAllById(idSet.stream()
                                        .map(OrganisationId::value)
                                        .toList())
                                .spliterator(),
                        true)
                .map(x -> x.asOrganisation(getOrganisationResolver(), getCountryResolver()))
                .collect(Collectors.toMap(Organisation::getId, x -> x));
    }

    @Override
    @Transactional
    public Map<OrganisationId, Organisation> loadOrganisationTree(Set<OrganisationId> idSet) {
        List<Long> idValues = idSet.stream().map(OrganisationId::value).toList();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("idSet", idValues);

        List<@Nullable Long> resultList = namedParameterJdbcTemplate.queryForList(getCteQuery(), parameters, Long.class);
        Set<OrganisationId> organisationIdSet =
                resultList.stream().filter(Objects::nonNull).map(OrganisationId::of).collect(Collectors.toSet());

        return findAllById(organisationIdSet);
    }

    @Override
    public Page<Organisation> findAll(@Nullable String filter, Pageable pageable) {
        Page<OrganisationDbo> page;
        if (filter != null) {
            OrganisationDbo organisationDbo = new OrganisationDbo();
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
                        organisationDbo.setName(unquotedValue);
                        matcher.set(matcher.get().withMatcher("name", m -> m.stringMatcher(value.matcher())));
                    }
                    case "shortName" -> {
                        organisationDbo.setShortName(unquotedValue);
                        matcher.set(matcher.get().withMatcher("shortName", m -> m.stringMatcher(value.matcher())));
                    }
                    case "id" -> {
                        organisationDbo.setId(Long.parseLong(unquotedValue));
                        matcher.set(matcher.get().withMatcher("id", ExampleMatcher.GenericPropertyMatcher::exact));
                    }
                }
            });

            page = organisationJdbcRepository.findAll(
                    Example.of(organisationDbo, matcher.get()),
                    FilterAndSortConverter.mapOrderProperties(pageable, OrganisationDbo::mapOrdersDomainToDbo));

        } else {
            page = organisationJdbcRepository.findAll(
                    FilterAndSortConverter.mapOrderProperties(pageable, OrganisationDbo::mapOrdersDomainToDbo));
        }
        return new PageImpl<>(
                page.stream()
                        .map(x -> OrganisationDbo.asOrganisation(x, getOrganisationResolver(), getCountryResolver()))
                        .toList(),
                FilterAndSortConverter.mapOrderProperties(page.getPageable(), OrganisationDbo::mapOrdersDboToDomain),
                page.getTotalElements());
    }

    @Override
    public List<Organisation> findByIds(Collection<OrganisationId> childOrganisationIds) {
        var childOrganisations = organisationJdbcRepository.findAllById(
                childOrganisationIds.stream().map(OrganisationId::value).toList());
        return StreamSupport.stream(childOrganisations.spliterator(), true)
                .map(x -> x.asOrganisation(getOrganisationResolver(), getCountryResolver()))
                .toList();
    }
}
