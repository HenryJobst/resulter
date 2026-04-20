package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationDto;
import de.jobst.resulter.adapter.driver.web.dto.OrganisationTypeDto;
import de.jobst.resulter.adapter.driver.web.mapper.OrganisationMapper;
import de.jobst.resulter.adapter.driver.web.mapper.OrganisationTypeMapper;
import de.jobst.resulter.application.port.OrganisationBatchResult;
import de.jobst.resulter.application.port.OrganisationQueryService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organisation")
@Slf4j
public class OrganisationController {

    private final OrganisationService organisationService;
    private final OrganisationQueryService organisationQueryService;

    public OrganisationController(
            OrganisationService organisationService, OrganisationQueryService organisationQueryService) {
        this.organisationService = organisationService;
        this.organisationQueryService = organisationQueryService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrganisationDto>> getAllOrganisations() {
        OrganisationBatchResult result = organisationQueryService.findAll();
        return ResponseEntity.ok(OrganisationMapper.toDtos(
                result.organisations(), result.countryMap(), result.childOrganisationMap()));
    }

    @GetMapping("")
    public ResponseEntity<Page<OrganisationDto>> searchOrganisations(
            @RequestParam Optional<String> filter, @Nullable Pageable pageable) {
        Pageable mappedPageable = pageable != null
                ? FilterAndSortConverter.mapOrderProperties(pageable, OrganisationDto::mapOrdersDtoToDomain)
                : Pageable.unpaged();
        OrganisationBatchResult result = organisationQueryService.findAll(filter.orElse(null), mappedPageable);
        List<OrganisationDto> dtos = OrganisationMapper.toDtos(
                result.organisations(), result.countryMap(), result.childOrganisationMap());
        return ResponseEntity.ok(new PageImpl<>(
                dtos,
                FilterAndSortConverter.mapOrderProperties(
                        result.resolvedPageable(), OrganisationDto::mapOrdersDomainToDto),
                result.totalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganisationDto> getOrganisation(@PathVariable Long id) {
        return organisationQueryService
                .findById(id)
                .map(result -> OrganisationMapper.toDtos(
                                result.organisations(), result.countryMap(), result.childOrganisationMap())
                        .getFirst())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/types")
    public ResponseEntity<List<OrganisationTypeDto>> handleOrganisationTypes() {
        List<OrganisationTypeDto> organisationTypes = Arrays.stream(OrganisationType.values())
                .map(OrganisationTypeMapper::toDto)
                .toList();
        return ResponseEntity.ok(organisationTypes);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<OrganisationDto> updateOrganisation(
            @PathVariable Long id, @RequestBody OrganisationDto organisationDto) {
        Organisation organisation = organisationService.updateOrganisation(
                OrganisationId.of(id),
                OrganisationName.of(organisationDto.name()),
                OrganisationShortName.of(organisationDto.shortName()),
                OrganisationType.fromValue(organisationDto.type().id()),
                (organisationDto.country() != null)
                        ? CountryId.of(organisationDto.country().id())
                        : null,
                organisationDto.childOrganisations() == null
                        ? new ArrayList<>()
                        : organisationDto.childOrganisations().stream()
                                .map(x -> OrganisationId.of(x.id()))
                                .toList());
        return organisationQueryService
                .findById(organisation.getId().value())
                .map(result -> OrganisationMapper.toDtos(
                                result.organisations(), result.countryMap(), result.childOrganisationMap())
                        .getFirst())
                .map(ResponseEntity::ok)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<OrganisationDto> createOrganisation(@RequestBody OrganisationDto organisationDto) {
        Organisation organisation = organisationService.createOrganisation(
                OrganisationName.of(organisationDto.name()),
                OrganisationShortName.of(organisationDto.shortName()),
                OrganisationType.fromValue(organisationDto.type().id()),
                (organisationDto.country() != null)
                        ? CountryId.of(organisationDto.country().id())
                        : null,
                organisationDto.childOrganisations() == null
                        ? new ArrayList<>()
                        : organisationDto.childOrganisations().stream()
                                .map(x -> OrganisationId.of(x.id()))
                                .toList());
        if (null != organisation) {
            return organisationQueryService
                    .findById(organisation.getId().value())
                    .map(result -> OrganisationMapper.toDtos(
                                    result.organisations(), result.countryMap(), result.childOrganisationMap())
                            .getFirst())
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganisation(@PathVariable Long id) {
        boolean success = organisationService.deleteOrganisation(OrganisationId.of(id));
        if (success) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
