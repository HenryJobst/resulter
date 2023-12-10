package de.jobst.resulter.adapter.out.jpa;

import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EventRepositoryDataJpaAdapter implements EventRepository {

    private final EventJpaRepository eventJpaRepository;

    public EventRepositoryDataJpaAdapter(EventJpaRepository eventJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
    }

    @Override
    public Event save(Event event) {
        EventDbo eventEntity = EventDbo.from(event);
        EventDbo savedEventEntity = eventJpaRepository.save(eventEntity);
        return savedEventEntity.asEvent();
    }

    @Override
    public List<Event> findAll() {
        return eventJpaRepository.findAll().stream()
                .map(EventDbo::asEvent)
                .toList();
    }

    @Override
    public Optional<Event> findById(EventId eventId) {
        Optional<EventDbo> eventEntity =
                eventJpaRepository.findById(eventId.value());
        return eventEntity.map(EventDbo::asEvent);
    }

    @Override
    public Event findOrCreate(Event event) {
        Optional<EventDbo> eventEntity =
                eventJpaRepository.findByName(event.getName().value());
        if (eventEntity.isEmpty()) {
            eventEntity = Optional.of(EventDbo.from(save(event)));
        }
        EventDbo entity = eventEntity.get();
        return entity.asEvent();
    }
}
