package de.jobst.resulter.domain;

public record SplitTime(SplitTimeId id, ControlCode controlCode, PunchTime punchTime) {
    public static SplitTime of(String controlCode, Double punchTime) {
        return new SplitTime(SplitTimeId.of(0L), ControlCode.of(controlCode), PunchTime.of(punchTime));
    }

    public static SplitTime of(Long id, String controlCode, Double punchTime) {
        return new SplitTime(SplitTimeId.of(id), ControlCode.of(controlCode), PunchTime.of(punchTime));
    }
}
