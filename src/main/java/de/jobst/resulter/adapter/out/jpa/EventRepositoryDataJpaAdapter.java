package de.jobst.resulter.adapter.out.jpa;

import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class EventRepositoryDataJpaAdapter implements EventRepository {

    private final EventJpaRepository eventJpaRepository;

    public EventRepositoryDataJpaAdapter(EventJpaRepository eventJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
    }

    @Override
    public Event save(Event event) {
        EventEntity eventEntity = EventEntity.from(event);
        EventEntity savedEventEntity = eventJpaRepository.save(eventEntity);
        return savedEventEntity.asEvent();
    }

    @Override
    public List<Event> findAll() {
        return StreamSupport.stream(eventJpaRepository.findAll().spliterator(), false)
                .map(EventEntity::asEvent)
                .toList();
    }

    @Override
    public Optional<Event> findById(EventId EventId) {
        return Optional.empty();
    }

    @Override
    public Event findOrCreate(Event event) {
        Optional<EventEntity> eventEntity =
                eventJpaRepository.findByName(event.getName().value());
        if (eventEntity.isEmpty()) {
            eventEntity = Optional.of(EventEntity.from(save(event)));
        }
        EventEntity entity = eventEntity.get();
        return Event.of(entity.getId(), entity.getName());
    }
}
