package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CountryRepository;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final CountryRepository countryRepository;

    public OrganisationService(OrganisationRepository organisationRepository, CountryRepository countryRepository) {
        this.organisationRepository = organisationRepository;
        this.countryRepository = countryRepository;
    }

    public List<Organisation> findAll() {
        return organisationRepository.findAll();
    }

    public Organisation findOrCreate(Organisation organisation) {
        return organisationRepository.findOrCreate(organisation);
    }

    public Collection<Organisation> findOrCreate(Collection<Organisation> organisations) {
        return organisationRepository.findOrCreate(organisations);
    }

    public Organisation getById(OrganisationId id) {
        return organisationRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public Optional<Organisation> findById(OrganisationId organisationId) {
        return organisationRepository.findById(organisationId);
    }

    @NonNull
    List<Organisation> findByIds(Collection<OrganisationId> childOrganisations) {
        return organisationRepository.findByIds(childOrganisations);
    }

    public @NonNull Organisation updateOrganisation(
            @NonNull OrganisationId id,
            @NonNull OrganisationName name,
            @NonNull OrganisationShortName shortName,
            @NonNull OrganisationType type,
            @Nullable CountryId countryId,
            @NonNull Collection<OrganisationId> childOrganisationIds) {

        Optional<Country> optionalCountry = Optional.ofNullable(countryId).flatMap(countryRepository::findById);
        List<Organisation> childOrganisations = findByIds(childOrganisationIds);
        return organisationRepository.save(new Organisation(
                findById(id).orElseThrow(ResourceNotFoundException::new).getId(),
                name,
                shortName,
                type,
                optionalCountry.map(Country::getId).orElse(null),
                childOrganisations));
    }

    public Organisation createOrganisation(
            OrganisationName name,
            OrganisationShortName shortName,
            OrganisationType type,
            CountryId countryId,
            Collection<OrganisationId> childOrganisationIds) {

        Optional<Country> optionalCountry = countryRepository.findById(countryId);
        if (optionalCountry.isEmpty()) {
            return null;
        }
        List<Organisation> childOrganisations = findByIds(childOrganisationIds);
        Organisation organisation = Organisation.of(name, shortName, type,
            optionalCountry.map(Country::getId).orElse(null), childOrganisations);
        return organisationRepository.save(organisation);
    }

    public boolean deleteOrganisation(OrganisationId id) {
        Optional<Organisation> optionalOrganisation = findById(id);
        if (optionalOrganisation.isEmpty()) {
            return false;
        }
        Organisation organisation = optionalOrganisation.get();
        organisationRepository.deleteOrganisation(organisation);
        return true;
    }

    public Page<Organisation> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        return organisationRepository.findAll(filter, pageable);
    }

    public List<Organisation> findAllById(Set<OrganisationId> organisationIds) {
        return organisationRepository.findByIds(organisationIds);
    }
}
