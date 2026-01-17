package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.CupStatisticsDto;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.aggregations.CupStatistics;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CupStatisticsMapper {

    private final OrganisationStatisticsMapper organisationStatisticsMapper;

    public CupStatisticsMapper(OrganisationStatisticsMapper organisationStatisticsMapper) {
        this.organisationStatisticsMapper = organisationStatisticsMapper;
    }

    public CupStatisticsDto toDto(CupStatistics cupStatistics) {
        return new CupStatisticsDto(
                CupOverallStatisticsMapper.toDto(cupStatistics.overallStatistics()),
                organisationStatisticsMapper.toDtos(cupStatistics.organisationStatistics()));
    }

    public CupStatisticsDto toDto(
            CupStatistics cupStatistics, Map<CountryId, Country> countryMap, Map<OrganisationId, Organisation> orgMap) {
        return new CupStatisticsDto(
                CupOverallStatisticsMapper.toDto(cupStatistics.overallStatistics()),
                cupStatistics.organisationStatistics().stream()
                        .map(stats -> organisationStatisticsMapper.toDto(stats, countryMap, orgMap))
                        .toList());
    }
}
