package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Race;
import de.jobst.resulter.domain.RaceId;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@PrimaryPort
public interface RaceService {

    Race findOrCreate(Race race);

    Optional<Race> findById(RaceId raceId);

    List<Race> findAll();

    Collection<Race> findOrCreate(Collection<Race> races);

    List<Race> findAllByEventIds(List<EventId> eventIds);
}
