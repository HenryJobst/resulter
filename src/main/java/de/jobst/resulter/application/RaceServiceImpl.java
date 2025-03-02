package de.jobst.resulter.application;

import de.jobst.resulter.application.port.RaceRepository;
import de.jobst.resulter.application.port.RaceService;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Race;
import de.jobst.resulter.domain.RaceId;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class RaceServiceImpl implements RaceService {

    private final RaceRepository raceRepository;

    public RaceServiceImpl(RaceRepository raceRepository) {
        this.raceRepository = raceRepository;
    }

    @Override
    public Race findOrCreate(Race race) {
        return raceRepository.findOrCreate(race);
    }

    @Override
    public Optional<Race> findById(RaceId raceId) {
        return raceRepository.findById(raceId);
    }

    @Override
    public List<Race> findAll() {
        return raceRepository.findAll();
    }

    @Override
    public Collection<Race> findOrCreate(Collection<Race> races) {
        return raceRepository.findOrCreate(races);
    }

    @Override
    public List<Race> findAllByEventIds(List<EventId> eventIds) {
        return raceRepository.findAllByEventIds(eventIds);
    }
}
