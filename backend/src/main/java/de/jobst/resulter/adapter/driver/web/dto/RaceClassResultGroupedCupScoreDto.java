package de.jobst.resulter.adapter.driver.web.dto;

import java.util.List;

public record RaceClassResultGroupedCupScoreDto(RaceDto race, List<ClassResultScoreDto> classResultScores) {}
