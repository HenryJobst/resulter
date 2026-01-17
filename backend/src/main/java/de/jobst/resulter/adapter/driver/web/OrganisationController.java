package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationDto;
import de.jobst.resulter.adapter.driver.web.dto.OrganisationTypeDto;
import de.jobst.resulter.adapter.driver.web.mapper.OrganisationMapper;
import de.jobst.resulter.adapter.driver.web.mapper.OrganisationTypeMapper;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;

@RestController
@RequestMapping("/organisation")
@Slf4j
public class OrganisationController {

    private final OrganisationService organisationService;
    private final CountryService countryService;
    private final OrganisationMapper organisationMapper;

    public OrganisationController(
            OrganisationService organisationService,
            CountryService countryService,
            OrganisationMapper organisationMapper) {
        this.organisationService = organisationService;
        this.countryService = countryService;
        this.organisationMapper = organisationMapper;
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrganisationDto>> getAllOrganisations() {
        List<Organisation> organisations = organisationService.findAll();
        Map<CountryId, Country> countryMap = countryService.batchLoadForOrganisations(organisations);
        Map<OrganisationId, Organisation> orgMap = organisationService.batchLoadChildOrganisations(organisations);

        return ResponseEntity.ok(organisations.stream()
                .map(o -> organisationMapper.toDto(o, countryMap, orgMap))
                .toList());
    }

    @GetMapping("")
    public ResponseEntity<Page<OrganisationDto>> searchOrganisations(
            @RequestParam Optional<String> filter, @Nullable Pageable pageable) {
        Page<Organisation> organisations = organisationService.findAll(
                filter.orElse(null),
                pageable != null
                        ? FilterAndSortConverter.mapOrderProperties(pageable, OrganisationDto::mapOrdersDtoToDomain)
                        : Pageable.unpaged());

        Map<CountryId, Country> countryMap = countryService.batchLoadForOrganisations(organisations.getContent());
        Map<OrganisationId, Organisation> orgMap = organisationService.batchLoadChildOrganisations(organisations.getContent());

        return ResponseEntity.ok(new PageImpl<>(
                organisations.getContent().stream()
                        .map(o -> organisationMapper.toDto(o, countryMap, orgMap))
                        .toList(),
                FilterAndSortConverter.mapOrderProperties(
                        organisations.getPageable(), OrganisationDto::mapOrdersDomainToDto),
                organisations.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganisationDto> getOrganisation(@PathVariable Long id) {
        Optional<Organisation> organisation = organisationService.findById(OrganisationId.of(id));
        return organisation
                .map(value -> ResponseEntity.ok(organisationMapper.toDto(value)))
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
        return ResponseEntity.ok(organisationMapper.toDto(organisation));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
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
            return ResponseEntity.ok(organisationMapper.toDto(organisation));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteOrganisation(@PathVariable Long id) {
        boolean success = organisationService.deleteOrganisation(OrganisationId.of(id));
        if (success) {
            return ResponseEntity.ok(Boolean.TRUE);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
