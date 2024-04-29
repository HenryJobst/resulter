package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.time.Instant;

@Getter
@Setter
public class EventCertificateStat {

    @NonNull
    private EventCertificateStatId id;

    @NonNull
    private Event event;

    @NonNull
    private Person person;

    @NonNull
    private Instant generated;

    public EventCertificateStat(@NonNull EventCertificateStatId id,
                                @NonNull Event event,
                                @NonNull Person person,
                                @NonNull Instant generated) {
        this.id = id;
        this.event = event;
        this.person = person;
        this.generated = generated;
    }

    public static EventCertificateStat of(Long id, Event event, Person person, Instant generated) {
        return new EventCertificateStat(EventCertificateStatId.of(id), event, person, generated);
    }

    public void update(Event event, Person person, Instant generated) {
        this.event = event;
        this.person = person;
        this.generated = generated;
    }
}
