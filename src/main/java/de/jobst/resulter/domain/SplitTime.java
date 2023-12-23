package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public class SplitTime implements Comparable<SplitTime> {
    @NonNull
    @Setter
    private SplitTimeId id;
    @NonNull
    private final PersonRaceResultId personRaceResultId;
    @NonNull
    private final ControlCode controlCode;
    @NonNull
    private final PunchTime punchTime;

    public SplitTime(@NonNull SplitTimeId id,
                     @NonNull PersonRaceResultId personRaceResultId,
                     @NonNull ControlCode controlCode,
                     @NonNull PunchTime punchTime) {
        this.id = id;
        this.personRaceResultId = personRaceResultId;
        this.controlCode = controlCode;
        this.punchTime = punchTime;
    }

    public static SplitTime of(String controlCode, Double punchTime) {
        return SplitTime.of(SplitTimeId.empty().value(),
                PersonRaceResultId.empty().value(),
                controlCode, punchTime);
    }

    public static SplitTime of(long id,
                               long personRaceResultId,
                               @Nullable String controlCode,
                               @Nullable Double punchTime) {
        return new SplitTime(SplitTimeId.of(id),
                PersonRaceResultId.of(personRaceResultId),
                ControlCode.of(controlCode), PunchTime.of(punchTime));
    }

    @Override
    public int compareTo(@NonNull SplitTime o) {
        int value = punchTime.compareTo(o.punchTime);
        if (value == 0) {
            value = controlCode.compareTo(o.controlCode);
        }
        if (value == 0) {
            value = id.compareTo(o.id);
        }
        return value;
    }
}
