package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationDto;
import de.jobst.resulter.adapter.driver.web.dto.OrganisationTypeDto;
import de.jobst.resulter.application.OrganisationService;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class OrganisationController {

    private final OrganisationService organisationService;

    @Autowired
    public OrganisationController(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    @GetMapping("/organisation")
    public ResponseEntity<List<OrganisationDto>> handleOrganisations() {
        try {
            List<Organisation> organisations = organisationService.findAll();
            return ResponseEntity.ok(organisations.stream().map(OrganisationDto::from).toList());
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/organisation/{id}")
    public ResponseEntity<OrganisationDto> getOrganisation(
            @PathVariable Long id
    ) {
        try {
            Optional<Organisation> organisation = organisationService.findById(OrganisationId.of(id));
            return organisation.map(value -> ResponseEntity.ok(OrganisationDto.from(value)))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/organisation_types")
    public ResponseEntity<List<OrganisationTypeDto>> handleOrganisationTypes() {
        try {
            List<OrganisationTypeDto>
                    organisationTypes =
                    Arrays.stream(OrganisationType.values()).map(OrganisationTypeDto::from).toList();
            return ResponseEntity.ok(organisationTypes);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/organisation/{id}")
    public ResponseEntity<OrganisationDto> updateOrganisation(@PathVariable Long id,
                                                              @RequestBody OrganisationDto organisationDto) {
        try {
            Organisation organisation = organisationService.updateOrganisation(
                    OrganisationId.of(id),
                    OrganisationName.of(organisationDto.name()),
                    OrganisationShortName.of(organisationDto.shortName()),
                    OrganisationType.fromValue(organisationDto.type().id()),
                    (organisationDto.countryDto() != null) ?
                            Country.of(organisationDto.countryDto().id(),
                                    organisationDto.countryDto().name(),
                                    organisationDto.countryDto().code()) : null,
                    Organisations.of(
                            organisationDto.organisations() == null ? new ArrayList<>() :
                                    organisationDto.organisations().stream()
                                            .map(it -> organisationService.findById(OrganisationId.of(it.id()))
                                                    .orElse(null))
                                            .filter(ObjectUtils::isNotEmpty)
                                            .toList()
                    )
            );
            if (null != organisation) {
                return ResponseEntity.ok(OrganisationDto.from(organisation));
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (DataIntegrityViolationException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (IllegalArgumentException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/organisation")
    public ResponseEntity<OrganisationDto> createOrganisation(@RequestBody OrganisationDto organisationDto) {
        try {
            Organisation organisation = organisationService.createOrganisation(
                    OrganisationName.of(organisationDto.name()),
                    OrganisationShortName.of(organisationDto.shortName()),
                    OrganisationType.fromValue(organisationDto.type().id()),
                    (organisationDto.countryDto() != null) ?
                            Country.of(organisationDto.countryDto().id(),
                                    organisationDto.countryDto().name(),
                                    organisationDto.countryDto().code()) : null,
                    Organisations.of(
                            organisationDto.organisations() == null ? new ArrayList<>() :
                                    organisationDto.organisations().stream()
                                            .map(it -> organisationService.findById(OrganisationId.of(it.id()))
                                                    .orElse(null))
                                            .filter(ObjectUtils::isNotEmpty)
                                            .toList()
                    )
            );
            if (null != organisation) {
                return ResponseEntity.ok(OrganisationDto.from(organisation));
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (DataIntegrityViolationException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (IllegalArgumentException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static void logError(Exception e) {
        log.error(e.getMessage());
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }
}
