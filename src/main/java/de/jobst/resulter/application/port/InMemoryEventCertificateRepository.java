package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "true")
public class InMemoryEventCertificateRepository implements EventCertificateRepository {

    private final Map<EventCertificateId, EventCertificate> eventCertificates = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<EventCertificate> savedEventCertificates = new ArrayList<>();

    @Override
    public EventCertificate save(EventCertificate eventCertificate) {
        if (ObjectUtils.isEmpty(eventCertificate.getId()) || eventCertificate.getId().value() == 0) {
            eventCertificate.setId(EventCertificateId.of(sequence.incrementAndGet()));
        }
        eventCertificates.put(eventCertificate.getId(), eventCertificate);
        savedEventCertificates.add(eventCertificate);
        return eventCertificate;
    }

    @Override
    public void delete(EventCertificate eventCertificate) {
        if (ObjectUtils.isEmpty(eventCertificate.getId()) || eventCertificate.getId().value() == 0) {
            return;
        }
        eventCertificates.remove(eventCertificate.getId());
        savedEventCertificates.remove(eventCertificate);
    }

    @Override
    public void deleteAllByEventId(EventId eventId) {
        eventCertificates.values().removeIf(eventCertificate -> eventCertificate.getEvent().getId().equals(eventId));
        savedEventCertificates.removeIf(eventCertificate -> eventCertificate.getEvent().getId().equals(eventId));
    }

    @Override
    public List<EventCertificate> findAll() {
        return List.copyOf(eventCertificates.values());
    }

    @Override
    public Optional<EventCertificate> findById(EventCertificateId EventCertificateId) {
        return Optional.ofNullable(eventCertificates.get(EventCertificateId));
    }

    @Override
    public Page<EventCertificate> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(eventCertificates.values()), pageable, eventCertificates.size());
    }

    @Override
    public List<EventCertificate> findAllByEvent(EventId id) {
        List<EventCertificate> result = new ArrayList<>();
        eventCertificates.values()
            .stream()
            .filter(eventCertificate -> eventCertificate.getEvent().getId().equals(id))
            .forEach(result::add);
        return result;
    }

    @Override
    public void saveAll(List<EventCertificate> eventCertificates) {
        eventCertificates.forEach(this::save);
    }


    @SuppressWarnings("unused")
    public List<EventCertificate> savedEventCertificates() {
        return savedEventCertificates;
    }

    @SuppressWarnings("unused")
    public int saveCount() {
        return savedEventCertificates.size();
    }

    @SuppressWarnings("unused")
    public void resetSaveCount() {
        savedEventCertificates.clear();
    }

}
