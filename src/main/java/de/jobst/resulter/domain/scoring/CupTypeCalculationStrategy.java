package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;

import java.util.List;

public interface CupTypeCalculationStrategy {

    boolean valid(ClassResult classResult);

    boolean valid(PersonResult personResult);

    List<CupScore> calculate(Cup cup, List<PersonRaceResult> personRaceResults);
}
