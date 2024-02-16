package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.RaceRepository;
import de.jobst.resulter.domain.Race;
import de.jobst.resulter.domain.RaceId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class RaceRepositoryDataJdbcAdapter implements RaceRepository {

    private final RaceJdbcRepository raceJdbcRepository;

    public RaceRepositoryDataJdbcAdapter(RaceJdbcRepository raceJdbcRepository) {
        this.raceJdbcRepository = raceJdbcRepository;
    }

    @Override
    public Race save(Race race) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setRaceDboResolver(id -> raceJdbcRepository.findById(id.value()).orElseThrow());
        RaceDbo raceEntity = RaceDbo.from(race, dboResolvers);
        RaceDbo savedRaceEntity = raceJdbcRepository.save(raceEntity);
        return savedRaceEntity.asRace();
    }

    @Override
    public List<Race> findAll() {
        return raceJdbcRepository.findAll().stream().map(RaceDbo::asRace).sorted().toList();
    }

    @Override
    public Optional<Race> findById(RaceId raceId) {
        Optional<RaceDbo> raceEntity = raceJdbcRepository.findById(raceId.value());
        return raceEntity.map(RaceDbo::asRace);
    }

    @Override
    public Race findOrCreate(Race race) {
        Optional<RaceDbo> raceEntity =
            raceJdbcRepository.findByEventIdAndNumber(AggregateReference.to(race.getEventId().value()),
                race.getRaceNumber().value());
        if (raceEntity.isEmpty()) {
            return save(race);
        }
        RaceDbo entity = raceEntity.get();
        return entity.asRace();
    }

    @Override
    @Transactional
    public Collection<Race> findOrCreate(Collection<Race> races) {
        return races.stream().map(this::findOrCreate).toList();
    }
}
