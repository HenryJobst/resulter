package de.jobst.resulter.domain;

import java.util.Comparator;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Association;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("FieldMayBeFinal")
@AggregateRoot
@Getter
public class Race implements Comparable<Race> {

    @Identity
    @Setter
    private RaceId id;

    @Association
    private EventId eventId;

    @Nullable
    private RaceName raceName;

    private RaceNumber raceNumber;

    public Race(
        RaceId id, EventId eventId, @Nullable RaceName raceName, RaceNumber raceNumber) {
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

    public static Race of(EventId eventId, @Nullable String raceName, Byte raceNumber) {
        return Race.of(eventId, RaceName.of(raceName), RaceNumber.of(raceNumber));
    }

    public static Race of(RaceId raceId, EventId eventId, @Nullable String raceName, Byte raceNumber) {
        return new Race(raceId, eventId, RaceName.of(raceName), RaceNumber.of(raceNumber));
    }

    private static final Comparator<Race> COMPARATOR =
        Comparator.comparing(Race::getRaceNumber)
            .thenComparing(Race::getRaceName, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Race::getEventId);

    @Override
    public int compareTo(Race o) {
        return COMPARATOR.compare(this, o);
    }

}
