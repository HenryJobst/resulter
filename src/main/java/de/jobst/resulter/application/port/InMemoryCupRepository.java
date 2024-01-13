package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.EventId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "true")
public class InMemoryCupRepository implements CupRepository {

    private final Map<CupId, Cup> cups = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<Cup> savedCups = new ArrayList<>();

    @Override
    public Cup save(Cup cup) {
        if (ObjectUtils.isEmpty(cup.getId()) || cup.getId().value() == 0) {
            cup.setId(CupId.of(sequence.incrementAndGet()));
        }
        cups.put(cup.getId(), cup);
        savedCups.add(cup);
        return cup;
    }

    @Override
    public List<Cup> findAll() {
        return List.copyOf(cups.values());
    }

    @Override
    public Optional<Cup> findById(CupId cupId) {
        return Optional.ofNullable(cups.get(cupId));
    }

    @Override
    public Cup findOrCreate(Cup cup) {
        return cups.values()
            .stream()
            .filter(it -> Objects.equals(it.getName(), cup.getName()))
            .findAny()
            .orElseGet(() -> save(cup));
    }

    @Override
    public void deleteCup(Cup cup) {
        if (ObjectUtils.isEmpty(cup.getId()) || cup.getId().value() == 0) {
            return;
        }
        cups.remove(cup.getId());
        savedCups.remove(cup);
    }

    @Override
    public Collection<Cup> findByEvent(EventId eventId) {
        return this.cups.values()
            .stream()
            .filter(it -> it.getEventIds().stream().anyMatch(x -> x.equals(eventId)))
            .toList();
    }

    @SuppressWarnings("unused")
    public List<Cup> savedCups() {
        return savedCups;
    }

    @SuppressWarnings("unused")
    public int saveCount() {
        return savedCups.size();
    }

    @SuppressWarnings("unused")
    public void resetSaveCount() {
        savedCups.clear();
    }

}
