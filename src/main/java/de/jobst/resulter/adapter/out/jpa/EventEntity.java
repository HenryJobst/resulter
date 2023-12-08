package de.jobst.resulter.adapter.out.jpa;

import de.jobst.resulter.domain.Event;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Entity
@Table(name = "EVENTS")
@Getter
@Setter
@NoArgsConstructor
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator")
    @SequenceGenerator(name = "entity_generator", sequenceName = "SEQ_EVENTS_ID", allocationSize = 1)
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    public static EventEntity from(Event event) {
        EventEntity entity = new EventEntity();
        if (event.getId() != null) {
            entity.setId(event.getId().value());
        }
        entity.setName(event.getName().value());
        return entity;
    }

    public Event asEvent() {
        return Event.of(id, name, new ArrayList<>());
    }
}