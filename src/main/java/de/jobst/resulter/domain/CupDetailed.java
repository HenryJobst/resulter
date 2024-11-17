package de.jobst.resulter.domain;

import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.List;

@Getter
public class CupDetailed extends Cup {

    private final List<EventRacesCupScore> eventRacesCupScore;

    public CupDetailed(@NonNull Cup cup, List<EventRacesCupScore> eventRacesCupScore) {
        super(cup.getId(), cup.getName(), cup.getType(), cup.getEventIds());
        this.eventRacesCupScore = eventRacesCupScore;
    }
}
