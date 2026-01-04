package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Association;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Function;

@AggregateRoot
@Getter
public class Event implements Comparable<Event> {

    @Identity
    @Setter
    private EventId id;

    private EventName name;

    @Nullable
    private DateTime startTime;

    @SuppressWarnings("FieldMayBeFinal")
    @Nullable
    private DateTime endTime;

    @Nullable
    private EventStatus eventState;

    @Association
    private Collection<OrganisationId> organisationIds;

    @Association
    @Nullable
    @Setter
    private EventCertificateId certificate;

    @Setter
    private Discipline discipline;

    @Setter
    private boolean aggregatedScore;

    public Event(
            EventId id,
            EventName eventName,
            @Nullable DateTime startTime,
            @Nullable DateTime endTime,
            Collection<OrganisationId> organisationIds,
            @Nullable EventStatus eventState,
            @Nullable EventCertificateId certificate,
            Discipline discipline,
            boolean aggregatedScore) {
        this.id = id;
        this.name = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organisationIds = organisationIds;
        this.eventState = eventState;
        this.certificate = certificate;
        this.discipline = discipline;
        this.aggregatedScore = aggregatedScore;
    }

    public static Event of(String name) {
        return Event.of(EventId.empty().value(), name);
    }

    public static Event of(@Nullable Long id, String name) {
        return Event.of(id, name, null, null, new HashSet<>(), EventStatus.getDefault(), Discipline.getDefault(),
            false);
    }

    public static Event of(String name, Collection<OrganisationId> organisations) {
        return Event.of(EventId.empty().value(), name, null, null, organisations, EventStatus.getDefault(), Discipline.getDefault(), false);
    }

    public static Event of(
            String name,
            @Nullable ZonedDateTime startTime,
            Collection<OrganisationId> organisations) {
        return Event.of(EventId.empty().value(), name, startTime, null, organisations, EventStatus.getDefault(),
            Discipline.getDefault(), false);
    }

    public static Event of(
            @Nullable Long id,
            String eventName,
            @Nullable ZonedDateTime startTime,
            @Nullable ZonedDateTime endTime,
            Collection<OrganisationId> organisations,
            @Nullable EventStatus eventState,
            Discipline discipline,
            boolean aggregatedScore) {
        return Event.of(id, eventName, startTime, endTime, organisations, eventState, null, discipline, aggregatedScore);
    }

    public static Event of(
            @Nullable Long id,
            String eventName,
            @Nullable ZonedDateTime startTime,
            @Nullable ZonedDateTime endTime,
            Collection<OrganisationId> organisations,
            @Nullable EventStatus eventState,
            @Nullable EventCertificateId certificate,
            Discipline discipline,
            boolean aggregatedScore) {
        return new Event(
                id == null ? EventId.empty() : EventId.of(id),
                EventName.of(eventName),
                DateTime.of(startTime),
                DateTime.of(endTime),
                organisations,
                eventState,
                certificate,
                discipline,
                aggregatedScore);
    }

    public void update(
        EventName eventName,
        @Nullable DateTime startTime,
        @Nullable EventStatus status,
        Collection<OrganisationId> organisations,
        @Nullable EventCertificateId certificate, Discipline discipline, boolean aggregatedScore) {
        ValueObjectChecks.requireNotNull(eventName);
        this.name = eventName;
        this.startTime = startTime;
        this.eventState = status;
        this.organisationIds = organisations;
        this.certificate = certificate;
        this.discipline = discipline;
        this.aggregatedScore = aggregatedScore;
    }

    @Override
    public int compareTo(Event o) {
        int val = (Objects.nonNull(this.startTime) && Objects.nonNull(o.startTime)
                ? this.startTime.compareTo(o.startTime)
                : (this.startTime == o.startTime ? 0 : (Objects.nonNull(this.startTime) ? -1 : 1)));
        if (val == 0) {
            val = this.name.compareTo(o.name);
        }
        return val;
    }

    public void withCertificate(Function<EventId, @Nullable EventCertificate> primaryEventCertificateResolver) {
        EventCertificate eventCertificate = primaryEventCertificateResolver.apply(id);
        if (eventCertificate != null) {
            setCertificate(eventCertificate.getId());
        }
    }
}
