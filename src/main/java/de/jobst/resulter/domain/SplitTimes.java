package de.jobst.resulter.domain;

import java.util.Collection;

public record SplitTimes(Collection<SplitTime> value) {
    public static SplitTimes of(Collection<SplitTime> splitTimes) {
        return new SplitTimes(splitTimes);
    }
}
