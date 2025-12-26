package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Race;
import de.jobst.resulter.domain.RaceId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "race")
public class RaceDbo {

    @Id
    @With
    @Column("id")
    @Nullable
    private Long id;

    @Column("event_id")
    private AggregateReference<EventDbo, Long> eventId;

    @Column("name")
    @Nullable
    private String name;

    @Column("number")
    private Byte number;


    public RaceDbo(Long eventId, @Nullable String name, Byte number) {
        this.id = null;
        this.eventId = AggregateReference.to(eventId);
        this.name = name;
        this.number = number;
    }

    public static @Nullable RaceDbo from(@Nullable Race race, DboResolvers dboResolvers) {
        if (null == race) {
            return null;
        }
        RaceDbo raceDbo;
        if (race.getId().isPersistent() && dboResolvers.getRaceDboResolver() != null) {
            raceDbo = dboResolvers.getRaceDboResolver().findDboById(race.getId());
            if (race.getRaceName() != null && race.getRaceName().value() != null) {
                raceDbo.setName(race.getRaceName().value());
            } else {
                raceDbo.setName(null);
            }
            raceDbo.setNumber(race.getRaceNumber().value());
        } else {
            raceDbo = new RaceDbo(race.getEventId().value(),
                race.getRaceName() != null ? race.getRaceName().value() : null,
                race.getRaceNumber().value());
        }
        return raceDbo;
    }

    public Race asRace() {
        return Race.of(id != null ? RaceId.of(id) : RaceId.empty(), EventId.of(eventId.getId()), name, number);
    }
}
