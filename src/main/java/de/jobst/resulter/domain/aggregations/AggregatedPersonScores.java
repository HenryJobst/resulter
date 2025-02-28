package de.jobst.resulter.domain.aggregations;

import de.jobst.resulter.domain.ClassResultShortName;
import de.jobst.resulter.domain.PersonWithScore;

import java.util.List;

public record AggregatedPersonScores(ClassResultShortName classResultShortName,
                                     List<PersonWithScore> personWithScoreList) {}
