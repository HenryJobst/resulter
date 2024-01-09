package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.ClassResult;
import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.PersonResult;

import java.util.List;

public interface CupTypeCalculationStrategy {
    boolean valid(ClassResult classResult);

    boolean valid(PersonResult personResult);

    void calculate(List<PersonRaceResult> personRaceResults);
}
