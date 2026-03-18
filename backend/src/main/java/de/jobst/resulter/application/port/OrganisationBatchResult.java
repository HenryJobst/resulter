package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

public record OrganisationBatchResult(
        List<Organisation> organisations,
        long totalElements,
        Pageable resolvedPageable,
        Map<CountryId, Country> countryMap,
        Map<OrganisationId, Organisation> childOrganisationMap) {}
