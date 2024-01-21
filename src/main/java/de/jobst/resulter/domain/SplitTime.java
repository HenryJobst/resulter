package de.jobst.resulter.domain;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public class SplitTime implements Comparable<SplitTime> {

    @NonNull
    private final ControlCode controlCode;
    @NonNull
    private final PunchTime punchTime;

    public SplitTime(@NonNull ControlCode controlCode, @NonNull PunchTime punchTime) {
        this.controlCode = controlCode;
        this.punchTime = punchTime;
    }

    public static SplitTime of(@Nullable String controlCode, @Nullable Double punchTime) {
        return new SplitTime(ControlCode.of(controlCode), PunchTime.of(punchTime));
    }

    @Override
    public int compareTo(@NonNull SplitTime o) {
        int value = punchTime.compareTo(o.punchTime);
        if (value == 0) {
            value = controlCode.compareTo(o.controlCode);
        }
        return value;
    }
}
