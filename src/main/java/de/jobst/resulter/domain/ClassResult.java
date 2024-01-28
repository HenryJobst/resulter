package de.jobst.resulter.domain;

import de.jobst.resulter.domain.scoring.CupTypeCalculationStrategy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record ClassResult(@NonNull ClassResultName classResultName, @NonNull ClassResultShortName classResultShortName,
                          @NonNull Gender gender, @NonNull PersonResults personResults, @Nullable CourseId courseId)
    implements Comparable<ClassResult> {

    public static ClassResult of(@NonNull String name,
                                 @NonNull String shortName,
                                 @NonNull Gender gender,
                                 @Nullable Collection<PersonResult> personResults,
                                 @Nullable CourseId courseId) {
        return new ClassResult(ClassResultName.of(name),
            ClassResultShortName.of(shortName),
            gender,
            PersonResults.of(personResults),
            courseId);
    }

    @Override
    public int compareTo(@NonNull ClassResult o) {
        return classResultName.compareTo(o.classResultName);
    }

    public void calculate(CupTypeCalculationStrategy cupTypeCalculationStrategy) {
        Set<RaceNumber> races = this.personResults()
            .value()
            .stream()
            .flatMap(it -> it.personRaceResults().value().stream())
            .map(PersonRaceResult::getRaceNumber)
            .collect(Collectors.toSet());
        races.forEach(raceNumber -> {
            List<PersonResult> personResults =
                this.personResults().value().stream().filter(cupTypeCalculationStrategy::valid).sorted().toList();
            List<PersonRaceResult> personRaceResults = personResults.stream()
                .flatMap(it -> it.personRaceResults().value().stream())
                .filter(x -> x.getRaceNumber() == raceNumber)
                .filter(y -> y.getState().equals(ResultStatus.OK))
                .sorted()
                .toList();
            cupTypeCalculationStrategy.calculate(personRaceResults);
        });

    }
}
