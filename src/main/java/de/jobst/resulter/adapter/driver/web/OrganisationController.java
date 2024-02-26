package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationDto;
import de.jobst.resulter.adapter.driver.web.dto.OrganisationTypeDto;
import de.jobst.resulter.application.OrganisationService;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class OrganisationController {

    private final OrganisationService organisationService;

    @Autowired
    public OrganisationController(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    private static void logError(Exception e) {
        log.error(e.getMessage());
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }

    @GetMapping("/organisation")
    public ResponseEntity<Page<OrganisationDto>> searchOrganisations(@RequestParam Optional<String> filter,
                                                                     Pageable pageable) {
        try {
            Page<Organisation> organisations = organisationService.findAll(filter.orElse(null),
                pageable != null ?
                FilterAndSortConverter.mapOrderProperties(pageable, OrganisationDto::mapOrdersDtoToDomain) :
                Pageable.unpaged());
            return ResponseEntity.ok(new PageImpl<>(organisations.getContent()
                .stream()
                .map(OrganisationDto::from)
                .toList(),
                FilterAndSortConverter.mapOrderProperties(organisations.getPageable(),
                    OrganisationDto::mapOrdersDomainToDto),
                organisations.getTotalElements()));
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/organisation/{id}")
    public ResponseEntity<OrganisationDto> getOrganisation(@PathVariable Long id) {
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
            List<OrganisationTypeDto> organisationTypes =
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
            Organisation organisation = organisationService.updateOrganisation(OrganisationId.of(id),
                OrganisationName.of(organisationDto.name()),
                OrganisationShortName.of(organisationDto.shortName()),
                OrganisationType.fromValue(organisationDto.type().id()),
                (organisationDto.countryId() != null) ? CountryId.of(organisationDto.countryId()) : null,
                organisationDto.organisationIds() == null ?
                new ArrayList<>() :
                organisationDto.organisationIds().stream().map(OrganisationId::of).toList());
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
            Organisation organisation =
                organisationService.createOrganisation(OrganisationName.of(organisationDto.name()),
                    OrganisationShortName.of(organisationDto.shortName()),
                    OrganisationType.fromValue(organisationDto.type().id()),
                    (organisationDto.countryId() != null) ? CountryId.of(organisationDto.countryId()) : null,
                    organisationDto.organisationIds() == null ?
                    new ArrayList<>() :
                    organisationDto.organisationIds().stream().map(OrganisationId::of).toList());
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

    @DeleteMapping("/organisation/{id}")
    public ResponseEntity<Boolean> deleteOrganisation(@PathVariable Long id) {
        try {
            boolean success = organisationService.deleteOrganisation(OrganisationId.of(id));
            if (success) {
                return ResponseEntity.ok(Boolean.TRUE);
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
}
