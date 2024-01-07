package de.jobst.resulter.application;

import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.domain.*;
import org.springframework.stereotype.Service;

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

    public Optional<Organisation> findById(OrganisationId organisationId) {
        return organisationRepository.findById(organisationId);
    }

    public Organisation updateOrganisation(OrganisationId id, OrganisationName name,
                                           OrganisationShortName shortName,
                                           OrganisationType type,
                                           Country country,
                                           Organisations organisations) {

        Optional<Organisation> optionalOrganisation = findById(id);
        if (optionalOrganisation.isEmpty()) {
            return null;
        }
        Organisation organisation = optionalOrganisation.get();
        organisation.update(name, shortName, type, country, organisations);
        return organisationRepository.save(organisation);
    }
}
