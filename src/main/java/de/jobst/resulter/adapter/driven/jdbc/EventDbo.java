package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventStatus;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.ResultListId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "event")
public class EventDbo {

    @Id
    @With
    private final Long id;

    @Column("name")
    private String name;
    @Column("start_time")
    private ZonedDateTime startTime;
    @Column("end_time")
    private ZonedDateTime endTime;
    @Column("state")
    private EventStatus state;

    @MappedCollection(idColumn = "event_id")
    private Set<EventResultListDbo> resultLists = new HashSet<>();

    @MappedCollection(idColumn = "event_id")
    private Set<EventOrganisationDbo> organisations = new HashSet<>();

    public EventDbo(String name) {
        this.id = null;
        this.name = name;
    }

    public static EventDbo from(@NonNull Event event, @NonNull DboResolvers dboResolvers) {
        EventDbo eventDbo;
        if (event.getId().isPersistent()) {
            eventDbo = dboResolvers.getEventDboResolver().findDboById(event.getId());
            eventDbo.setName(event.getName().value());
        } else {
            eventDbo = new EventDbo(event.getName().value());
        }

        if (ObjectUtils.isNotEmpty(event.getStartTime())) {
            eventDbo.setStartTime(event.getStartTime().value());
        } else {
            eventDbo.setStartTime(null);
        }

        if (ObjectUtils.isNotEmpty(event.getEndTime())) {
            eventDbo.setEndTime(event.getEndTime().value());
        } else {
            eventDbo.setEndTime(null);
        }

        if (ObjectUtils.isNotEmpty(event.getEventState())) {
            eventDbo.setState(event.getEventState());
        } else {
            eventDbo.setState(null);
        }

        eventDbo.setOrganisations(event.getReferencedOrganisationIds()
            .stream()
            .map(x -> new EventOrganisationDbo(x.value()))
            .collect(Collectors.toSet()));
        eventDbo.setResultLists(event.getResultListIds()
            .stream()
            .map(x -> new EventResultListDbo(x.value()))
            .collect(Collectors.toSet()));

        return eventDbo;
    }

    static public List<Event> asEvents(@NonNull Collection<EventDbo> eventDbos) {

        return eventDbos.stream()
            .map(it -> Event.of(it.id,
                it.name,
                it.startTime,
                it.endTime,
                it.resultLists.stream()
                    .map(x -> Objects.nonNull(x) ? ResultListId.of(x.id.getId()) : null)
                    .filter(Objects::nonNull)
                    .toList(),
                it.organisations.stream()
                    .map(x -> Objects.nonNull(x) ? OrganisationId.of(x.id.getId()) : null)
                    .filter(Objects::nonNull)
                    .toList(),
                it.state))
            .toList();
    }

    static public Event asEvent(@NonNull EventDbo eventDbo) {
        return asEvents(List.of(eventDbo)).getFirst();
    }

}
