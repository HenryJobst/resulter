package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.util.List;

public record RaceClassResultGroupedCupScore(Race race, List<ClassResultScores> classResultScores) implements Comparable<RaceClassResultGroupedCupScore> {

    @Override
    public int compareTo(@NonNull RaceClassResultGroupedCupScore o) {
        return this.race.compareTo(o.race);
    }
}
