package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationStatisticsDto;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.aggregations.OrganisationStatistics;
import java.util.List;
import java.util.Map;

public class OrganisationStatisticsMapper {

    public static OrganisationStatisticsDto toDto(
            OrganisationStatistics stats,
            Map<CountryId, Country> countryMap,
            Map<OrganisationId, Organisation> orgMap) {
        return new OrganisationStatisticsDto(
                OrganisationMapper.toDto(stats.organisation(), countryMap, orgMap),
                stats.runnerCount(),
                stats.totalStarts(),
                stats.nonScoringStarts(),
                stats.startsPerRunner(),
                stats.nonScoringStartsPerRunner(),
                stats.nonScoringRatio());
    }

    public static List<OrganisationStatisticsDto> toDtos(
            List<OrganisationStatistics> organisationStatistics,
            Map<CountryId, Country> countryMap,
            Map<OrganisationId, Organisation> orgMap) {
        return organisationStatistics.stream()
                .map(stats -> toDto(stats, countryMap, orgMap))
                .toList();
    }
}
