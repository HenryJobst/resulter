package de.jobst.resulter.domain;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.Association;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.Nullable;

@Entity
@Getter
@Setter
public class EventCertificateStat {

    @Identity
    private EventCertificateStatId id;

    @Association
    private EventId event;

    @Association
    private PersonId person;

    private Instant generated;

    public EventCertificateStat(
            EventCertificateStatId id,
            EventId event,
            PersonId person,
            Instant generated) {
        this.id = id;
        this.event = event;
        this.person = person;
        this.generated = generated;
    }

    public static EventCertificateStat of(@Nullable Long id, EventId event, PersonId person, Instant generated) {
        return new EventCertificateStat(
            id != null ?
            EventCertificateStatId.of(id) : EventCertificateStatId.empty(),
            event, person, generated);
    }

    public void update(EventId event, PersonId person, Instant generated) {
        this.event = event;
        this.person = person;
        this.generated = generated;
    }
}
