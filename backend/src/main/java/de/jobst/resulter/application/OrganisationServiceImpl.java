package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CountryRepository;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class OrganisationServiceImpl implements OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final CountryRepository countryRepository;

    public OrganisationServiceImpl(OrganisationRepository organisationRepository, CountryRepository countryRepository) {
        this.organisationRepository = organisationRepository;
        this.countryRepository = countryRepository;
    }

    @Override
    public List<Organisation> findAll() {
        return organisationRepository.findAll();
    }

    @Override
    public Organisation findOrCreate(Organisation organisation) {
        return organisationRepository.findOrCreate(organisation);
    }

    @Override
    public Collection<Organisation> findOrCreate(Collection<Organisation> organisations) {
        return organisationRepository.findOrCreate(organisations);
    }

    @Override
    public Organisation getById(OrganisationId id) {
        return organisationRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Optional<Organisation> findById(OrganisationId organisationId) {
        return organisationRepository.findById(organisationId);
    }

    @Override
    public List<Organisation> findByIds(Collection<OrganisationId> childOrganisations) {
        return organisationRepository.findByIds(childOrganisations);
    }

    @Override
    public Organisation updateOrganisation(
        OrganisationId id,
        OrganisationName name,
        OrganisationShortName shortName,
        OrganisationType type,
        @org.jspecify.annotations.Nullable CountryId countryId,
        Collection<OrganisationId> childOrganisationIds) {

        Optional<Country> optionalCountry = Optional.ofNullable(countryId).flatMap(countryRepository::findById);
        List<Organisation> childOrganisations = findByIds(childOrganisationIds);
        return organisationRepository.save(new Organisation(
                findById(id).orElseThrow(ResourceNotFoundException::new).getId(),
                name,
                shortName,
                type,
                optionalCountry.map(Country::getId).orElse(null),
                childOrganisations.stream().map(Organisation::getId).toList()));
    }

    @Override
    public @org.jspecify.annotations.Nullable Organisation createOrganisation(
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
        Organisation organisation = Organisation.of(
                name,
                shortName,
                type,
                optionalCountry.map(Country::getId).orElse(null),
                childOrganisations.stream().map(Organisation::getId).toList());
        return organisationRepository.save(organisation);
    }

    @Override
    public boolean deleteOrganisation(OrganisationId id) {
        Optional<Organisation> optionalOrganisation = findById(id);
        if (optionalOrganisation.isEmpty()) {
            return false;
        }
        Organisation organisation = optionalOrganisation.get();
        organisationRepository.deleteOrganisation(organisation);
        return true;
    }

    @Override
    public Page<Organisation> findAll(@Nullable String filter, Pageable pageable) {
        return organisationRepository.findAll(filter, pageable);
    }

    @Override
    public List<Organisation> findAllById(Set<OrganisationId> organisationIds) {
        return organisationRepository.findByIds(organisationIds);
    }
}
