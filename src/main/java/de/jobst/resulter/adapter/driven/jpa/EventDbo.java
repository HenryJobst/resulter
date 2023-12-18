package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.*;
import jakarta.persistence.*;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed"})
@Entity
@Table(name = "EVENT")
public class EventDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator")
    @SequenceGenerator(name = "entity_generator", sequenceName = "SEQ_EVENT_ID", allocationSize = 1)
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;
    @Column(name = "START_TIME")
    private LocalDateTime startTime;
    @Column(name = "END_TIME")
    private LocalDateTime endTime;
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

    public static EventDbo from(Event event) {
        EventDbo eventDbo = new EventDbo();
        if (event.getId() != null) {
            eventDbo.setId(event.getId().value());
        }
        eventDbo.setName(event.getName().value());

        if (ObjectUtils.isNotEmpty(event.getStartTime())) {
            eventDbo.setStartTime(event.getStartTime().value());
        }
        if (ObjectUtils.isNotEmpty(event.getEndTime())) {
            eventDbo.setEndTime(event.getEndTime().value());
        }

        eventDbo.setClassResults(Objects.requireNonNull(event.getClassResults())
                .value().stream().map(it -> ClassResultDbo.from(it, eventDbo)).collect(Collectors.toSet()));

        eventDbo.setOrganisations(Objects.requireNonNull(event.getOrganisations())
                .value().stream().map(it -> OrganisationDbo.from(it)).collect(Collectors.toSet()));

        if (ObjectUtils.isNotEmpty(event.getEventState())) {
            eventDbo.setState(event.getEventState());
        }
        return eventDbo;
    }

    static public List<Event> asEvents(EventConfig eventConfig, List<EventDbo> eventDbos) {
        Map<EventId, List<ClassResult>> classResultsByEventId =
                ClassResultDbo.asClassResults(eventConfig,
                                eventDbos.stream().flatMap(x -> x.classResults.stream()).toList())
                        .stream()
                        .collect(Collectors.groupingBy(ClassResult::eventId));

        return eventDbos.stream()
                .map(it -> Event.of(it.id,
                        it.name,
                        it.startTime,
                        it.endTime,
                        classResultsByEventId.getOrDefault(EventId.of(it.id), new ArrayList<>()),
                        it.organisations.stream().map(x -> x.asOrganisation()).toList(),
                        it.state)
                )
                .toList();
    }

    public long getId() {
        return id != null ? id : EventId.empty().value();
    }

    public void setId(long id) {
        this.id = id;
    }

    public Collection<ClassResultDbo> getClassResults() {
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
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