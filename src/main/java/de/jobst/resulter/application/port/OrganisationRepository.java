package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;

import java.util.*;

public interface OrganisationRepository {

    Organisation save(Organisation organisation);

    List<Organisation> findAll();

    Optional<Organisation> findById(OrganisationId organisationId);

    Organisation findOrCreate(Organisation organisation);

    Collection<Organisation> findOrCreate(Collection<Organisation> organisations);

    void deleteOrganisation(Organisation organisation);

    Map<OrganisationId, Organisation> findAllById(Set<OrganisationId> idSet);

    Map<OrganisationId, Organisation> loadOrganisationTree(Set<OrganisationId> idSet);
}
