package de.jobst.resulter.domain.aggregations;

import de.jobst.resulter.domain.ClassResultScores;
import de.jobst.resulter.domain.Race;
import org.springframework.lang.NonNull;

import java.util.List;

public record RaceClassResultGroupedCupScore(Race race, List<ClassResultScores> classResultScores) implements Comparable<RaceClassResultGroupedCupScore> {

    @Override
    public int compareTo(@NonNull RaceClassResultGroupedCupScore o) {
        return this.race.compareTo(o.race);
    }
}
