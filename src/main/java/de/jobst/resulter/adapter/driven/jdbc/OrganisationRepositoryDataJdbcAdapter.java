package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.adapter.driver.web.FilterAndSortConverter;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class OrganisationRepositoryDataJdbcAdapter implements OrganisationRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final OrganisationJdbcRepository organisationJdbcRepository;
    private final CountryJdbcRepository countryJdbcRepository;

    public OrganisationRepositoryDataJdbcAdapter(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                                 OrganisationJdbcRepository organisationJdbcRepository,
                                                 CountryJdbcRepository countryJdbcRepository) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.organisationJdbcRepository = organisationJdbcRepository;
        this.countryJdbcRepository = countryJdbcRepository;
    }

    @NonNull
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
        dboResolvers.setOrganisationDboResolver(id -> organisationJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setCountryDboResolver(id -> countryJdbcRepository.findById(id.value()).orElseThrow());
        OrganisationDbo organisationDbo = OrganisationDbo.from(organisation, dboResolvers);
        OrganisationDbo savedOrganisationEntity = organisationJdbcRepository.save(organisationDbo);
        return savedOrganisationEntity.asOrganisation(getOrganisationResolver(), getCountryResolver());
    }

    @NonNull
    private Function<Long, Organisation> getOrganisationResolver() {
        return id -> organisationJdbcRepository.findById(id)
            .orElseThrow()
            .asOrganisation(getOrganisationResolver(), getCountryResolver());
    }

    @NonNull
    private Function<Long, Country> getCountryResolver() {
        return id -> countryJdbcRepository.findById(id).orElseThrow().asCountry();
    }

    @Override
    @Transactional
    public List<Organisation> findAll() {
        return organisationJdbcRepository.findAll()
            .stream()
            .map(x -> x.asOrganisation(getOrganisationResolver(), getCountryResolver()))
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
        return StreamSupport.stream(organisationJdbcRepository.findAllById(idSet.stream()
                .map(OrganisationId::value)
                .toList()).spliterator(), true)
            .map(x -> x.asOrganisation(getOrganisationResolver(), getCountryResolver()))
            .collect(Collectors.toMap(Organisation::getId, x -> x));
    }

    @Override
    @Transactional
    public Map<OrganisationId, Organisation> loadOrganisationTree(Set<OrganisationId> idSet) {
        List<Long> idValues = idSet.stream().map(OrganisationId::value).toList();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("idSet", idValues);

        List<Long> resultList = namedParameterJdbcTemplate.queryForList(getCteQuery(), parameters, Long.class);
        Set<OrganisationId> organisationIdSet = resultList.stream().map(OrganisationId::of).collect(Collectors.toSet());

        return findAllById(organisationIdSet);
    }

    @Override
    public Page<Organisation> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        Page<OrganisationDbo> page = organisationJdbcRepository.findAll(FilterAndSortConverter.mapOrderProperties(
            pageable,
            OrganisationDbo::mapOrdersDomainToDbo));
        return new PageImpl<>(page.stream()
            .map(x -> x.asOrganisation(getOrganisationResolver(), getCountryResolver()))
            .toList(),
            FilterAndSortConverter.mapOrderProperties(page.getPageable(), OrganisationDbo::mapOrdersDboToDomain),
            page.getTotalElements());
    }

    @NonNull
    @Override
    public List<Organisation> findByIds(Collection<OrganisationId> childOrganisationIds) {
        var childOrganisations =
            organisationJdbcRepository.findAllById(childOrganisationIds.stream().map(OrganisationId::value).toList());
        return StreamSupport.stream(childOrganisations.spliterator(), true)
            .map(x -> x.asOrganisation(getOrganisationResolver(), getCountryResolver()))
            .toList();
    }
}
