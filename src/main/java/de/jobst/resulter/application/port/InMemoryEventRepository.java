package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventConfig;
import de.jobst.resulter.domain.EventId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "true")
public class InMemoryEventRepository implements EventRepository {
    private final Map<EventId, Event> events = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<Event> savedEvents = new ArrayList<>();

    @Override
    public Event save(Event event) {
        if (ObjectUtils.isEmpty(event.getId()) || event.getId().value() == 0) {
            event.setId(EventId.of(sequence.incrementAndGet()));
        }
        events.put(event.getId(), event);
        savedEvents.add(event);
        return event;
    }

    @Override
    public List<Event> findAll(EventConfig eventConfig) {
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

    @SuppressWarnings("unused")
    public List<Event> savedEvents() {
        return savedEvents;
    }

    @SuppressWarnings("unused")
    public int saveCount() {
        return savedEvents.size();
    }

    @SuppressWarnings("unused")
    public void resetSaveCount() {
        savedEvents.clear();
    }

}
