package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;

import java.util.List;
import java.util.Map;

public class AddCalculationStrategy implements CupTypeCalculationStrategy {

    @Override
    public boolean valid(ClassResult classResult) {
        return false;
    }

    @Override
    public boolean valid(PersonResult personResult) {
        return false;
    }

    @Override
    public boolean valid(Organisation organisation) { return false; }

    @Override
    public List<CupScore> calculate(Cup cup, List<PersonRaceResult> personRaceResults,
                                    Map<PersonId, OrganisationId> organisationByPerson) {

        return List.of();
    }

}
