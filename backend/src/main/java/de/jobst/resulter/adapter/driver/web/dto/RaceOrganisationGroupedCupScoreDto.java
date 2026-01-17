package de.jobst.resulter.adapter.driver.web.dto;

import java.util.List;

public record RaceOrganisationGroupedCupScoreDto(RaceDto race, List<OrganisationScoreDto> organisationScores) {}
