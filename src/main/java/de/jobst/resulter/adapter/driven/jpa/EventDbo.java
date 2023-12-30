package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.*;
import jakarta.persistence.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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
    @JoinTable(
            name = "EVENT_ORGANISATION",
            joinColumns = @JoinColumn(name = "EVENT_ID"),
            inverseJoinColumns = @JoinColumn(name = "ORGANISATION_ID"))
    private Set<OrganisationDbo> organisations = new HashSet<>();


    @Column(name = "STATE")
    @Enumerated(value = EnumType.STRING)
    private EventStatus state;

    public static EventDbo from(@NonNull Event event, @Nullable EventDbo persistedEventDbo) {
        EventDbo eventDbo = new EventDbo();
        if (event.getId().value() != EventId.empty().value()) {
            eventDbo.setId(event.getId().value());
        }
        eventDbo.setName(event.getName().value());

        if (ObjectUtils.isNotEmpty(event.getStartTime())) {
            eventDbo.setStartTime(event.getStartTime().value());
        }
        if (ObjectUtils.isNotEmpty(event.getEndTime())) {
            eventDbo.setEndTime(event.getEndTime().value());
        }

        if (event.getClassResults().isLoaded()) {
            eventDbo.setClassResults(Objects.requireNonNull(event.getClassResults().get())
                    .value().stream().map(it -> {
                        ClassResultDbo
                                persistedClassResultDbo =
                                persistedEventDbo != null ?
                                        persistedEventDbo.getClassResults()
                                                .stream()
                                                .filter(x -> x.getId() == it.getId().value())
                                                .findFirst()
                                                .orElse(null) :
                                        null;
                        return ClassResultDbo.from(it, eventDbo, persistedClassResultDbo);
                    }).collect(Collectors.toSet()));
        } else if (persistedEventDbo != null) {
            eventDbo.setClassResults(persistedEventDbo.getClassResults());
        } else if (event.getId().isPersistent()) {
            throw new IllegalArgumentException();
        }

        if (event.getOrganisations().isLoaded()) {
            eventDbo.setOrganisations(Objects.requireNonNull(event.getOrganisations().get())
                    .value().stream().map(it -> {
                        OrganisationDbo persistedOrganisationDbo =
                                persistedEventDbo != null ?
                                        (persistedEventDbo.getOrganisations()
                                                .stream()
                                                .filter(x -> x.getId() == it.getId().value())
                                                .findFirst()
                                                .orElse(null))
                                        : null;
                        return OrganisationDbo.from(it, persistedOrganisationDbo);
                    }).collect(Collectors.toSet()));
        } else if (persistedEventDbo != null) {
            eventDbo.setOrganisations(persistedEventDbo.getOrganisations());
        } else if (event.getId().isPersistent()) {
            throw new IllegalArgumentException();
        }

        if (ObjectUtils.isNotEmpty(event.getEventState())) {
            eventDbo.setState(event.getEventState());
        }
        return eventDbo;
    }

    static public List<Event> asEvents(@NonNull EventConfig eventConfig, @NonNull List<EventDbo> eventDbos) {
        Map<EventId, List<ClassResult>> classResultsByEventId;
        if (!eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.CLASS_RESULTS)) {
            classResultsByEventId =
                    ClassResultDbo.asClassResults(eventConfig,
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
                        classResultsByEventId == null ? null :
                                classResultsByEventId.getOrDefault(EventId.of(it.id), new ArrayList<>()),
                        eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.EVENT_ORGANISATIONS) ? null :
                                it.organisations.stream()
                                        .map(x -> ObjectUtils.isNotEmpty(x) ? x.asOrganisation() : null)
                                        .toList(),
                        it.state)
                )
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
}