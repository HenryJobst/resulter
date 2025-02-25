package de.jobst.resulter.application;

import de.jobst.resulter.application.port.RaceRepository;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Race;
import de.jobst.resulter.domain.RaceId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class RaceService {

    private final RaceRepository raceRepository;

    public RaceService(RaceRepository raceRepository) {
        this.raceRepository = raceRepository;
    }

    public Race findOrCreate(Race race) {
        return raceRepository.findOrCreate(race);
    }

    public Optional<Race> findById(RaceId raceId) {
        return raceRepository.findById(raceId);
    }

    public List<Race> findAll() {
        return raceRepository.findAll();
    }

    public Collection<Race> findOrCreate(Collection<Race> races) {
        return raceRepository.findOrCreate(races);
    }

    public List<Race> findAllByEventIds(List<EventId> eventIds) {
        return raceRepository.findAllByEventIds(eventIds);
    }
}
