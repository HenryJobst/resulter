package de.jobst.resulter.application;

import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.domain.*;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class OrganisationService {

    private final OrganisationRepository organisationRepository;

    public OrganisationService(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
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

    public Optional<Organisation> findById(OrganisationId organisationId) {
        return organisationRepository.findById(organisationId);
    }

    public Organisation updateOrganisation(OrganisationId id,
                                           OrganisationName name,
                                           OrganisationShortName shortName,
                                           OrganisationType type,
                                           CountryId countryId,
                                           Collection<OrganisationId> parentOrganisationIds) {

        Optional<Organisation> optionalOrganisation = findById(id);
        if (optionalOrganisation.isEmpty()) {
            return null;
        }
        Organisation organisation = optionalOrganisation.get();
        organisation.update(name, shortName, type, countryId, parentOrganisationIds);
        return organisationRepository.save(organisation);
    }

    public Organisation createOrganisation(OrganisationName name,
                                           OrganisationShortName shortName,
                                           OrganisationType type,
                                           CountryId countryId,
                                           Collection<OrganisationId> parentOrganisationIds) {

        Organisation organisation = Organisation.of(name, shortName, type, countryId, parentOrganisationIds);
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

}
