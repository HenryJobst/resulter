package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

public record CupScoreId(CupType type, PersonRaceResultId value) implements Comparable<CupScoreId> {

    public static CupScoreId of(CupType type, long value) {
        if (value < 0L || type == null) {
            throw new IllegalArgumentException("Id must be greater or equal 0.");
        }
        return new CupScoreId(type, PersonRaceResultId.of(value));
    }

    public static CupScoreId empty() {
        return new CupScoreId(null, PersonRaceResultId.empty());
    }

    public static CupScoreId of(CupType cupType,
                                ClassResultShortName classResultShortName,
                                PersonId personId,
                                RaceNumber raceNumber,
                                PersonId personId1) {
        return null;
    }

    public boolean isPersistent() {
        return value != empty().value;
    }

    @Override
    public int compareTo(@NonNull CupScoreId o) {
        int val = type.compareTo(o.type);
        if (val == 0) {
            val = value.compareTo(o.value);
        }
        return val;
    }
}
