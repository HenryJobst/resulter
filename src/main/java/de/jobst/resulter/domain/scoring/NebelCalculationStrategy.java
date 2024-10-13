package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;

import java.util.List;
import java.util.Map;

public class NebelCalculationStrategy implements CupTypeCalculationStrategy {

    private final Map<OrganisationId, Organisation> organisationById;

    public NebelCalculationStrategy(Map<OrganisationId, Organisation> organisationById) {
        this.organisationById = organisationById;
    }

    @Override
    public boolean valid(ClassResult classResult) {
        return false;
    }

    @Override
    public boolean valid(PersonResult personResult) {
        return false;
    }

    @Override
    public List<CupScore> calculate(Cup cup, List<PersonRaceResult> personRaceResults) {

        return null;
    }
}
