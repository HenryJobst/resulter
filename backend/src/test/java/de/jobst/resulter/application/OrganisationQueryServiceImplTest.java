package de.jobst.resulter.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationDto;
import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class OrganisationQueryServiceImplTest {

    private OrganisationService organisationService;
    private CountryService countryService;
    private OrganisationQueryServiceImpl queryService;

    @BeforeEach
    void setUp() {
        organisationService = mock(OrganisationService.class);
        countryService = mock(CountryService.class);
        queryService = new OrganisationQueryServiceImpl(organisationService, countryService);
    }

    @Test
    void findAllAsDto_shouldReturnDtosWithCorrectFields() {
        CountryId countryId = CountryId.of(5L);
        OrganisationId orgId = OrganisationId.of(1L);
        Organisation org = Organisation.of(orgId.value(), "OC Zürich", "OCZ");
        Country country = Country.of(countryId.value(), "SUI", "Schweiz");

        when(organisationService.findAll()).thenReturn(List.of(org));
        when(countryService.batchLoadForOrganisations(List.of(org))).thenReturn(Map.of(countryId, country));
        when(organisationService.batchLoadChildOrganisations(List.of(org))).thenReturn(Map.of());

        List<OrganisationDto> dtos = queryService.findAllAsDto();

        assertThat(dtos).hasSize(1);
        OrganisationDto dto = dtos.getFirst();
        assertThat(dto.id()).isEqualTo(orgId.value());
        assertThat(dto.name()).isEqualTo("OC Zürich");
        assertThat(dto.shortName()).isEqualTo("OCZ");
        assertThat(dto.childOrganisations()).isEmpty();
    }

    @Test
    void findAllAsDto_shouldReturnEmptyList() {
        when(organisationService.findAll()).thenReturn(List.of());
        when(countryService.batchLoadForOrganisations(List.of())).thenReturn(Map.of());
        when(organisationService.batchLoadChildOrganisations(List.of())).thenReturn(Map.of());

        List<OrganisationDto> dtos = queryService.findAllAsDto();

        assertThat(dtos).isEmpty();
    }

    @Test
    void findByIdAsDto_shouldReturnPopulatedDto() {
        OrganisationId orgId = OrganisationId.of(2L);
        Organisation org = Organisation.of(orgId.value(), "SC Bern", "SCB");

        when(organisationService.findById(orgId)).thenReturn(Optional.of(org));
        when(countryService.batchLoadForOrganisations(List.of(org))).thenReturn(Map.of());
        when(organisationService.batchLoadChildOrganisations(List.of(org))).thenReturn(Map.of());

        Optional<OrganisationDto> dto = queryService.findByIdAsDto(orgId.value());

        assertThat(dto).isPresent();
        assertThat(dto.get().id()).isEqualTo(orgId.value());
        assertThat(dto.get().name()).isEqualTo("SC Bern");
    }

    @Test
    void findByIdAsDto_shouldReturnEmptyForUnknownId() {
        when(organisationService.findById(OrganisationId.of(999L))).thenReturn(Optional.empty());

        Optional<OrganisationDto> dto = queryService.findByIdAsDto(999L);

        assertThat(dto).isEmpty();
    }

    @Test
    void findAllAsDto_pageable_shouldReturnPagedDtos() {
        OrganisationId orgId = OrganisationId.of(3L);
        Organisation org = Organisation.of(orgId.value(), "LV Basel", "LVB");
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Organisation> page = new PageImpl<>(List.of(org), pageable, 1L);

        when(organisationService.findAll(null, pageable)).thenReturn(page);
        when(countryService.batchLoadForOrganisations(List.of(org))).thenReturn(Map.of());
        when(organisationService.batchLoadChildOrganisations(List.of(org))).thenReturn(Map.of());

        Page<OrganisationDto> result = queryService.findAllAsDto(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent().getFirst().name()).isEqualTo("LV Basel");
    }
}
