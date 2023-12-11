package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;

import java.util.List;
import java.util.Optional;

public interface OrganisationRepository {
    Organisation save(Organisation organisation);

    List<Organisation> findAll();

    Optional<Organisation> findById(OrganisationId OrganisationId);

    Organisation findOrCreate(Organisation organisation);
}
