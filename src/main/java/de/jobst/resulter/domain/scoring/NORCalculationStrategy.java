package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;

import java.util.List;
import java.util.Set;

public class NORCalculationStrategy implements CupTypeCalculationStrategy {
    public static final CupType CUP_TYPE = CupType.NOR;
    Set<String> classesToSkip = Set.of("BK", "BL");

    @Override
    public boolean valid(ClassResult classResult) {
        return classesToSkip.stream().noneMatch(it -> classResult.getClassResultShortName().value().equals(it));
    }

    @Override
    public boolean valid(PersonResult personResult) {
        return personResult.getOrganisation().get().containsOrganisationWithName(CupType.NOR.value());
    }

    @Override
    public void calculate(List<PersonRaceResult> personRaceResults) {
        if (personRaceResults.isEmpty()) {
            return;
        }
        PersonRaceResult first = personRaceResults.getFirst();
        Position firstPosition = first.getPosition();
        PunchTime fastestTime = first.getRuntime();
        personRaceResults.forEach(personRaceResult -> {
            if (personRaceResult.getPosition().equals(firstPosition)) {
                personRaceResult.setScore(CUP_TYPE, CupScore.of(12));
            } else {
                personRaceResult.setScore(CUP_TYPE, calculateScore(fastestTime, personRaceResult.getRuntime()));
            }
        });
    }

    private CupScore calculateScore(PunchTime fastestTime, PunchTime runtime) {
        // TODO
        return CupScore.of(0);
    }
}
