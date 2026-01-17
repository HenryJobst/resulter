package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.*;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@PrimaryPort
public interface OrganisationService {

    List<Organisation> findAll();

    Organisation findOrCreate(Organisation organisation);

    Collection<Organisation> findOrCreate(Collection<Organisation> organisations);

    Organisation getById(OrganisationId id);

    Optional<Organisation> findById(OrganisationId organisationId);

    @NonNull
    List<Organisation> findByIds(Collection<OrganisationId> childOrganisations);

    @NonNull
    Organisation updateOrganisation(
            @NonNull OrganisationId id,
            @NonNull OrganisationName name,
            @NonNull OrganisationShortName shortName,
            @NonNull OrganisationType type,
            @Nullable CountryId countryId,
            @NonNull Collection<OrganisationId> childOrganisationIds);

    Organisation createOrganisation(
            OrganisationName name,
            OrganisationShortName shortName,
            OrganisationType type,
            CountryId countryId,
            Collection<OrganisationId> childOrganisationIds);

    boolean deleteOrganisation(OrganisationId id);

    Page<Organisation> findAll(@Nullable String filter, @NonNull Pageable pageable);

    List<Organisation> findAllById(Set<OrganisationId> organisationIds);

    Map<OrganisationId, Organisation> findAllByIdAsMap(Set<OrganisationId> organisationIds);

    Map<OrganisationId, Organisation> batchLoadChildOrganisations(List<Organisation> organisations);
}
