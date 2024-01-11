package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Hibernate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class OrganisationRepositoryDataJpaAdapter implements OrganisationRepository {

    private final OrganisationJpaRepository organisationJpaRepository;
    private final CountryJpaRepository countryJpaRepository;
    private final EntityManager entityManager;

    public OrganisationRepositoryDataJpaAdapter(OrganisationJpaRepository organisationJpaRepository,
                                                CountryJpaRepository countryJpaRepository,
                                                EntityManager entityManager) {
        this.organisationJpaRepository = organisationJpaRepository;
        this.countryJpaRepository = countryJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Organisation save(Organisation organisation) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setOrganisationDboResolver(
                id -> organisationJpaRepository.findById(id.value()).orElseThrow());
        dboResolvers.setCountryDboResolver(
                id -> countryJpaRepository.findById(id.value()).orElseThrow());
        OrganisationDbo organisationDbo = OrganisationDbo.from(organisation, null, dboResolvers);
        if (organisationDbo.getCountry() != null &&
                Hibernate.isInitialized(organisationDbo.getCountry())) {
            organisationDbo.setCountry(
                    organisationDbo.getCountry().getId() != CountryId.empty().value() ? organisationDbo.getCountry() :
                            countryJpaRepository.save(organisationDbo.getCountry())
            );
        }
        if (Hibernate.isInitialized(organisationDbo.getParentOrganisations())) {
            var organisationsToSave = organisationDbo.getParentOrganisations()
                    .stream()
                    .filter(it -> it != null && Hibernate.isInitialized(it))
                    .toList();
            organisationJpaRepository.saveAll(organisationsToSave);
        }
        OrganisationDbo savedOrganisationEntity = organisationJpaRepository.save(organisationDbo);
        return savedOrganisationEntity.asOrganisation();
    }

    @Override
    @Transactional
    public List<Organisation> findAll() {
        return organisationJpaRepository.findAll().stream()
                .map(OrganisationDbo::asOrganisation)
                .sorted()
                .toList();
    }

    @Override
    @Transactional
    public Optional<Organisation> findById(OrganisationId organisationId) {
        Optional<OrganisationDbo> organisationEntity =
                organisationJpaRepository.findById(organisationId.value());
        return organisationEntity.map(OrganisationDbo::asOrganisation);
    }

    @Override
    @Transactional
    public Organisation findOrCreate(Organisation organisation) {
        Optional<OrganisationDbo> organisationEntity =
                organisationJpaRepository.findByName(organisation.getName().value());
        if (organisationEntity.isEmpty()) {
            return save(organisation);
        }
        OrganisationDbo entity = organisationEntity.get();
        return entity.asOrganisation();
    }

    @Override
    public void deleteOrganisation(Organisation organisation) {
        organisationJpaRepository.deleteById(organisation.getId().value());
    }

    private EntityGraph<OrganisationDbo> getEntityGraph(boolean deep) {
        EntityGraph<OrganisationDbo> entityGraph = entityManager.createEntityGraph(OrganisationDbo.class);
        if (deep) {
            entityGraph.addSubgraph(OrganisationDbo_.parentOrganisations);
        }
        return entityGraph;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<OrganisationId, Organisation> findAllById(Set<OrganisationId> idSet, boolean deep) {
        EntityGraph<?> entityGraph = getEntityGraph(deep);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrganisationDbo> query = cb.createQuery(OrganisationDbo.class);

        Root<OrganisationDbo> root = query.from(OrganisationDbo.class);

        Predicate
                idPredicate =
                root.get("id").in(idSet.stream().map(OrganisationId::value).collect(Collectors.toSet()));
        query.select(root).where(idPredicate);

        TypedQuery<OrganisationDbo> typedQuery = entityManager.createQuery(query);
        typedQuery.setHint("jakarta.persistence.loadgraph", entityGraph);

        List<OrganisationDbo> resultList = typedQuery.getResultList();
        return resultList.stream()
                .map(OrganisationDbo::asOrganisation)
                .collect(Collectors.toMap(Organisation::getId, it -> it));
    }

    @Override
    @Transactional
    public Map<OrganisationId, Organisation> loadOrganisationTree(Set<OrganisationId> idSet) {

        @SuppressWarnings({"unchecked"})
        List<Long> resultList = entityManager.createNativeQuery(getCteQuery(), Long.class)
                .setParameter("idSet", idSet.stream().map(OrganisationId::value).toList())
                .getResultList();

        return findAllById(resultList.stream().map(OrganisationId::of).collect(Collectors.toSet()), true);
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
