package de.jobst.resulter.adapter.driver.web.dto;

import java.util.List;

public record OrganisationScoreDto(
        OrganisationDto organisation, Double score, List<PersonWithScoreDto> personWithScores) {}
