package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;

@ValueObject
public record SplitTime(@Nullable ControlCode controlCode, PunchTime punchTime, SplitTimeListId splitTimeListId) implements Comparable<SplitTime> {

    public static SplitTime of(@Nullable String controlCode, @Nullable Double punchTime, SplitTimeListId splitTimeListId) {
        return new SplitTime(ControlCode.of(controlCode), PunchTime.of(punchTime), splitTimeListId);
    }

    @Override
    public int compareTo(SplitTime o) {
        int value = punchTime.compareTo(o.punchTime);
        if (value == 0) {
            value = Objects.compare(controlCode, o.controlCode, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if (value == 0) {
            value = splitTimeListId.compareTo(o.splitTimeListId);
        }
        return value;
    }
}
