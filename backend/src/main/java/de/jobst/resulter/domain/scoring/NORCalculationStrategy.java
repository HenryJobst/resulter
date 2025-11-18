package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class NORCalculationStrategy implements CupTypeCalculationStrategy {

    public static final CupType CUP_TYPE = CupType.NOR;

    @Nullable private final Map<OrganisationId, Organisation> organisationById;
    @Nullable private final Organisation norOrganisation;

    Set<String> classesToSkip = Set.of(
            "BK",
            "BL",
            "Beg",
            "Trim",
            "Hasen",
            "Beginner",
            "OffK",
            "OffL",
            "D/H12 Be",
            "D/H10B",
            "D/H10 B",
            "D/H10 Be",
            "D/H12b",
            "D/H12B",
            "D/H -12b",
            "D/H12 Beg.",
            "Begleitung",
            "D10B",
            "H10B",
            "D12B",
            "H12B",
            "KL");

    String mainClassWomenLong = "D19L";
    Set<String> mainClassesWomenLong = Set.of("D19", "D19L", "D19AL", "D20", "D21", "D21L", "D21AL");
    String mainClassWomenShort = "D19K";
    Set<String> mainClassesWomenShort = Set.of("D19K", "D19AK", "D21K", "D21B");

    String mainClassMenLong = "H19L";
    Set<String> mainClassesMenLong = Set.of("H19", "H19L", "H19AL", "H20", "H21", "H21L", "H21AL");
    String mainClassMenShort = "H19K";
    Set<String> mainClassesMenShort = Set.of("H19K", "H19AK", "H21K", "H21B");

    public NORCalculationStrategy(@Nullable Map<OrganisationId, Organisation> organisationById) {
        this.organisationById = organisationById;
        norOrganisation = organisationById != null
                ? organisationById.values().stream()
                        .filter(x -> x.containsOrganisationWithShortName(CUP_TYPE.value(), organisationById))
                        .findFirst()
                        .orElse(null)
                : null;
    }

    private static double multiplyTime(double baseTime, double factor) {
        return baseTime * factor;
    }

    public static int calculateNorPoints(double bestTime, double currentTime) {
        if (currentTime < bestTime) {
            throw new IllegalArgumentException("currentTime < bestTime");
        } else if (currentTime == bestTime) {
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
        return classesToSkip.stream()
                .noneMatch(it -> classResult.classResultShortName().value().equals(it));
    }

    @Override
    public boolean valid(PersonResult personResult) {
        if (organisationById == null) {
            throw new IllegalArgumentException("organisationById is null");
        }
        if (norOrganisation == null) {
            throw new IllegalArgumentException("norOrganisation is null");
        }
        Optional<Organisation> optionalOrganisation = organisationById.containsKey(personResult.organisationId())
                ? Optional.of(organisationById.get(personResult.organisationId()))
                : Optional.empty();
        return optionalOrganisation.isPresent()
                && norOrganisation.containsOrganisationWithId(
                        optionalOrganisation.get().getId(), organisationById);
    }

    @Override
    public List<CupScore> calculate(
            Cup cup, List<PersonRaceResult> personRaceResults, Map<PersonId, OrganisationId> organisationByPerson) {
        if (personRaceResults.isEmpty()) {
            return List.of();
        }

        PunchTime fastestTime = personRaceResults.getFirst().getRuntime();

        return personRaceResults.stream()
                .map(x -> calculateScore(x, organisationByPerson.get(x.getPersonId()), fastestTime))
                .toList();
    }

    private CupScore calculateScore(
            PersonRaceResult personRaceResult, OrganisationId organisationId, PunchTime fastestTime) {
        return CupScore.of(
                personRaceResult.getPersonId(),
                organisationId,
                personRaceResult.getClassResultShortName(),
                calculateNorPoints(
                        fastestTime.value(), personRaceResult.getRuntime().value()));
    }

    @Override
    public int getBestOfRacesCount(int racesCount) {
        return (racesCount / 2) + 1;
    }

    @Override
    public ClassResultShortName harmonizeClassResultShortName(ClassResultShortName classResultShortName) {
        String shortName = classResultShortName.value();
        shortName = shortName.strip();
        shortName = shortName.replace("-", "");
        if (mainClassesMenLong.contains(shortName)) {
            shortName = mainClassMenLong;
        } else if (mainClassesMenShort.contains(shortName)) {
            shortName = mainClassMenShort;
        } else if (mainClassesWomenLong.contains(shortName)) {
            shortName = mainClassWomenLong;
        } else if (mainClassesWomenShort.contains(shortName)) {
            shortName = mainClassWomenShort;
        }
        return ClassResultShortName.of(shortName);
    }
}
