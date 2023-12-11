package de.jobst.resulter.application;

import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrganisationService {

    private final OrganisationRepository organisationRepository;

    public OrganisationService(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    @Transactional
    public Organisation findOrCreate(Organisation organisation) {
        return organisationRepository.findOrCreate(organisation);
    }

    Optional<Organisation> findById(OrganisationId organisationId) {
        return organisationRepository.findById(organisationId);
    }
}
