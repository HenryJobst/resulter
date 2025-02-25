package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.EventCertificateStat;
import de.jobst.resulter.domain.EventCertificateStatId;
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
public class InMemoryEventCertificateStatRepository implements EventCertificateStatRepository {

    private final Map<EventCertificateStatId, EventCertificateStat> eventCertificateStats = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<EventCertificateStat> savedEventCertificateStats = new ArrayList<>();

    @Override
    public EventCertificateStat save(EventCertificateStat eventCertificate) {
        if (ObjectUtils.isEmpty(eventCertificate.getId()) || eventCertificate.getId().value() == 0) {
            eventCertificate.setId(EventCertificateStatId.of(sequence.incrementAndGet()));
        }
        eventCertificateStats.put(eventCertificate.getId(), eventCertificate);
        savedEventCertificateStats.add(eventCertificate);
        return eventCertificate;
    }

    @Override
    public void delete(EventCertificateStat eventCertificate) {
        if (ObjectUtils.isEmpty(eventCertificate.getId()) || eventCertificate.getId().value() == 0) {
            return;
        }
        eventCertificateStats.remove(eventCertificate.getId());
        savedEventCertificateStats.remove(eventCertificate);
    }

    @Override
    public void deleteAllByEventId(EventId eventId) {
        eventCertificateStats.values()
            .removeIf(eventCertificate -> eventCertificate.getEvent().equals(eventId));
        savedEventCertificateStats.removeIf(eventCertificate -> eventCertificate.getEvent().equals(eventId));
    }

    @Override
    public void deleteById(EventCertificateStatId id) {
        eventCertificateStats.remove(id);
        savedEventCertificateStats.removeIf(eventCertificate -> eventCertificate.getId().equals(id));
    }

    @Override
    public List<EventCertificateStat> findAll() {
        return List.copyOf(eventCertificateStats.values());
    }

    @Override
    public Optional<EventCertificateStat> findById(EventCertificateStatId EventCertificateStatId) {
        return Optional.ofNullable(eventCertificateStats.get(EventCertificateStatId));
    }

    @Override
    public Page<EventCertificateStat> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(eventCertificateStats.values()), pageable, eventCertificateStats.size());
    }

    @Override
    public List<EventCertificateStat> findAllByEvent(EventId id) {
        List<EventCertificateStat> result = new ArrayList<>();
        eventCertificateStats.values()
            .stream()
            .filter(eventCertificate -> eventCertificate.getEvent().equals(id))
            .forEach(result::add);
        return result;
    }

    @Override
    public void saveAll(List<EventCertificateStat> eventCertificates) {
        eventCertificates.forEach(this::save);
    }


    @SuppressWarnings("unused")
    public List<EventCertificateStat> savedEventCertificateStats() {
        return savedEventCertificateStats;
    }

    @SuppressWarnings("unused")
    public int saveCount() {
        return savedEventCertificateStats.size();
    }

    @SuppressWarnings("unused")
    public void resetSaveCount() {
        savedEventCertificateStats.clear();
    }

}
