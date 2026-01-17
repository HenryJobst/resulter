package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.RaceOrganisationGroupedCupScoreDto;
import de.jobst.resulter.domain.aggregations.RaceOrganisationGroupedCupScore;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RaceOrganisationGroupedCupScoreMapper {

    private final OrganisationScoreMapper organisationScoreMapper;

    public RaceOrganisationGroupedCupScoreMapper(OrganisationScoreMapper organisationScoreMapper) {
        this.organisationScoreMapper = organisationScoreMapper;
    }

    public RaceOrganisationGroupedCupScoreDto toDto(RaceOrganisationGroupedCupScore raceCupScore) {
        return new RaceOrganisationGroupedCupScoreDto(
                RaceMapper.toDto(raceCupScore.race()),
                raceCupScore.organisationScores() != null
                        ? organisationScoreMapper.toDtos(raceCupScore.organisationScores())
                        : List.of());
    }
}
