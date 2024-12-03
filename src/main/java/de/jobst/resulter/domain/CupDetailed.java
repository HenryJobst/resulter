package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ClassResultShortNameScoreSummary;
import de.jobst.resulter.domain.util.CustomCollectors;
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
            .flatMap(x -> x.organisationScores().stream())
            .toList();

        Set<String> allClasses = organisationScores.stream()
            .flatMap(x -> x.personWithScores().stream())
            .map(PersonWithScore::classResultShortName)
            .map(ClassResultShortName::value)
            .collect(Collectors.toUnmodifiableSet());

        this.overallOrganisationScores = organisationWithScore.entrySet().stream().map(entry -> {
            Map<ClassResultShortName, ClassResultShortNameScoreSummary> groupedScores = organisationScores.stream()
                .filter(x -> x.organisation().equals(entry.getKey()))
                .flatMap(x -> x.personWithScores().stream())
                .collect(CustomCollectors.groupingByClassResultShortNameAndScoreSumming());

            Map<ClassResultShortName, ClassResultShortNameScoreSummary> completeScores = allClasses.stream()
                .collect(Collectors.toMap(ClassResultShortName::of,
                    classResultShortName -> new ClassResultShortNameScoreSummary(0.0, Set.of())));

            completeScores.putAll(groupedScores);

            List<PersonWithScore> personWithScores = completeScores.entrySet()
                .stream()
                .flatMap(e -> e.getValue()
                    .getIds()
                    .stream()
                    .map(personId -> new PersonWithScore(personId, e.getValue().getScore(), e.getKey())))
                .sorted(Comparator.comparing(PersonWithScore::score).reversed().thenComparing(
                    PersonWithScore::classResultShortName))
                .toList();

            return new OrganisationScore(entry.getKey(), entry.getValue(), personWithScores);
        }).sorted(Comparator.comparingDouble(OrganisationScore::score).reversed()).toList();

    }
}
