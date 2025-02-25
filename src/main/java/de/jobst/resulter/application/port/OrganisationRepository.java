package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import java.util.*;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Repository
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
