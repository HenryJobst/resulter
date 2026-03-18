package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.application.port.OrganisationBatchResult;
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
    public OrganisationBatchResult findAll() {
        List<Organisation> organisations = organisationService.findAll();
        return buildBatchResult(organisations, organisations.size(), Pageable.unpaged());
    }

    @Override
    public OrganisationBatchResult findAll(String filter, Pageable pageable) {
        Page<Organisation> page = organisationService.findAll(filter, pageable);
        return buildBatchResult(page.getContent(), page.getTotalElements(), page.getPageable());
    }

    @Override
    public Optional<OrganisationBatchResult> findById(Long id) {
        return organisationService.findById(OrganisationId.of(id))
                .map(org -> buildBatchResult(List.of(org), 1, Pageable.unpaged()));
    }

    private OrganisationBatchResult buildBatchResult(
            List<Organisation> organisations, long totalElements, Pageable pageable) {
        Map<CountryId, Country> countryMap = countryService.batchLoadForOrganisations(organisations);
        Map<OrganisationId, Organisation> childOrganisationMap =
                organisationService.batchLoadChildOrganisations(organisations);
        return new OrganisationBatchResult(organisations, totalElements, pageable, countryMap, childOrganisationMap);
    }
}
