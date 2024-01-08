package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import org.hibernate.Hibernate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class OrganisationRepositoryDataJpaAdapter implements OrganisationRepository {

    private final OrganisationJpaRepository organisationJpaRepository;
    private final CountryJpaRepository countryJpaRepository;

    public OrganisationRepositoryDataJpaAdapter(OrganisationJpaRepository organisationJpaRepository,
                                                CountryJpaRepository countryJpaRepository) {
        this.organisationJpaRepository = organisationJpaRepository;
        this.countryJpaRepository = countryJpaRepository;
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
            organisationDbo.setCountry(countryJpaRepository.save(organisationDbo.getCountry()));
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
}
