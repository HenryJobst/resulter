package de.jobst.resulter.domain;

import java.util.List;

public record SplitTimes(List<SplitTime> value) {
    public static SplitTimes of(List<SplitTime> splitTimes) {
        return new SplitTimes(splitTimes.stream().sorted().toList());
    }
}
