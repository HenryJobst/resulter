package de.jobst.resulter.application;

import de.jobst.resulter.adapter.out.jpa.EventEntity;
import de.jobst.resulter.adapter.out.jpa.EventRepository;
import de.jobst.resulter.domain.Event;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Event findOrCreate(Event event) {
        Optional<EventEntity> byName = eventRepository.findByName(event.name());
        if (byName.isEmpty()) {
            EventEntity eventEntity = new EventEntity();
            eventEntity.setName(event.name());
            eventEntity = this.eventRepository.save(eventEntity);
            return Event.of(eventEntity.getId(), eventEntity.getName());
        }
        return Event.of(byName.get().getId(), byName.get().getName());
    }
}
