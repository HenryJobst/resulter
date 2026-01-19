package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class KristallCalculationStrategy implements CupTypeCalculationStrategy {

    @Nullable private final Map<OrganisationId, Organisation> organisationById;

    Set<String> classesToSkip = Set.of("BK", "BL", "Beg", "Trim", "Beginner", "OffK", "OffL", "D/H-12 Be");
    Set<String> organisationsToSkip = Set.of("ohne", "Volkssport", "Volkssport Berlin");

    public KristallCalculationStrategy(@Nullable Map<OrganisationId, Organisation> organisationById) {
        this.organisationById = organisationById;
    }

    @Override
    public boolean valid(ClassResult classResult) {
        return classesToSkip.stream().noneMatch(it -> classResult.classResultShortName().value().equals(it));
    }

    @Override
    public boolean valid(PersonResult personResult) {
        if (organisationById == null) {
            throw new IllegalArgumentException("organisationById is null");
        }
        return Optional.ofNullable(organisationById.get(personResult.organisationId()))
            .map(org -> isNotSkippedOrganisation(org.getShortName().value()))
            .orElse(false);
    }

    @Override
    public boolean valid(Organisation organisation) {
        return isNotSkippedOrganisation(organisation.getShortName().value());
    }

    private boolean isNotSkippedOrganisation(String organisationShortName) {
        return organisationsToSkip.stream().noneMatch(organisationShortName::contains);
    }

    @Override
    public List<CupScore> calculate(Cup cup,
                                    List<PersonRaceResult> personRaceResults,
                                    Map<PersonId, OrganisationId> organisationByPerson) {
        if (personRaceResults.isEmpty()) {
            return List.of();
        }

        Set<OrganisationId> organisationWithScore = new HashSet<>();
        var personRaceResultsWithScore = personRaceResults.stream()
            .filter(x -> Optional.ofNullable(organisationByPerson.get(x.getPersonId()))
                .filter(organisationWithScore::add) // predicate will be applied only if Optional is not empty
                .isPresent())
            .toList();

        List<CupScore> scores = new ArrayList<>();
        int nextPoints = 10;
        PunchTime previousRuntime = null;
        int currentGroupPoints = 10;
        int currentGroupSize = 0;

        for (PersonRaceResult result : personRaceResultsWithScore) {
            PunchTime currentRuntime = result.getRuntime();

            if (previousRuntime != null && !currentRuntime.equals(previousRuntime)) {
                // New time group, update points for next group
                nextPoints = Math.max(currentGroupPoints - currentGroupSize, 1);
                currentGroupPoints = nextPoints;
                currentGroupSize = 0;
            }

            currentGroupSize++;
            previousRuntime = currentRuntime;

            scores.add(calculateScore(result,
                organisationByPerson.get(result.getPersonId()),
                currentGroupPoints));
        }

        return scores;
    }

    private CupScore calculateScore(PersonRaceResult personRaceResult,
                                    OrganisationId organisationId,
                                    Integer nextPoints) {
        return CupScore.of(personRaceResult.getPersonId(),
            organisationId,
            personRaceResult.getClassResultShortName(),
            nextPoints);
    }

}
