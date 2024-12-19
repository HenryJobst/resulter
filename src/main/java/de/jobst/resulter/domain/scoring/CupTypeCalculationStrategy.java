package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;

import java.util.List;
import java.util.Map;

public interface CupTypeCalculationStrategy {

    boolean valid(ClassResult classResult);

    boolean valid(PersonResult personResult);

    List<CupScore> calculate(Cup cup, List<PersonRaceResult> personRaceResults,
                             Map<PersonId, OrganisationId> organisationByPerson);

    default int getBestOfRacesCount(int racesCount) {
        return racesCount;
    }

    default ClassResultShortName harmonizeClassResultShortName(ClassResultShortName classResultShortName) {
        return classResultShortName;
    }
}
