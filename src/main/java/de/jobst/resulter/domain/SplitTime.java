package de.jobst.resulter.domain;

public record SplitTime(SplitTimeId id,
                        PersonRaceResultId personRaceResultId,
                        ControlCode controlCode,
                        PunchTime punchTime) {
    public static SplitTime of(String controlCode, Double punchTime) {
        return SplitTime.of(SplitTimeId.empty().value(),
                PersonRaceResultId.empty().value(),
                controlCode, punchTime);
    }

    public static SplitTime of(Long id, Long personRaceResultId, String controlCode, Double punchTime) {
        return new SplitTime(SplitTimeId.of(id),
                PersonRaceResultId.of(personRaceResultId),
                ControlCode.of(controlCode), PunchTime.of(punchTime));
    }
}
