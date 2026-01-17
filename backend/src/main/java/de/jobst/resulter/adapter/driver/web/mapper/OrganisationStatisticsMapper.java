package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationStatisticsDto;
import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.aggregations.OrganisationStatistics;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OrganisationStatisticsMapper {

    private final OrganisationMapper organisationMapper;
    private final CountryService countryService;
    private final OrganisationService organisationService;

    public OrganisationStatisticsMapper(
            OrganisationMapper organisationMapper,
            CountryService countryService,
            OrganisationService organisationService) {
        this.organisationMapper = organisationMapper;
        this.countryService = countryService;
        this.organisationService = organisationService;
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
                stats.nonScoringRatio());
    }

    public List<OrganisationStatisticsDto> toDtos(List<OrganisationStatistics> organisationStatistics) {
        List<Organisation> organisations = organisationStatistics.stream()
                .map(OrganisationStatistics::organisation)
                .toList();
        Map<CountryId, Country> countryMap = countryService.batchLoadForOrganisations(organisations);
        Map<OrganisationId, Organisation> orgMap = organisationService.batchLoadChildOrganisations(organisations);
        return organisationStatistics.stream()
                .map(stats -> toDto(stats, countryMap, orgMap))
                .toList();
    }
}
