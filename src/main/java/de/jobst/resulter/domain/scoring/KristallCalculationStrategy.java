package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.ClassResult;
import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.PersonResult;

import java.util.List;

public class KristallCalculationStrategy implements CupTypeCalculationStrategy {
    @Override
    public boolean valid(ClassResult classResult) {
        return false;
    }

    @Override
    public boolean valid(PersonResult personResult) {
        return false;
    }

    @Override
    public void calculate(List<PersonRaceResult> personRaceResults) {

    }
}
