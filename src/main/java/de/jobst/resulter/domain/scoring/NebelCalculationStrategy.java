package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class NebelCalculationStrategy implements CupTypeCalculationStrategy {

    private final Map<OrganisationId, Organisation> organisationById;

    Set<String> classesToSkip = Set.of("BK", "BL", "Beg", "Trim", "Beginner", "OffK", "OffL", "D/H-12 Be");
    Set<String> organisationsToSkip = Set.of("ohne", "Volkssport");

    public NebelCalculationStrategy(Map<OrganisationId, Organisation> organisationById) {
        this.organisationById = organisationById;
    }

    @Override
    public boolean valid(ClassResult classResult) {
        return classesToSkip.stream().noneMatch(it -> classResult.classResultShortName().value().equals(it));
    }

    @Override
    public boolean valid(PersonResult personResult) {
        return Optional.ofNullable(organisationById.get(personResult.organisationId()))
            .map(org -> isNotSkippedOrganisation(org.getShortName().value()))
            .orElse(false);
    }

    private boolean isNotSkippedOrganisation(String organisationShortName) {
        return organisationsToSkip.stream().noneMatch(organisationShortName::contains);
    }

    @Override
    public List<CupScore> calculate(Cup cup, List<PersonRaceResult> personRaceResults,
                                    Map<PersonId, OrganisationId> organisationByPerson) {
        if (personRaceResults.isEmpty()) {
            return List.of();
        }

        PunchTime fastestTime = personRaceResults.getFirst().getRuntime();
        Set<OrganisationId> organisationWithScore = new HashSet<>();
        var personRaceResultsWithScore = personRaceResults.stream()
            .filter(x -> Optional.ofNullable(organisationByPerson.get(x.getPersonId()))
                .filter(organisationWithScore::add) // predicate will be applied only if Optional is not empty
                .isPresent())
            .toList();

        return personRaceResultsWithScore.stream().map(x -> calculateScore(x, fastestTime)).toList();
    }

    private CupScore calculateScore(PersonRaceResult personRaceResult, PunchTime fastestTime) {
        return CupScore.of(personRaceResult.getPersonId(),
            personRaceResult.getClassResultShortName(),
            NORCalculationStrategy.calculateNorPoints(fastestTime.value(), personRaceResult.getRuntime().value()));
    }
}
