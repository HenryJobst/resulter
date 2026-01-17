package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.RaceRepository;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Race;
import de.jobst.resulter.domain.RaceId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class RaceRepositoryDataJdbcAdapter implements RaceRepository {

    private final RaceJdbcRepository raceJdbcRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public RaceRepositoryDataJdbcAdapter(
            RaceJdbcRepository raceJdbcRepository, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.raceJdbcRepository = raceJdbcRepository;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Race save(Race race) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setRaceDboResolver(
                id -> raceJdbcRepository.findById(id.value()).orElseThrow());
        RaceDbo raceEntity = RaceDbo.from(race, dboResolvers);
        RaceDbo savedRaceEntity = raceJdbcRepository.save(raceEntity);
        return savedRaceEntity.asRace();
    }

    @Override
    public List<Race> findAll() {
        return raceJdbcRepository.findAll().stream()
                .map(RaceDbo::asRace)
                .sorted()
                .toList();
    }

    @Override
    public Optional<Race> findById(RaceId raceId) {
        Optional<RaceDbo> raceEntity = raceJdbcRepository.findById(raceId.value());
        return raceEntity.map(RaceDbo::asRace);
    }

    @Override
    public Race findOrCreate(Race race) {
        Optional<RaceDbo> raceEntity = raceJdbcRepository.findByEventIdAndNumber(
                AggregateReference.to(race.getEventId().value()),
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
        if (races.isEmpty()) {
            return List.of();
        }

        Map<Race.DomainKey, Race> existingRaces = batchFindExistingRaces(races);

        List<Race> results = new ArrayList<>();
        List<Race> toCreate = new ArrayList<>();

        for (Race race : races) {
            Race.DomainKey key = race.getDomainKey();
            Race existing = existingRaces.get(key);
            if (existing != null) {
                results.add(existing);
            } else {
                toCreate.add(race);
            }
        }

        if (!toCreate.isEmpty()) {
            List<Race> created = batchInsertRaces(toCreate);
            results.addAll(created);
        }

        return results;
    }

    private Map<Race.DomainKey, Race> batchFindExistingRaces(Collection<Race> races) {
        StringBuilder sql = new StringBuilder("SELECT * FROM race WHERE ");
        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> conditions = new ArrayList<>();

        int idx = 0;
        for (Race race : races) {
            Long eventId = race.getEventId().value();
            Byte number = race.getRaceNumber().value();

            String condition = "(event_id = :e" + idx + " AND number = :n" + idx + ")";
            conditions.add(condition);

            params.addValue("e" + idx, eventId);
            params.addValue("n" + idx, number);
            idx++;
        }

        sql.append(String.join(" OR ", conditions));

        List<RaceDbo> found = namedParameterJdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> {
            RaceDbo dbo = new RaceDbo(rs.getLong("event_id"), rs.getString("name"), rs.getByte("number"));
            dbo.setId(rs.getLong("id"));
            return dbo;
        });

        return found.stream().map(RaceDbo::asRace).collect(Collectors.toMap(Race::getDomainKey, r -> r));
    }

    private List<Race> batchInsertRaces(List<Race> races) {
        List<Race> created = new ArrayList<>();
        for (Race race : races) {
            Race saved = save(race);
            created.add(saved);
        }
        return created;
    }

    @Override
    public List<Race> findAllByEventIds(List<EventId> events) {
        return raceJdbcRepository
                .findAllByEventIdIn(events.stream()
                        .map(event -> AggregateReference.<EventDbo, Long>to(event.value()))
                        .toList())
                .stream()
                .map(RaceDbo::asRace)
                .toList();
    }
}
