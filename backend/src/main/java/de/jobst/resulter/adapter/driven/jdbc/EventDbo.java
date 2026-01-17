package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.*;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @PersistenceCreator)
@Table(name = "event")
public class EventDbo {

    @Nullable
    @Id
    @With
    @Column("id")
    private final Long id;

    @Column("name")
    private String name;

    @Nullable
    @Column("start_time")
    private Timestamp startTime;

    @Nullable
    @Column("start_time_zone")
    private String startTimeZone;

    @Nullable
    @Column("end_time")
    private Timestamp endTime;

    @Nullable
    @Column("end_time_zone")
    private String endTimeZone;

    @Nullable
    @Column("state")
    private EventStatus state;

    @Column("discipline")
    private Discipline discipline;

    @Column("aggregate_score")
    private boolean aggregateScore;

    @MappedCollection(idColumn = "event_id")
    private Set<EventOrganisationDbo> organisations = new HashSet<>();

    public EventDbo() {
        this.id = null;
    }

    public EventDbo(String name) {
        this.id = null;
        this.name = name;
    }

    @SuppressWarnings("ConstantConditions")
    public static EventDbo from(Event event, DboResolvers dboResolvers) {
        EventDbo eventDbo;
        if (event.getId().isPersistent()) {
            eventDbo = Objects.requireNonNull(dboResolvers.getEventDboResolver().findDboById(event.getId()));
            eventDbo.setName(event.getName().value());
        } else {
            eventDbo = new EventDbo(event.getName().value());
        }

        if (null != event.getStartTime() && null != event.getStartTime().value()) {
            eventDbo.setStartTime(Timestamp.from(event.getStartTime().value().toInstant()));
            eventDbo.setStartTimeZone(event.getStartTime().value().getZone().getId());
        } else {
            eventDbo.setStartTime(null);
            eventDbo.setStartTimeZone(null);
        }

        if (null != event.getEndTime() && null != event.getEndTime().value()) {
            eventDbo.setEndTime(Timestamp.from(event.getEndTime().value().toInstant()));
            eventDbo.setEndTimeZone(event.getEndTime().value().getZone().getId());
        } else {
            eventDbo.setEndTime(null);
            eventDbo.setEndTimeZone(null);
        }

        if (ObjectUtils.isNotEmpty(event.getEventState())) {
            eventDbo.setState(event.getEventState());
        } else {
            eventDbo.setState(null);
        }

        eventDbo.setOrganisations(event.getOrganisationIds().stream()
                .map(x -> new EventOrganisationDbo(x.value()))
                .collect(Collectors.toSet()));

        eventDbo.setDiscipline(event.getDiscipline());

        eventDbo.setAggregateScore(event.isAggregatedScore());

        return eventDbo;
    }

    @SuppressWarnings("unused")
    public static List<Event> asEvents(
            Collection<EventDbo> eventDbos, Function<Long, Organisation> organisationResolver) {

        return eventDbos.stream()
                .map(it -> Event.of(
                        it.id,
                        it.name,
                        it.startTime != null && it.startTimeZone != null
                                ? it.startTime.toInstant().atZone(ZoneId.of(it.startTimeZone))
                                : null,
                        it.endTime != null && it.endTimeZone != null
                                ? it.endTime.toInstant().atZone(ZoneId.of(it.endTimeZone))
                                : null,
                        it.organisations.stream()
                                .map(x -> OrganisationId.of(x.id.getId()))
                                .toList(),
                        it.state,
                        it.discipline,
                        it.aggregateScore))
                .toList();
    }

    @SuppressWarnings("unused")
    public static Event asEvent(EventDbo eventDbo, Function<Long, Organisation> organisationResolver) {
        return asEvents(List.of(eventDbo), organisationResolver).getFirst();
    }

    @SuppressWarnings("unused")
    public Event asEvent(Function<Long, Organisation> organisationResolver) {
        return asEvents(List.of(this), organisationResolver).getFirst();
    }

    /**
     * Convert a collection of EventDbos to Events using pre-loaded Organisation map for batch loading.
     */
    public static List<Event> asEvents(Collection<EventDbo> eventDbos, Map<Long, Organisation> organisationMap) {

        return eventDbos.stream()
                .map(it -> Event.of(
                        it.id,
                        it.name,
                        it.startTime != null && it.startTimeZone != null
                                ? it.startTime.toInstant().atZone(ZoneId.of(it.startTimeZone))
                                : null,
                        it.endTime != null && it.endTimeZone != null
                                ? it.endTime.toInstant().atZone(ZoneId.of(it.endTimeZone))
                                : null,
                        it.organisations.stream()
                                .map(x -> OrganisationId.of(x.id.getId()))
                                .toList(),
                        it.state,
                        it.discipline,
                        it.aggregateScore))
                .toList();
    }

    /**
     * Static method to convert a single EventDbo to Event using pre-loaded Organisation map.
     */
    public static Event asEvent(EventDbo eventDbo, Map<Long, Organisation> organisationMap) {
        return asEvents(List.of(eventDbo), organisationMap).getFirst();
    }

    /**
     * Instance method to convert this EventDbo to Event using pre-loaded Organisation map.
     */
    public Event asEvent(Map<Long, Organisation> organisationMap) {
        return asEvents(List.of(this), organisationMap).getFirst();
    }

    public static String mapOrdersDomainToDbo(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            case "event.name.value" -> "name";
            case "startTime.value" -> "startTime";
            case "endTime.value" -> "endTime";
            case "state.id" -> "state";
            case "childOrganisationIds" -> "organisations";
            default -> order.getProperty();
        };
    }

    public static String mapOrdersDboToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            case "name" -> "event.name.value";
            case "startTime" -> "startTime.value";
            case "endTime" -> "endTime.value";
            case "state" -> "state.id";
            case "organisations" -> "childOrganisationIds";
            default -> order.getProperty();
        };
    }
}
