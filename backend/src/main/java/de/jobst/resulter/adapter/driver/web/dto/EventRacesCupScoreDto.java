package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.adapter.driver.web.mapper.OrganisationScoreMapper;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.domain.aggregations.EventRacesCupScore;

import java.util.List;

public record EventRacesCupScoreDto(
        EventDto event,
        List<RaceOrganisationGroupedCupScoreDto> raceOrganisationGroupedCupScores,
        List<RaceClassResultGroupedCupScoreDto> raceClassResultGroupedCupScores) {

    public static EventRacesCupScoreDto from(
            EventRacesCupScore eventRacesCupScore,
            OrganisationService organisationService,
            EventCertificateService eventCertificateService,
            Boolean hasSplitTimes,
            OrganisationScoreMapper organisationScoreMapper) {
        return new EventRacesCupScoreDto(
                EventDto.from(eventRacesCupScore.event(), organisationService, eventCertificateService, hasSplitTimes),
                eventRacesCupScore.raceOrganisationGroupedCupScores().stream()
                        .map(r -> RaceOrganisationGroupedCupScoreDto.from(r, organisationScoreMapper))
                        .toList(),
                eventRacesCupScore.raceClassResultGroupedCupScores().stream()
                        .map(RaceClassResultGroupedCupScoreDto::from)
                        .toList());
    }
}
