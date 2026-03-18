package de.jobst.resulter.application;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationDto;
import de.jobst.resulter.adapter.driver.web.mapper.OrganisationMapper;
import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.application.port.OrganisationQueryService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrganisationQueryServiceImpl implements OrganisationQueryService {

    private final OrganisationService organisationService;
    private final CountryService countryService;

    public OrganisationQueryServiceImpl(OrganisationService organisationService, CountryService countryService) {
        this.organisationService = organisationService;
        this.countryService = countryService;
    }

    @Override
    public List<OrganisationDto> findAllAsDto() {
        return toDtos(organisationService.findAll());
    }

    @Override
    public Page<OrganisationDto> findAllAsDto(String filter, Pageable pageable) {
        Page<Organisation> page = organisationService.findAll(filter, pageable);
        return new PageImpl<>(toDtos(page.getContent()), page.getPageable(), page.getTotalElements());
    }

    @Override
    public Optional<OrganisationDto> findByIdAsDto(Long id) {
        return organisationService.findById(OrganisationId.of(id))
                .map(org -> toDtos(List.of(org)).getFirst());
    }

    private List<OrganisationDto> toDtos(List<Organisation> organisations) {
        Map<CountryId, Country> countryMap = countryService.batchLoadForOrganisations(organisations);
        Map<OrganisationId, Organisation> orgMap = organisationService.batchLoadChildOrganisations(organisations);
        return OrganisationMapper.toDtos(organisations, countryMap, orgMap);
    }
}
