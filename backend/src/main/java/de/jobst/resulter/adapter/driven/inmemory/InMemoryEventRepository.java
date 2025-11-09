package de.jobst.resulter.adapter.driven.inmemory;

import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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

    @NonNull
    @Override
    public Event save(@NonNull Event event) {
        if (ObjectUtils.isEmpty(event.getId()) || event.getId().value() == 0) {
            event.setId(EventId.of(sequence.incrementAndGet()));
        }
        events.put(event.getId(), event);
        savedEvents.add(event);
        return event;
    }

    @Override
    public void deleteEvent(Event event) {
        if (ObjectUtils.isEmpty(event.getId()) || event.getId().value() == 0) {
            return;
        }
        events.remove(event.getId());
        savedEvents.remove(event);
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

    @Override
    public Page<Event> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(events.values()), pageable, events.size());
    }

    @Override
    public List<Event> findAllById(Collection<EventId> eventIds) {
        return eventIds.stream().map(x -> events.getOrDefault(x, null)).filter(Objects::nonNull).toList();
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
