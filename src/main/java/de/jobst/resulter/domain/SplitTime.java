package de.jobst.resulter.domain;

public record SplitTime(ControlCode controlCode, PunchTime punchTime) {
    public static SplitTime of(String controlCode, Double punchTime) {
        return new SplitTime(ControlCode.of(controlCode), PunchTime.of(punchTime));
    }
}
