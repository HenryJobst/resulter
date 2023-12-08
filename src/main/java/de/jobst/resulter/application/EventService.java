package de.jobst.resulter.application;

import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
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
        return eventRepository.findOrCreate(event);
    }

    Optional<Event> findById(EventId eventId) {
        return eventRepository.findById(eventId);
    }
}
