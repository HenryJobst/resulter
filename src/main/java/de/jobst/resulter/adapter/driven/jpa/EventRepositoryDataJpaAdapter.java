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
        return savedEventEntity.asEvent(EventConfig.fromEvent(event));
    }

    @Override
    public List<Event> findAll(EventConfig eventConfig) {
        return eventJpaRepository.findAll().stream()
                .map(it -> it.asEvent(eventConfig))
                .toList();
    }

    @Override
    public Optional<Event> findById(EventId eventId) {
        Optional<EventDbo> eventEntity =
                eventJpaRepository.findById(eventId.value());
        return eventEntity.map(it -> it.asEvent(EventConfig.full()));
    }

    @Override
    public Event findOrCreate(Event event) {
        Optional<EventDbo> eventEntity =
                eventJpaRepository.findByName(event.getName().value());
        if (eventEntity.isEmpty()) {
            eventEntity = Optional.of(EventDbo.from(save(event)));
        }
        EventDbo entity = eventEntity.get();
        return entity.asEvent(EventConfig.fromEvent(event));
    }
}
