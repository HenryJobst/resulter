package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Association;
import org.jmolecules.ddd.annotation.Identity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Function;

@AggregateRoot
@Getter
public class Event implements Comparable<Event> {

    @Identity
    @NonNull
    @Setter
    private EventId id;

    @NonNull
    private EventName name;

    @Nullable
    private DateTime startTime;

    @SuppressWarnings("FieldMayBeFinal")
    @Nullable
    private DateTime endTime;

    @NonNull
    private EventStatus eventState;

    @Association
    @NonNull
    private Collection<OrganisationId> organisationIds;

    @Association
    @Nullable
    @Setter
    private EventCertificateId certificate;

    public Event(
            @NonNull EventId id,
            @NonNull EventName eventName,
            @Nullable DateTime startTime,
            @Nullable DateTime endTime,
            @NonNull Collection<OrganisationId> organisationIds,
            @NonNull EventStatus eventState,
            @Nullable EventCertificateId certificate) {
        this.id = id;
        this.name = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organisationIds = organisationIds;
        this.eventState = eventState;
        this.certificate = certificate;
    }

    public static Event of(@NonNull String name) {
        return Event.of(EventId.empty().value(), name);
    }

    public static Event of(Long id, @NonNull String name) {
        return Event.of(id, name, null, null, new HashSet<>(), EventStatus.getDefault());
    }

    public static Event of(@NonNull String name, @NonNull Collection<OrganisationId> organisations) {
        return Event.of(EventId.empty().value(), name, null, null, organisations, EventStatus.getDefault());
    }

    public static Event of(
            @NonNull String name,
            @Nullable ZonedDateTime startTime,
            @NonNull Collection<OrganisationId> organisations) {
        return Event.of(EventId.empty().value(), name, startTime, null, organisations, EventStatus.getDefault());
    }

    public static Event of(
            Long id,
            @NonNull String eventName,
            @Nullable ZonedDateTime startTime,
            @Nullable ZonedDateTime endTime,
            @NonNull Collection<OrganisationId> organisations,
            @NonNull EventStatus eventState) {
        return Event.of(id, eventName, startTime, endTime, organisations, eventState, null);
    }

    public static Event of(
            Long id,
            @NonNull String eventName,
            @Nullable ZonedDateTime startTime,
            @Nullable ZonedDateTime endTime,
            @NonNull Collection<OrganisationId> organisations,
            @NonNull EventStatus eventState,
            @Nullable EventCertificateId certificate) {
        return new Event(
                EventId.of(id),
                EventName.of(eventName),
                DateTime.of(startTime),
                DateTime.of(endTime),
                organisations,
                eventState,
                certificate);
    }

    public void update(
            @NonNull EventName eventName,
            @Nullable DateTime startTime,
            @NonNull EventStatus status,
            @NonNull Collection<OrganisationId> organisations,
            @Nullable EventCertificateId certificate) {
        ValueObjectChecks.requireNotNull(eventName);
        this.name = eventName;
        this.startTime = startTime;
        this.eventState = status;
        this.organisationIds = organisations;
        this.certificate = certificate;
    }

    @Override
    public int compareTo(@NonNull Event o) {
        int val = (Objects.nonNull(this.startTime) && Objects.nonNull(o.startTime)
                ? this.startTime.compareTo(o.startTime)
                : (this.startTime == o.startTime ? 0 : (Objects.nonNull(this.startTime) ? -1 : 1)));
        if (val == 0) {
            val = this.name.compareTo(o.name);
        }
        return val;
    }

    public void withCertificate(Function<EventId, EventCertificate> primaryEventCertificateResolver) {
        setCertificate(primaryEventCertificateResolver.apply(id).getId());
    }
}
