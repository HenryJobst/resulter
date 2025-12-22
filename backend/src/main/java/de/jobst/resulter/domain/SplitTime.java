package de.jobst.resulter.domain;

import lombok.Getter;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
@Getter
public class SplitTime implements Comparable<SplitTime> {

    private final ControlCode controlCode;

    private final PunchTime punchTime;

    @Nullable
    private final SplitTimeListId splitTimeListId;

    public SplitTime(
            ControlCode controlCode, PunchTime punchTime, @Nullable SplitTimeListId splitTimeListId) {
        this.controlCode = controlCode;
        this.punchTime = punchTime;
        this.splitTimeListId = splitTimeListId;
    }

    public static SplitTime of(
            @Nullable String controlCode, @Nullable Double punchTime, @Nullable SplitTimeListId splitTimeListId) {
        return new SplitTime(ControlCode.of(controlCode), PunchTime.of(punchTime), splitTimeListId);
    }

    public static SplitTime of(@Nullable String controlCode, @Nullable Double punchTime) {
        return new SplitTime(ControlCode.of(controlCode), PunchTime.of(punchTime), null);
    }

    @Override
    public int compareTo(SplitTime o) {
        int value = punchTime.compareTo(o.punchTime);
        if (value == 0) {
            value = controlCode.compareTo(o.controlCode);
        }
        if (value == 0) {
            if (splitTimeListId != null && o.splitTimeListId != null) {
                value = splitTimeListId.compareTo(o.splitTimeListId);
            } else if (splitTimeListId == null && o.splitTimeListId == null) {
            } else if (splitTimeListId == null) {
                value = -1;
            } else {
                value = 1;
            }
        }
        return value;
    }
}
