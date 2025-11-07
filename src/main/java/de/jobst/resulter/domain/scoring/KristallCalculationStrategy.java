package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class KristallCalculationStrategy implements CupTypeCalculationStrategy {

    @Nullable private final Map<OrganisationId, Organisation> organisationById;

    Set<String> classesToSkip = Set.of("BK", "BL", "Beg", "Trim", "Beginner", "OffK", "OffL", "D/H-12 Be");
    Set<String> organisationsToSkip = Set.of("ohne", "Volkssport");

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

        AtomicInteger nextPoints = new AtomicInteger(10);

        Set<OrganisationId> organisationWithScore = new HashSet<>();
        var personRaceResultsWithScore = personRaceResults.stream()
            .filter(x -> Optional.ofNullable(organisationByPerson.get(x.getPersonId()))
                .filter(organisationWithScore::add) // predicate will be applied only if Optional is not empty
                .isPresent())
            .toList();

        return personRaceResultsWithScore.stream()
            .map(x -> calculateScore(x,
                organisationByPerson.get(x.getPersonId()),
                Math.max(nextPoints.getAndUpdate(n -> n > 1 ? n - 1 : 1), 1)))
            .toList();
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
