package de.jobst.resulter.domain;

import de.jobst.resulter.application.AggregatedPersonScores;
import de.jobst.resulter.domain.aggregations.EventRacesCupScore;
import de.jobst.resulter.domain.aggregations.OrganisationScore;
import de.jobst.resulter.domain.util.ClassResultShortNameScoreSummary;
import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Getter
public class CupDetailed extends Cup {

    // scores by race and organisation (grouped by) or classResultShortName (grouped by)
    private final List<EventRacesCupScore> eventRacesCupScore;

    // overall results by organisation (with score)
    private List<OrganisationScore> overallOrganisationScores;

    private List<AggregatedPersonScores> aggregatedPersonScoresList;

    public CupDetailed(@NonNull Cup cup, @NonNull List<EventRacesCupScore> eventRacesCupScore,
                       List<AggregatedPersonScores> aggregatedPersonScoresList) {
        super(cup.getId(), cup.getName(), cup.getType(), cup.getYear(), cup.getEventIds());

        this.eventRacesCupScore = eventRacesCupScore;
        if (cup.getType().isGroupedByOrganisation()) {
            initializeGroupedByOrganisationData();
        } else {
            this.overallOrganisationScores = List.of();
            this.aggregatedPersonScoresList = aggregatedPersonScoresList;
        }
    }

    private void initializeGroupedByOrganisationData() {

        Map<Organisation, Double> organisationWithScore = this.eventRacesCupScore.stream()
            .flatMap(x -> x.raceOrganisationGroupedCupScores()
                .stream()
                .flatMap(raceCupScore -> Objects.nonNull(raceCupScore.organisationScores()) ?
                                         raceCupScore.organisationScores().stream() :
                                         Stream.empty()))
            .collect(Collectors.groupingBy(OrganisationScore::organisation, // Gruppieren nach Organisation
                Collectors.summingDouble(OrganisationScore::score)));

        List<OrganisationScore> organisationScores = this.eventRacesCupScore.stream()
            .flatMap(x -> x.raceOrganisationGroupedCupScores().stream())
            .flatMap(x -> x.organisationScores().stream())
            .toList();

        Set<String> allClasses = organisationScores.stream()
            .flatMap(x -> x.personWithScores().stream())
            .map(PersonWithScore::classResultShortName)
            .map(ClassResultShortName::value)
            .collect(Collectors.toUnmodifiableSet());

        this.overallOrganisationScores = organisationWithScore.entrySet().stream().map(entry -> {

            Map<ClassResultShortName, List<ClassResultShortNameScoreSummary>> groupedScores =
                organisationScores.stream()
                    .filter(x -> x.organisation().equals(entry.getKey()))
                    .flatMap(x -> x.personWithScores().stream())
                    .collect(Collectors.toMap(PersonWithScore::classResultShortName,
                        x -> new ArrayList<>(List.of(new ClassResultShortNameScoreSummary(x.score(), x.id()))),
                        (existing, merging) -> {
                            existing.addAll(merging);
                            return existing;
                        }));

            Map<ClassResultShortName, List<ClassResultShortNameScoreSummary>> completeScores = allClasses.stream()
                .collect(Collectors.toMap(ClassResultShortName::of,
                    classResultShortName -> List.of(new ClassResultShortNameScoreSummary(0.0, PersonId.empty()))));

            completeScores.putAll(groupedScores);

            List<PersonWithScore> personWithScores = completeScores.entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream().map(x -> new PersonWithScore(x.getId(), x.getScore(), e.getKey())))
                .sorted(Comparator.comparing(PersonWithScore::score)
                    .reversed()
                    .thenComparing(PersonWithScore::classResultShortName))
                .toList();

            return new OrganisationScore(entry.getKey(), entry.getValue(), personWithScores);
        }).sorted(Comparator.comparingDouble(OrganisationScore::score).reversed()).toList();
    }
}
