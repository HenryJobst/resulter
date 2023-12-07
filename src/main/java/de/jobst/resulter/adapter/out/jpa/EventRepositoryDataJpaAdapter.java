package de.jobst.resulter.adapter.out.jpa;

import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.domain.Event;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class EventRepositoryDataJpaAdapter implements EventRepository {

    private final EventJpaRepository eventJpaRepository;

    public EventRepositoryDataJpaAdapter(EventJpaRepository eventJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
    }

    @Override
    public Event findOrCreate(Event event) {
        Optional<EventEntity> eventEntity =
                eventJpaRepository.findByName(event.name().value());
        if (eventEntity.isEmpty()) {
            EventEntity entity = new EventEntity();
            entity.setName(event.name().value());
            eventEntity = Optional.of(eventJpaRepository.save(entity));
        }
        EventEntity entity = eventEntity.get();
        return Event.of(entity.getId(), entity.getName());
    }
}
