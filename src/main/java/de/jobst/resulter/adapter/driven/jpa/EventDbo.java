package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.*;
import jakarta.persistence.*;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.Hibernate;
import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed", "unused"})
@Entity
@Table(name = "EVENT")
public class EventDbo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator_event")
    @SequenceGenerator(name = "entity_generator_event", sequenceName = "SEQ_EVENT_ID", allocationSize = 1)
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;
    @Column(name = "START_TIME")
    private ZonedDateTime startTime;
    @Column(name = "END_TIME")
    private ZonedDateTime endTime;
    @OneToMany(mappedBy = "eventDbo", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClassResultDbo> classResults = new HashSet<>();


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "EVENT_ORGANISATION", joinColumns = @JoinColumn(name = "EVENT_ID"),
               inverseJoinColumns = @JoinColumn(name = "ORGANISATION_ID"))
    private Set<OrganisationDbo> organisations = new HashSet<>();


    @Column(name = "STATE")
    @Enumerated(value = EnumType.STRING)
    private EventStatus state;

    //@ManyToMany(mappedBy = "events", fetch = FetchType.LAZY)
    //private Set<CupDbo> cups = new HashSet<>();

    public static EventDbo from(@NonNull Event event,
                                DboResolver<EventId, EventDbo> dboResolver,
                                @NonNull DboResolvers dboResolvers) {
        EventDbo eventDbo = null;
        EventDbo persistedEventDbo;
        if (event.getId().value() != EventId.empty().value()) {
            if (dboResolver != null) {
                eventDbo = dboResolver.findDboById(event.getId());
            }
            if (eventDbo == null) {
                eventDbo = dboResolvers.getEventDboResolver().findDboById(event.getId());
            }
            persistedEventDbo = eventDbo;
        } else {
            eventDbo = new EventDbo();
            persistedEventDbo = null;
        }

        eventDbo.setName(event.getName().value());

        if (ObjectUtils.isNotEmpty(event.getStartTime())) {
            eventDbo.setStartTime(event.getStartTime().value());
        }
        if (ObjectUtils.isNotEmpty(event.getEndTime())) {
            eventDbo.setEndTime(event.getEndTime().value());
        }

        if (event.getClassResults().isLoaded()) {
            EventDbo finalEventDbo = eventDbo;
            eventDbo.setClassResults(Objects.requireNonNull(event.getClassResults().get()).value().stream().map(it -> {
                ClassResultDbo persistedClassResultDbo =
                    persistedEventDbo != null && Hibernate.isInitialized(persistedEventDbo.getClassResults()) ?
                    persistedEventDbo.getClassResults()
                        .stream()
                        .filter(x -> x.getId() == it.getId().value())
                        .findFirst()
                        .orElse(null) :
                    null;
                return ClassResultDbo.from(it, finalEventDbo, (id) -> persistedClassResultDbo, dboResolvers);
            }).collect(Collectors.toSet()));
        } else if (persistedEventDbo != null) {
            eventDbo.setClassResults(persistedEventDbo.getClassResults());
        } else if (event.getId().isPersistent()) {
            throw new IllegalArgumentException();
        }

        eventDbo.setOrganisations(event.getOrganisationIds()
            .stream()
            .map(it -> dboResolvers.getOrganisationDboResolver().findDboById(it))
            .collect(Collectors.toSet()));

        if (ObjectUtils.isNotEmpty(event.getEventState())) {
            eventDbo.setState(event.getEventState());
        }

        return eventDbo;
    }

    static public List<Event> asEvents(@NonNull EventConfig eventConfig, @NonNull List<EventDbo> eventDbos) {
        Map<EventId, List<ClassResult>> classResultsByEventId;
        if (!eventConfig.shallowLoads().contains(EventConfig.ShallowEventLoads.CLASS_RESULTS)) {
            classResultsByEventId = ClassResultDbo.asClassResults(eventConfig,
                    eventDbos.stream().flatMap(x -> x.classResults.stream()).toList())
                .stream()
                .collect(Collectors.groupingBy(ClassResult::getEventId));
        } else {
            classResultsByEventId = null;
        }

        return eventDbos.stream()
            .map(it -> Event.of(it.id,
                it.name,
                it.startTime,
                it.endTime,
                classResultsByEventId == null ?
                null :
                classResultsByEventId.getOrDefault(EventId.of(it.id), new ArrayList<>()),
                eventConfig.shallowLoads().contains(EventConfig.ShallowEventLoads.EVENT_ORGANISATIONS) ?
                null :
                it.organisations.stream()
                    .map(x -> Objects.nonNull(x) ? OrganisationId.of(x.getId()) : null)
                    .filter(Objects::nonNull)
                    .toList(),
                it.state))
            .toList();
    }

    static public Event asEvent(@NonNull EventConfig eventConfig, @NonNull EventDbo eventDbo) {
        return asEvents(eventConfig, List.of(eventDbo)).getFirst();
    }

    public long getId() {
        return id != null ? id : EventId.empty().value();
    }

    public void setId(long id) {
        this.id = id;
    }

    public Set<ClassResultDbo> getClassResults() {
        return classResults;
    }

    public void setClassResults(Set<ClassResultDbo> classResults) {
        this.classResults = classResults;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    public Set<OrganisationDbo> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(Set<OrganisationDbo> organisations) {
        this.organisations = organisations;
    }

    public EventStatus getState() {
        return state;
    }

    public void setState(EventStatus state) {
        this.state = state;
    }

    /*
    public Set<CupDbo> getCups() {
        return cups;
    }

    public void setCups(Set<CupDbo> cups) {
        this.cups = cups;
    }
    */
}
