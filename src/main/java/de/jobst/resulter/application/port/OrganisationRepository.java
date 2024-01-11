package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface OrganisationRepository {
    Organisation save(Organisation organisation);

    List<Organisation> findAll();

    Optional<Organisation> findById(OrganisationId organisationId);

    Organisation findOrCreate(Organisation organisation);

    void deleteOrganisation(Organisation organisation);

    @Transactional(readOnly = true)
    Map<OrganisationId, Organisation> findAllById(Set<OrganisationId> idSet, boolean deep);

    @Transactional
    Map<OrganisationId, Organisation> loadOrganisationTree(Set<OrganisationId> idSet);
}
