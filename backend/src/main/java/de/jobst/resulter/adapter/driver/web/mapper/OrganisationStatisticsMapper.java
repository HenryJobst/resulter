package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationStatisticsDto;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.aggregations.OrganisationStatistics;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrganisationStatisticsMapper {

    private final OrganisationMapper organisationMapper;

    public OrganisationStatisticsMapper(OrganisationMapper organisationMapper) {
        this.organisationMapper = organisationMapper;
    }

    public OrganisationStatisticsDto toDto(
            OrganisationStatistics stats,
            Map<CountryId, Country> countryMap,
            Map<OrganisationId, Organisation> orgMap) {
        return new OrganisationStatisticsDto(
                organisationMapper.toDto(stats.organisation(), countryMap, orgMap),
                stats.runnerCount(),
                stats.totalStarts(),
                stats.nonScoringStarts(),
                stats.startsPerRunner(),
                stats.nonScoringStartsPerRunner(),
                stats.nonScoringRatio()
        );
    }
}
