package de.jobst.resulter.domain;

import de.jobst.resulter.domain.scoring.CupTypeCalculationStrategy;
import de.jobst.resulter.domain.scoring.KJCalculationStrategy;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@ValueObject
public record ClassResult(
        @NonNull ClassResultName classResultName,
        @NonNull ClassResultShortName classResultShortName,
        @NonNull Gender gender,
        @NonNull PersonResults personResults,
        @Nullable CourseId courseId)
        implements Comparable<ClassResult> {

    public static ClassResult of(
            @NonNull String name,
            @NonNull String shortName,
            @NonNull Gender gender,
            @Nullable Collection<PersonResult> personResults,
            @Nullable CourseId courseId) {
        return new ClassResult(
                ClassResultName.of(name),
                ClassResultShortName.of(shortName),
                gender,
                PersonResults.of(personResults),
                courseId);
    }

    @Override
    public int compareTo(@NonNull ClassResult o) {
        return classResultName.compareTo(o.classResultName);
    }

    public List<CupScore> calculate(Cup cup, CupTypeCalculationStrategy cupTypeCalculationStrategy) {
        List<PersonResult> personResults = this.personResults().value().stream()
                .filter(cupTypeCalculationStrategy::valid)
                .sorted()
                .toList();
        var organisationByPerson = personResults.stream()
                .filter(v -> null != v.organisationId())
                .collect(Collectors.toMap(PersonResult::personId, PersonResult::organisationId));

        List<PersonRaceResult> personRaceResults;
        if (cupTypeCalculationStrategy instanceof KJCalculationStrategy) {
            personRaceResults = personResults.stream()
                    .flatMap(it -> it.personRaceResults().value().stream())
                    .toList();
        } else {
            personRaceResults = personResults.stream()
                    .flatMap(it -> it.personRaceResults().value().stream())
                    .filter(y -> y.getState().equals(ResultStatus.OK))
                    .sorted()
                    .toList();
        }

        return cupTypeCalculationStrategy.calculate(cup, personRaceResults, organisationByPerson);
    }
}
