package de.jobst.resulter.adapter.out.inmem;

import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("unused")
public class InMemoryEventRepository implements EventRepository {
    private final Map<EventId, Event> events = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<Event> savedEvents = new ArrayList<>();

    @Override
    public Event save(Event event) {
        if (ObjectUtils.isNotEmpty(event.getId())) {
            event = Event.of(sequence.getAndIncrement(),
                    event.getName().value(),
                    Objects.requireNonNull(event.getClassResults()).classResults());
        }
        events.put(event.getId(), event);
        savedEvents.add(event);
        return event;
    }

    @Override
    public List<Event> findAll() {
        return List.copyOf(events.values());
    }

    @Override
    public Optional<Event> findById(EventId EventId) {
        return Optional.ofNullable(events.get(EventId));
    }

    @Override
    public Event findOrCreate(Event event) {
        return events.values()
                .stream()
                .filter(it -> Objects.equals(it.getName(), event.getName()))
                .findAny()
                .orElseGet(() -> save(event));
    }

    public List<Event> savedEvents() {
        return savedEvents;
    }

    public int saveCount() {
        return savedEvents.size();
    }

    public void resetSaveCount() {
        savedEvents.clear();
    }

}
