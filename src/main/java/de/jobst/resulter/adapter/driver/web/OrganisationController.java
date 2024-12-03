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
import org.springframework.data.web.PageableDefault;
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
                                                                     @PageableDefault(page = 0, size = 5000)
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
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/organisation/{id}")
    public ResponseEntity<OrganisationDto> getOrganisation(@PathVariable Long id) {
        try {
            Optional<Organisation> organisation = organisationService.findById(OrganisationId.of(id));
            return organisation.map(value -> ResponseEntity.ok(OrganisationDto.from(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
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
            return ResponseEntity.internalServerError().build();
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
                (organisationDto.country() != null) ? CountryId.of(organisationDto.country().id()) : null,
                organisationDto.childOrganisations() == null ?
                new ArrayList<>() :
                organisationDto.childOrganisations().stream().map(x -> OrganisationId.of(x.id())).toList());
            if (null != organisation) {
                return ResponseEntity.ok(OrganisationDto.from(organisation));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (DataIntegrityViolationException e) {
            logError(e);
            return ResponseEntity.status(HttpStatus.CONFLICT.value()).build();
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/organisation")
    public ResponseEntity<OrganisationDto> createOrganisation(@RequestBody OrganisationDto organisationDto) {
        try {
            Organisation organisation =
                organisationService.createOrganisation(OrganisationName.of(organisationDto.name()),
                    OrganisationShortName.of(organisationDto.shortName()),
                    OrganisationType.fromValue(organisationDto.type().id()),
                    (organisationDto.country() != null) ? CountryId.of(organisationDto.country().id()) : null,
                    organisationDto.childOrganisations() == null ?
                    new ArrayList<>() :
                    organisationDto.childOrganisations().stream().map(x -> OrganisationId.of(x.id())).toList());
            if (null != organisation) {
                return ResponseEntity.ok(OrganisationDto.from(organisation));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (DataIntegrityViolationException e) {
            logError(e);
            return ResponseEntity.status(HttpStatus.CONFLICT.value()).build();
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/organisation/{id}")
    public ResponseEntity<Boolean> deleteOrganisation(@PathVariable Long id) {
        try {
            boolean success = organisationService.deleteOrganisation(OrganisationId.of(id));
            if (success) {
                return ResponseEntity.ok(Boolean.TRUE);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (DataIntegrityViolationException e) {
            logError(e);
            return ResponseEntity.status(HttpStatus.CONFLICT.value()).build();
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
