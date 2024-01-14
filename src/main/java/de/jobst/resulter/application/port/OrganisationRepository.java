package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public interface OrganisationRepository {

    Organisation save(Organisation organisation);

    List<Organisation> findAll();

    Optional<Organisation> findById(OrganisationId organisationId);

    Organisation findOrCreate(Organisation organisation);

    @Transactional
    Collection<Organisation> findOrCreate(Collection<Organisation> organisations);

    void deleteOrganisation(Organisation organisation);

    @Transactional(readOnly = true)
    Map<OrganisationId, Organisation> findAllById(Set<OrganisationId> idSet);

    @Transactional
    Map<OrganisationId, Organisation> loadOrganisationTree(Set<OrganisationId> idSet);
}
