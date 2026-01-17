package de.jobst.resulter.adapter.driver.web.dto;

import java.util.List;

public record ClassResultScoreDto(String classResultShortName,
                                  List<PersonWithScoreDto> personWithScores) {
}
