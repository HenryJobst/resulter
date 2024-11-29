package de.jobst.resulter.domain;

import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class CupDetailed extends Cup {

    private final List<EventRacesCupScore> eventRacesCupScore;
    private final List<OrganisationScore> overallOrganisationScores;

    public CupDetailed(@NonNull Cup cup, List<EventRacesCupScore> eventRacesCupScore) {
        super(cup.getId(), cup.getName(), cup.getType(), cup.getYear(), cup.getEvents());

        this.eventRacesCupScore = eventRacesCupScore;
        Map<Organisation, Double> organisationWithScore = eventRacesCupScore.stream()
            .flatMap(x -> x.eventRaces()
                .stream()
                .flatMap(raceCupScore -> Objects.nonNull(raceCupScore.organisationScores()) ?
                                         raceCupScore.organisationScores().stream() :
                                         Stream.empty()))
            .collect(Collectors.groupingBy(OrganisationScore::organisation, // Gruppieren nach Organisation
                Collectors.summingDouble(OrganisationScore::score)));

        List<OrganisationScore> organisationScores = eventRacesCupScore.stream()
            .flatMap(x -> x.eventRaces().stream())
            .flatMap(x -> x.organisationScores().stream()).toList();

        Set<String> allClasses =
            organisationScores.stream().flatMap(x -> x.personWithScores().stream())
                .map(PersonWithScore::classResultShortName)
                .map(ClassResultShortName::value)
                .collect(Collectors.toUnmodifiableSet());

        this.overallOrganisationScores = organisationWithScore
            .entrySet()
            .stream()
            .map(entry -> {
                Map<ClassResultShortName, Double> groupedScores = organisationScores.stream()
                    .filter(x -> x.organisation().equals(entry.getKey()))
                    .flatMap(x -> x.personWithScores().stream())
                    .collect(Collectors.groupingBy(PersonWithScore::classResultShortName,
                        Collectors.summingDouble(PersonWithScore::score)));

                Map<ClassResultShortName, Double> completeScores = allClasses.stream()
                    .collect(Collectors.toMap(ClassResultShortName::of,
                        classResultShortName -> 0.0 ));

                completeScores.putAll(groupedScores);

                return new OrganisationScore(
                    entry.getKey(),
                    entry.getValue(),
                    completeScores.entrySet().stream()
                        .map(e -> new PersonWithScore(PersonId.empty(), e.getValue(), e.getKey()))
                        .sorted(Comparator.comparing(PersonWithScore::classResultShortName)
                            .thenComparing(Comparator.comparing(PersonWithScore::score).reversed()))
                        .toList()
                    );
            })
            .sorted(Comparator.comparingDouble(OrganisationScore::score).reversed())
            .toList();

    }
}
