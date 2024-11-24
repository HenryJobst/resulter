package de.jobst.resulter.domain;

import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class CupDetailed extends Cup {

    private final List<EventRacesCupScore> eventRacesCupScore;
    private final List<Map.Entry<Organisation, Double>> overallOrganisationScores;

    public CupDetailed(@NonNull Cup cup, List<EventRacesCupScore> eventRacesCupScore) {
        super(cup.getId(), cup.getName(), cup.getType(), cup.getYear(), cup.getEvents());
        this.eventRacesCupScore = eventRacesCupScore;
        this.overallOrganisationScores = eventRacesCupScore.stream()
            .flatMap(x -> x.eventRaces().stream().flatMap(y ->
                y.organisationScores() != null ? y.organisationScores().stream() :
                Stream.empty()))
            .collect(Collectors.groupingBy(OrganisationScore::organisation,
                Collectors.summingDouble(OrganisationScore::score)))
            .entrySet()
            .stream()
            .sorted(Map.Entry.<Organisation, Double>comparingByValue().reversed())
            .toList();
    }
}
