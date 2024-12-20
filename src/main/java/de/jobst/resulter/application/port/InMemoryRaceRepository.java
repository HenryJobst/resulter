package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Race;
import de.jobst.resulter.domain.RaceId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "true")
public class InMemoryRaceRepository implements RaceRepository {

    private final Map<RaceId, Race> races = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<Race> savedRaces = new ArrayList<>();

    @Override
    public Race save(Race race) {
        if (ObjectUtils.isEmpty(race.getId()) || race.getId().value() == 0) {
            race.setId(RaceId.of(sequence.incrementAndGet()));
        }
        races.put(race.getId(), race);
        savedRaces.add(race);
        return race;
    }

    @Override
    public List<Race> findAll() {
        return List.copyOf(races.values());
    }

    @Override
    public Optional<Race> findById(RaceId RaceId) {
        return Optional.ofNullable(races.get(RaceId));
    }

    @Override
    public Race findOrCreate(Race race) {
        return races.values()
            .stream()
            .filter(it -> Objects.equals(it.getRaceName(), race.getRaceName()) &&
                          Objects.equals(it.getRaceNumber(), race.getRaceNumber()))
            .findAny()
            .orElseGet(() -> save(race));
    }

    @Override
    public Collection<Race> findOrCreate(Collection<Race> races) {
        return races.stream().map(this::findOrCreate).toList();
    }

    @Override
    public List<Race> findAllByEvents(List<Event> events) {
        Set<EventId> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        return races.values().stream().filter(race -> eventIds.contains(race.getEventId())).toList();
    }

    @SuppressWarnings("unused")
    public List<Race> savedRaces() {
        return savedRaces;
    }

    @SuppressWarnings("unused")
    public int saveCount() {
        return savedRaces.size();
    }

    @SuppressWarnings("unused")
    public void resetSaveCount() {
        savedRaces.clear();
    }

}
