package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventConfig;
import de.jobst.resulter.domain.EventId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class EventRepositoryDataJpaAdapter implements EventRepository {

    private final EventJpaRepository eventJpaRepository;

    public EventRepositoryDataJpaAdapter(EventJpaRepository eventJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
    }

    @Override
    public Event save(Event event) {
        EventDbo eventEntity = EventDbo.from(event);
        EventDbo savedEventEntity = eventJpaRepository.save(eventEntity);
        return EventDbo.asEvents(EventConfig.fromEvent(event), List.of(savedEventEntity)).getFirst();
    }

    @Override
    public List<Event> findAll(EventConfig eventConfig) {
        return EventDbo.asEvents(eventConfig, eventJpaRepository.findAll());
    }

    @Override
    public Optional<Event> findById(EventId eventId) {
        Optional<EventDbo> eventEntity =
                eventJpaRepository.findById(eventId.value());
        return eventEntity.map(it -> EventDbo.asEvents(EventConfig.full(), List.of(it)).getFirst());
    }

    @Override
    public Event findOrCreate(Event event) {
        Optional<EventDbo> optionalEventDbo =
                eventJpaRepository.findByName(event.getName().value());
        if (optionalEventDbo.isEmpty()) {
            optionalEventDbo = Optional.of(EventDbo.from(save(event)));
        }
        EventDbo eventDbo = optionalEventDbo.get();
        return EventDbo.asEvents(EventConfig.fromEvent(event), List.of(eventDbo)).getFirst();
    }
}
