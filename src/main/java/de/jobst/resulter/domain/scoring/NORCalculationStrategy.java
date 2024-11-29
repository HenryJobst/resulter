package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class NORCalculationStrategy implements CupTypeCalculationStrategy {

    public static final CupType CUP_TYPE = CupType.NOR;

    private final Map<OrganisationId, Organisation> organisationById;
    private final Organisation norOrganisation;

    Set<String> classesToSkip = Set.of("BK", "BL", "Beg", "Trim", "Beginner", "OffK", "OffL", "D/H-12 Be");

    public NORCalculationStrategy(Map<OrganisationId, Organisation> organisationById) {
        this.organisationById = organisationById;
        norOrganisation = organisationById.values()
            .stream()
            .filter(x -> x.containsOrganisationWithShortName(CUP_TYPE.value()))
            .findFirst()
            .orElse(null);
    }

    private static double multiplyTime(double baseTime, double factor) {
        return baseTime * factor;
    }

    public static int calculateNorPoints(double bestTime, double currentTime) {
        if (currentTime <= bestTime) {
            return 12;
        } else if (currentTime <= multiplyTime(bestTime, 1.05)) {
            return 11;
        } else if (currentTime <= multiplyTime(bestTime, 1.10)) {
            return 10;
        } else if (currentTime <= multiplyTime(bestTime, 1.15)) {
            return 9;
        } else if (currentTime <= multiplyTime(bestTime, 1.20)) {
            return 8;
        } else if (currentTime <= multiplyTime(bestTime, 1.25)) {
            return 7;
        } else if (currentTime <= multiplyTime(bestTime, 1.35)) {
            return 6;
        } else if (currentTime <= multiplyTime(bestTime, 1.50)) {
            return 5;
        } else if (currentTime <= multiplyTime(bestTime, 1.70)) {
            return 4;
        } else if (currentTime <= multiplyTime(bestTime, 2.0)) {
            return 3;
        } else if (currentTime <= multiplyTime(bestTime, 3.0)) {
            return 2;
        } else {
            return 1;
        }
    }

    @Override
    public boolean valid(ClassResult classResult) {
        return classesToSkip.stream().noneMatch(it -> classResult.classResultShortName().value().equals(it));
    }

    @Override
    public boolean valid(PersonResult personResult) {
        Optional<Organisation> optionalOrganisation = organisationById.containsKey(personResult.organisationId()) ?
                                                      Optional.of(organisationById.get(personResult.organisationId())) :
                                                      Optional.empty();
        return optionalOrganisation.isPresent() && norOrganisation != null &&
               norOrganisation.containsOrganisationWithId(optionalOrganisation.get().getId());
    }

    @Override
    public List<CupScore> calculate(Cup cup,
                                    List<PersonRaceResult> personRaceResults,
                                    Map<PersonId, OrganisationId> organisationByPerson) {
        if (personRaceResults.isEmpty()) {
            return List.of();
        }

        PunchTime fastestTime = personRaceResults.getFirst().getRuntime();

        return personRaceResults.stream()
            .map(x -> calculateScore(x, organisationByPerson.get(x.getPersonId()), fastestTime))
            .toList();
    }

    private CupScore calculateScore(PersonRaceResult personRaceResult,
                                    OrganisationId organisationId,
                                    PunchTime fastestTime) {
        return CupScore.of(personRaceResult.getPersonId(),
            organisationId,
            personRaceResult.getClassResultShortName(),
            calculateNorPoints(fastestTime.value(), personRaceResult.getRuntime().value()));
    }
}
