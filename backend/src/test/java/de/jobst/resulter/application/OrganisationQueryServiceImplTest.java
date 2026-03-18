package de.jobst.resulter.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.application.port.OrganisationBatchResult;
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
    void findAll_shouldReturnBatchResultWithCorrectData() {
        CountryId countryId = CountryId.of(5L);
        OrganisationId orgId = OrganisationId.of(1L);
        Organisation org = Organisation.of(orgId.value(), "OC Zürich", "OCZ");
        Country country = Country.of(countryId.value(), "SUI", "Schweiz");

        when(organisationService.findAll()).thenReturn(List.of(org));
        when(countryService.batchLoadForOrganisations(List.of(org))).thenReturn(Map.of(countryId, country));
        when(organisationService.batchLoadChildOrganisations(List.of(org))).thenReturn(Map.of());

        OrganisationBatchResult result = queryService.findAll();

        assertThat(result.organisations()).hasSize(1);
        assertThat(result.organisations().getFirst().getId()).isEqualTo(orgId);
        assertThat(result.countryMap()).containsKey(countryId);
        assertThat(result.childOrganisationMap()).isEmpty();
    }

    @Test
    void findAll_shouldReturnEmptyBatchResult() {
        when(organisationService.findAll()).thenReturn(List.of());
        when(countryService.batchLoadForOrganisations(List.of())).thenReturn(Map.of());
        when(organisationService.batchLoadChildOrganisations(List.of())).thenReturn(Map.of());

        OrganisationBatchResult result = queryService.findAll();

        assertThat(result.organisations()).isEmpty();
    }

    @Test
    void findById_shouldReturnBatchResultWithSingleOrganisation() {
        OrganisationId orgId = OrganisationId.of(2L);
        Organisation org = Organisation.of(orgId.value(), "SC Bern", "SCB");

        when(organisationService.findById(orgId)).thenReturn(Optional.of(org));
        when(countryService.batchLoadForOrganisations(List.of(org))).thenReturn(Map.of());
        when(organisationService.batchLoadChildOrganisations(List.of(org))).thenReturn(Map.of());

        Optional<OrganisationBatchResult> result = queryService.findById(orgId.value());

        assertThat(result).isPresent();
        assertThat(result.get().organisations()).hasSize(1);
        assertThat(result.get().organisations().getFirst().getId()).isEqualTo(orgId);
    }

    @Test
    void findById_shouldReturnEmptyForUnknownId() {
        when(organisationService.findById(OrganisationId.of(999L))).thenReturn(Optional.empty());

        Optional<OrganisationBatchResult> result = queryService.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_pageable_shouldReturnBatchResultWithPageMetadata() {
        OrganisationId orgId = OrganisationId.of(3L);
        Organisation org = Organisation.of(orgId.value(), "LV Basel", "LVB");
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Organisation> page = new PageImpl<>(List.of(org), pageable, 1L);

        when(organisationService.findAll(null, pageable)).thenReturn(page);
        when(countryService.batchLoadForOrganisations(List.of(org))).thenReturn(Map.of());
        when(organisationService.batchLoadChildOrganisations(List.of(org))).thenReturn(Map.of());

        OrganisationBatchResult result = queryService.findAll(null, pageable);

        assertThat(result.organisations()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.organisations().getFirst().getId()).isEqualTo(orgId);
    }
}
