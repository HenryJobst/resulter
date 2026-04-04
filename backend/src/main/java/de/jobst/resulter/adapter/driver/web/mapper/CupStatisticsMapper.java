package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.CupStatisticsDto;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.aggregations.CupStatistics;
import java.util.Map;

public class CupStatisticsMapper {

    public static CupStatisticsDto toDto(
            CupStatistics cupStatistics, Map<CountryId, Country> countryMap, Map<OrganisationId, Organisation> orgMap) {
        return new CupStatisticsDto(
                CupOverallStatisticsMapper.toDto(cupStatistics.overallStatistics()),
                OrganisationStatisticsMapper.toDtos(cupStatistics.organisationStatistics(), countryMap, orgMap));
    }
}
