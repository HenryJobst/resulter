package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

public record CupScore(int value) implements Comparable<CupScore> {

    public static CupScore of(int score) {
        return new CupScore(score);
    }

    @Override
    public int compareTo(@NonNull CupScore o) {
        return Integer.compare(value, o.value);
    }
}
