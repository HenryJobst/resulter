package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class OrganisationRepositoryDataJdbcAdapter implements OrganisationRepository {

    private final OrganisationJdbcRepository organisationJdbcRepository;
    private final CountryJdbcRepository countryJdbcRepository;

    public OrganisationRepositoryDataJdbcAdapter(OrganisationJdbcRepository organisationJdbcRepository,
                                                 CountryJdbcRepository countryJdbcRepository) {
        this.organisationJdbcRepository = organisationJdbcRepository;
        this.countryJdbcRepository = countryJdbcRepository;
    }

    @Override
    @Transactional
    public Organisation save(Organisation organisation) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setOrganisationDboResolver(id -> organisationJdbcRepository.findById(id.value()).orElseThrow());
        dboResolvers.setCountryDboResolver(id -> countryJdbcRepository.findById(id.value()).orElseThrow());
        OrganisationDbo organisationDbo = OrganisationDbo.from(organisation, dboResolvers);
        OrganisationDbo savedOrganisationEntity = organisationJdbcRepository.save(organisationDbo);
        return savedOrganisationEntity.asOrganisation();
    }

    @Override
    @Transactional
    public List<Organisation> findAll() {
        return organisationJdbcRepository.findAll().stream().map(OrganisationDbo::asOrganisation).sorted().toList();
    }

    @Override
    @Transactional
    public Optional<Organisation> findById(OrganisationId organisationId) {
        Optional<OrganisationDbo> organisationEntity = organisationJdbcRepository.findById(organisationId.value());
        return organisationEntity.map(OrganisationDbo::asOrganisation);
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
        return entity.asOrganisation();
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
        //organisationJdbcRepository.findAllById(idSet.stream().map(OrganisationId::value).toList());
        return null;
    }

    @Override
    @Transactional
    public Map<OrganisationId, Organisation> loadOrganisationTree(Set<OrganisationId> idSet) {
        /*@SuppressWarnings({"unchecked"})
        List<Long> resultList =
            entityManager.createNativeQuery(getCteQuery(), Long.class)
                .setParameter("idSet", idSet.stream().map(OrganisationId::value).toList())
                .getResultList();

        return findAllById(resultList.stream().map(OrganisationId::of).collect(Collectors.toSet()), true);
         */
        return new HashMap<>();
    }

    @Override
    public Page<Organisation> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        Page<OrganisationDbo> page = organisationJdbcRepository.findAll(pageable);
        return new PageImpl<>(page.stream().map(OrganisationDbo::asOrganisation).toList(),
            page.getPageable(),
            page.getTotalElements());
    }

    @NonNull
    private static String getCteQuery() {
        return """
               WITH RECURSIVE sub_organisations AS (
               -- Start mit den untergeordneten Organisationen
               SELECT id FROM organisation WHERE id IN (:idSet)
               UNION ALL
               -- Rekursiver Schritt: Finde die übergeordneten Organisationen
               SELECT o.id FROM organisation o
                                INNER JOIN organisation_organisation oo ON o.id = oo.parent_organisation_id
                                INNER JOIN sub_organisations so ON oo.organisation_id = so.id
               )
               SELECT DISTINCT id FROM sub_organisations
               """;
    }
}
