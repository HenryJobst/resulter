package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.Event;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "EVENT")
@Getter
@Setter
@NoArgsConstructor
public class EventDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator")
    @SequenceGenerator(name = "entity_generator", sequenceName = "SEQ_EVENT_ID", allocationSize = 1)
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "eventDbo", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClassResultDbo> classResults = new HashSet<>();

    public static EventDbo from(Event event) {
        EventDbo eventDbo = new EventDbo();
        if (event.getId() != null) {
            eventDbo.setId(event.getId().value());
        }
        eventDbo.setName(event.getName().value());
        eventDbo.setClassResults(Objects.requireNonNull(event.getClassResults())
                .value().stream().map(it -> ClassResultDbo.from(it, eventDbo)).collect(Collectors.toSet()));
        return eventDbo;
    }

    public Event asEvent() {
        return Event.of(id, name, classResults.stream().map(ClassResultDbo::asClassResult).toList());
    }
}