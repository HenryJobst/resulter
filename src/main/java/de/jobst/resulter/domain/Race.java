package de.jobst.resulter.domain;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@SuppressWarnings("FieldMayBeFinal")
@AggregateRoot
@Getter
public class Race implements Comparable<Race> {

    @Identity
    @NonNull
    @Setter
    private RaceId id;

    @NonNull
    private EventId eventId;

    @Nullable
    private RaceName raceName;

    @NonNull
    private RaceNumber raceNumber;

    public Race(
            @NonNull RaceId id, @NonNull EventId eventId, @Nullable RaceName raceName, @NonNull RaceNumber raceNumber) {
        this.id = id;
        this.eventId = eventId;
        this.raceName = raceName;
        this.raceNumber = raceNumber;
    }

    public static Race of(EventId eventId, Byte raceNumber) {
        return Race.of(RaceId.empty(), eventId, null, raceNumber);
    }

    public static Race of(EventId eventId, RaceName raceName, RaceNumber raceNumber) {
        return new Race(RaceId.empty(), eventId, raceName, raceNumber);
    }

    public static Race of(EventId eventId, String raceName, Byte raceNumber) {
        return Race.of(eventId, RaceName.of(raceName), RaceNumber.of(raceNumber));
    }

    public static Race of(RaceId raceId, EventId eventId, String raceName, Byte raceNumber) {
        return new Race(raceId, eventId, RaceName.of(raceName), RaceNumber.of(raceNumber));
    }

    @Override
    public int compareTo(@NonNull Race o) {
        int val = this.raceNumber.compareTo(o.raceNumber);
        if (val == 0) {
            val = Objects.compare(this.raceName, o.raceName, RaceName::compareTo);
        }
        if (val == 0) {
            val = this.eventId.compareTo(o.eventId);
        }
        return val;
    }
}
