package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Race;
import de.jobst.resulter.domain.RaceId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.Repository;

@Repository
public interface RaceRepository {

    Race save(Race event);

    List<Race> findAll();

    Optional<Race> findById(RaceId RaceId);

    Race findOrCreate(Race race);

    Collection<Race> findOrCreate(Collection<Race> races);

    List<Race> findAllByEventIds(List<EventId> eventIds);
}
