package de.jobst.resulter.domain;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.Association;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;

@Entity
@Getter
@Setter
public class EventCertificateStat {

    @Identity
    @NonNull
    private EventCertificateStatId id;

    @Association
    @NonNull
    private EventId event;

    @Association
    @NonNull
    private PersonId person;

    @NonNull
    private Instant generated;

    public EventCertificateStat(
            @NonNull EventCertificateStatId id,
            @NonNull EventId event,
            @NonNull PersonId person,
            @NonNull Instant generated) {
        this.id = id;
        this.event = event;
        this.person = person;
        this.generated = generated;
    }

    public static EventCertificateStat of(Long id, EventId event, PersonId person, Instant generated) {
        return new EventCertificateStat(EventCertificateStatId.of(id), event, person, generated);
    }

    public void update(EventId event, PersonId person, Instant generated) {
        this.event = event;
        this.person = person;
        this.generated = generated;
    }
}
