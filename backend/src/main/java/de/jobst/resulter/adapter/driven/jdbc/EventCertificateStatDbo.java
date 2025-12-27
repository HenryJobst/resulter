package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.*;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ =@PersistenceCreator)
@Table(name = "event_certificate_stat")
public class EventCertificateStatDbo {

    @Id
    @With
    @Column("id")
    @Nullable
    private final Long id;

    @Column("event_id")
    private AggregateReference<EventDbo, Long> event;

    @Column("person_id")
    private AggregateReference<PersonDbo, Long> person;

    @Column("generated")
    private Timestamp generated;

    public EventCertificateStatDbo(
            AggregateReference<EventDbo, Long> event, AggregateReference<PersonDbo, Long> person, Timestamp generated) {
        this.id = null;
        this.event = event;
        this.person = person;
        this.generated = generated;
    }

    public static EventCertificateStatDbo from(
            EventCertificateStat eventCertificateStat, DboResolvers dboResolvers) {
        EventCertificateStatDbo eventCertificateStatDbo;
        if (eventCertificateStat.getId().isPersistent() && dboResolvers.getEventCertificateStatDboResolver() != null) {
            eventCertificateStatDbo =
                    dboResolvers.getEventCertificateStatDboResolver().findDboById(eventCertificateStat.getId());
            eventCertificateStatDbo.setEvent(
                    AggregateReference.to(eventCertificateStat.getEvent().value()));
            eventCertificateStatDbo.setPerson(
                    AggregateReference.to(eventCertificateStat.getPerson().value()));
            eventCertificateStatDbo.setGenerated(Timestamp.from(eventCertificateStat.getGenerated()));
        } else {
            eventCertificateStatDbo = new EventCertificateStatDbo(
                    AggregateReference.to(eventCertificateStat.getEvent().value()),
                    AggregateReference.to(eventCertificateStat.getPerson().value()),
                    Timestamp.from(eventCertificateStat.getGenerated()));
        }

        return eventCertificateStatDbo;
    }

    public static List<EventCertificateStat> asEventCertificateStats(
            Collection<EventCertificateStatDbo> eventCertificateStatDbos,
            Function<Long, Event> eventResolver,
            Function<Long, Person> personResolver) {

        return eventCertificateStatDbos.stream()
                .map(it -> EventCertificateStat.of(
                        it.id, EventId.of(it.event.getId()), PersonId.of(it.person.getId()), it.generated.toInstant()))
                .toList();
    }

    public static EventCertificateStat asEventCertificateStat(
            EventCertificateStatDbo eventCertificateStatDbo,
            Function<Long, Event> eventResolver,
            Function<Long, Person> personResolver) {
        return asEventCertificateStats(List.of(eventCertificateStatDbo), eventResolver, personResolver)
                .getFirst();
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
