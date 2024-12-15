package de.jobst.resulter.adapter.driver.web.dto;

import java.util.List;

public record AggregatedPersonScoresDto(String classResultShortName,
                                        List<PersonWithScoreDto> personWithScoreList) {}
