package de.jobst.resulter.application;

import de.jobst.resulter.domain.ClassResultShortName;
import de.jobst.resulter.domain.PersonWithScore;

import java.util.List;

public record AggregatedPersonScores(ClassResultShortName classResultShortName,
                                     List<PersonWithScore> personWithScoreList) {}
