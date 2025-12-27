package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

@Repository
@SecondaryPort
public interface OrganisationRepository {

    Organisation save(Organisation organisation);

    List<Organisation> findAll();

    Optional<Organisation> findById(OrganisationId organisationId);

    Organisation findOrCreate(Organisation organisation);

    Collection<Organisation> findOrCreate(Collection<Organisation> organisations);

    void deleteOrganisation(Organisation organisation);

    Map<OrganisationId, Organisation> findAllById(Set<OrganisationId> idSet);

    Map<OrganisationId, Organisation> loadOrganisationTree(Set<OrganisationId> idSet);

    Page<Organisation> findAll(@Nullable String filter, @NonNull Pageable pageable);

    @NonNull
    List<Organisation> findByIds(Collection<OrganisationId> childOrganisations);
}
