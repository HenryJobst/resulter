package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

public record CupScore(CupScoreId id, double value) implements Comparable<CupScore> {

    public static CupScore of(CupScoreId id, double score) {
        return new CupScore(id, score);
    }

    @Override
    public int compareTo(@NonNull CupScore o) {
        int val = id.compareTo(o.id);
        if (val == 0) {
            val = Double.compare(value, o.value);
        }
        return val;
    }

}
