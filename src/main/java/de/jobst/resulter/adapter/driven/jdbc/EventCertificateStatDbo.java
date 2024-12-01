package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificateStat;
import de.jobst.resulter.domain.Person;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "event_certificate_stat")
public class EventCertificateStatDbo {

    @Id
    @With
    @Column("id")
    private final Long id;

    @Column("event_id")
    private AggregateReference<EventDbo, Long> event;

    @Column("person_id")
    private AggregateReference<PersonDbo, Long> person;

    @Column("generated")
    private Timestamp generated;

    public EventCertificateStatDbo(AggregateReference<EventDbo, Long> event,
                                   AggregateReference<PersonDbo, Long> person,
                                   Timestamp generated) {
        this.id = null;
        this.event = event;
        this.person = person;
        this.generated = generated;
    }

    public static EventCertificateStatDbo from(@NonNull EventCertificateStat eventCertificateStat,
                                               @NonNull DboResolvers dboResolvers) {
        EventCertificateStatDbo eventCertificateStatDbo;
        if (eventCertificateStat.getId().isPersistent()) {
            eventCertificateStatDbo =
                dboResolvers.getEventCertificateStatDboResolver().findDboById(eventCertificateStat.getId());
            eventCertificateStatDbo.setEvent(AggregateReference.to(eventCertificateStat.getEvent().getId().value()));
            eventCertificateStatDbo.setPerson(AggregateReference.to(eventCertificateStat.getPerson().getId().value()));
            eventCertificateStatDbo.setGenerated(Timestamp.from(eventCertificateStat.getGenerated()));
        } else {
            eventCertificateStatDbo =
                new EventCertificateStatDbo(AggregateReference.to(eventCertificateStat.getEvent().getId().value()),
                    AggregateReference.to(eventCertificateStat.getPerson().getId().value()),
                    Timestamp.from(eventCertificateStat.getGenerated()));
        }

        return eventCertificateStatDbo;
    }

    static public List<EventCertificateStat> asEventCertificateStats(
        @NonNull Collection<EventCertificateStatDbo> eventCertificateStatDbos,
        Function<Long, Event> eventResolver,
        Function<Long, Person> personResolver) {

        return eventCertificateStatDbos.stream()
            .map(it -> EventCertificateStat.of(it.id,
                eventResolver.apply(it.event.getId()),
                personResolver.apply(it.person.getId()),
                it.generated.toInstant()))
            .toList();
    }

    static public EventCertificateStat asEventCertificateStat(@NonNull EventCertificateStatDbo eventCertificateStatDbo,
                                                              Function<Long, Event> eventResolver,
                                                              Function<Long, Person> personResolver) {
        return asEventCertificateStats(List.of(eventCertificateStatDbo), eventResolver, personResolver).getFirst();
    }

    public static String mapOrdersDomainToDbo(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            default -> order.getProperty();
        };
    }

    public static String mapOrdersDboToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            default -> order.getProperty();
        };
    }
}
