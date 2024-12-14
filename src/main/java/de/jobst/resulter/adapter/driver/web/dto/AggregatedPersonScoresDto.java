package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.ClassResultShortName;

import java.util.List;

public record AggregatedPersonScoresDto(ClassResultShortName classResultShortName,
                                        List<PersonWithScoreDto> personWithScoreList) {}
