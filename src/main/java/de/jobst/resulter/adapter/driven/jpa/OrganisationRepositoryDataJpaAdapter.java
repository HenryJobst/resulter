package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class OrganisationRepositoryDataJpaAdapter implements OrganisationRepository {

    private final OrganisationJpaRepository organisationJpaRepository;

    public OrganisationRepositoryDataJpaAdapter(OrganisationJpaRepository organisationJpaRepository) {
        this.organisationJpaRepository = organisationJpaRepository;
    }

    @Override
    @Transactional
    public Organisation save(Organisation organisation) {
        OrganisationDbo organisationEntity = OrganisationDbo.from(organisation);
        OrganisationDbo savedOrganisationEntity = organisationJpaRepository.save(organisationEntity);
        return savedOrganisationEntity.asOrganisation();
    }

    @Override
    @Transactional
    public List<Organisation> findAll() {
        return organisationJpaRepository.findAll().stream()
                .map(OrganisationDbo::asOrganisation)
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
            organisationEntity = Optional.of(OrganisationDbo.from(save(organisation)));
        }
        OrganisationDbo entity = organisationEntity.get();
        return entity.asOrganisation();
    }
}
