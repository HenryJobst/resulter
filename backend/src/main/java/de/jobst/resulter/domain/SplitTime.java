package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
public record SplitTime(ControlCode controlCode, PunchTime punchTime, SplitTimeListId splitTimeListId) implements Comparable<SplitTime> {

    public static SplitTime of(String controlCode, @Nullable Double punchTime, SplitTimeListId splitTimeListId) {
        return new SplitTime(ControlCode.of(controlCode), PunchTime.of(punchTime), splitTimeListId);
    }

    @Override
    public int compareTo(SplitTime o) {
        int value = punchTime.compareTo(o.punchTime);
        if (value == 0) {
            value = controlCode.compareTo(o.controlCode);
        }
        if (value == 0) {
            value = splitTimeListId.compareTo(o.splitTimeListId);
        }
        return value;
    }
}
