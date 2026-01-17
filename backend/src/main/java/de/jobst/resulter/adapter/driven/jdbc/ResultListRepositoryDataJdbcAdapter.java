package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.domain.*;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
@Slf4j
public class ResultListRepositoryDataJdbcAdapter implements ResultListRepository {

    private final ResultListJdbcRepository resultListJdbcRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ResultListRepositoryDataJdbcAdapter(
            ResultListJdbcRepository resultListJdbcRepository, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.resultListJdbcRepository = resultListJdbcRepository;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @SuppressWarnings("unused")
    private static Throwable getRootCause(Throwable t) {
        Throwable rootCause = NestedExceptionUtils.getRootCause(t);
        return rootCause != null ? rootCause : t;
    }

    @Override
    @Transactional
    public @Nullable ResultList save(ResultList resultList) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setResultListDboResolver(
                id -> resultListJdbcRepository.findById(id.value()).orElseThrow());
        ResultListDbo resultListDbo = ResultListDbo.from(resultList, dboResolvers);
        ResultListDbo savedResultListEntity = resultListJdbcRepository.save(resultListDbo);
        return ResultListDbo.asResultLists(List.of(savedResultListEntity)).stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultList> findAll() {
        return ResultListDbo.asResultLists(resultListJdbcRepository.findAll()).stream()
                .sorted()
                .toList();
    }

    @Override
    @Transactional
    public @Nullable ResultList findOrCreate(ResultList resultList) {
        Optional<ResultListId> resultListId = resultListJdbcRepository.findResultListIdByDomainKey(
                resultList.getEventId().value(),
                resultList.getRaceId().value(),
                resultList.getCreator(),
                resultList.getCreateTime() != null
                        ? Timestamp.from(
                                resultList.getCreateTime().toOffsetDateTime().toInstant())
                        : null,
                resultList.getCreateTime() != null
                        ? resultList.getCreateTime().getZone().getId()
                        : null);
        return resultListId.map(listId -> findById(listId).orElseThrow()).orElseGet(() -> save(resultList));
    }

    @Override
    @Transactional
    public Collection<ResultList> findOrCreate(Collection<ResultList> resultLists) {
        if (resultLists.isEmpty()) {
            return List.of();
        }

        Map<ResultList.DomainKey, ResultList> existingResultLists = batchFindExistingResultLists(resultLists);

        List<ResultList> results = new ArrayList<>();
        List<ResultList> toCreate = new ArrayList<>();

        for (ResultList resultList : resultLists) {
            ResultList.DomainKey key = resultList.getDomainKey();
            ResultList existing = existingResultLists.get(key);
            if (existing != null) {
                results.add(existing);
            } else {
                toCreate.add(resultList);
            }
        }

        if (!toCreate.isEmpty()) {
            List<ResultList> created = batchInsertResultLists(toCreate);
            results.addAll(created);
        }

        return results;
    }

    private Map<ResultList.DomainKey, ResultList> batchFindExistingResultLists(Collection<ResultList> resultLists) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, event_id, race_id, creator, create_time, create_time_zone, status FROM result_list WHERE ");
        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> conditions = new ArrayList<>();

        int idx = 0;
        for (ResultList resultList : resultLists) {
            Long eventId = resultList.getEventId().value();
            Long raceId = resultList.getRaceId().value();
            String creator = resultList.getCreator();
            ZonedDateTime createTime = resultList.getCreateTime();

            StringBuilder condition = new StringBuilder();
            condition.append("(event_id = :e").append(idx);
            condition.append(" AND race_id = :r").append(idx);

            params.addValue("e" + idx, eventId);
            params.addValue("r" + idx, raceId);

            if (creator != null) {
                condition.append(" AND creator = :c").append(idx);
                params.addValue("c" + idx, creator);
            } else {
                condition.append(" AND creator IS NULL");
            }

            if (createTime != null) {
                condition.append(" AND create_time = :t").append(idx);
                condition.append(" AND create_time_zone = :z").append(idx);
                params.addValue(
                        "t" + idx, Timestamp.from(createTime.toOffsetDateTime().toInstant()));
                params.addValue("z" + idx, createTime.getZone().getId());
            } else {
                condition.append(" AND create_time IS NULL");
            }

            condition.append(")");
            conditions.add(condition.toString());
            idx++;
        }

        sql.append(String.join(" OR ", conditions));

        List<ResultList> found = namedParameterJdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> {
            Long id = rs.getLong("id");
            Long eventId = rs.getLong("event_id");
            Long raceId = rs.getLong("race_id");
            String creator = rs.getString("creator");
            Timestamp createTime = rs.getTimestamp("create_time");
            String createTimeZone = rs.getString("create_time_zone");
            String status = rs.getString("status");

            ZonedDateTime zonedCreateTime = null;
            if (createTime != null && createTimeZone != null) {
                zonedCreateTime = createTime.toInstant().atZone(ZoneId.of(createTimeZone));
            }

            return new ResultList(
                    ResultListId.of(id),
                    EventId.of(eventId),
                    RaceId.of(raceId),
                    creator,
                    zonedCreateTime,
                    status,
                    null);
        });

        return found.stream().collect(Collectors.toMap(ResultList::getDomainKey, r -> r));
    }

    private List<ResultList> batchInsertResultLists(List<ResultList> resultLists) {
        List<ResultList> created = new ArrayList<>();
        for (ResultList resultList : resultLists) {
            ResultList saved = save(resultList);
            created.add(saved);
        }
        return created;
    }

    @Override
    @Transactional
    public ResultList update(ResultList resultList) {
        return save(resultList);
    }

    @Override
    public Collection<ResultList> findByEventId(EventId id) {
        Collection<PersonRaceResultJdbcDto> personRaceResultJdbcDtos =
                resultListJdbcRepository.findPersonRaceResultsByEventId(id.value());

        return PersonRaceResultJdbcDto.asResultLists(personRaceResultJdbcDtos);
    }

    @Override
    public Optional<ResultList> findById(ResultListId resultListId) {
        Collection<PersonRaceResultJdbcDto> personRaceResultJdbcDtos =
                resultListJdbcRepository.findPersonRaceResultsByResultListId(resultListId.value());
        if (personRaceResultJdbcDtos.isEmpty()) {
            return Optional.empty();
        }
        return PersonRaceResultJdbcDto.asResultLists(personRaceResultJdbcDtos).stream()
                .findFirst();
    }

    @Override
    public @Nullable ResultList findByResultListIdAndClassResultShortNameAndPersonId(
            ResultListId resultListId, ClassResultShortName classResultShortName, PersonId personId) {
        List<PersonRaceResultJdbcDto> personRaceResults =
                resultListJdbcRepository.findPersonRaceResultByResultListIdAndClassResultShortNameAndPersonId(
                        resultListId.value(), classResultShortName.value(), personId.value());
        if (personRaceResults.isEmpty()) {
            return null;
        }
        return Optional.of(personRaceResults.getFirst())
                .flatMap(personRaceResultJdbcDto ->
                        PersonRaceResultJdbcDto.asResultLists(List.of(personRaceResultJdbcDto)).stream()
                                .findFirst())
                .orElse(null);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void replacePersonId(PersonId oldPersonId, PersonId newPersonId) {
        // 1. Clone parent rows (person_result) for the new person
        long clonedRows = resultListJdbcRepository.cloneResultsForPerson(oldPersonId.value(), newPersonId.value());
        log.debug("Cloned {} rows in person_result from person_id {} to {}", clonedRows, oldPersonId, newPersonId);

        // 2. Update child rows (person_race_result) to point to new person
        long updatedChildRows =
                resultListJdbcRepository.replacePersonIdInPersonRaceResult(oldPersonId.value(), newPersonId.value());
        log.debug(
                "Updated {} rows in person_race_result from person_id {} to {}",
                updatedChildRows,
                oldPersonId,
                newPersonId);

        // 3. Delete old parent rows
        long deletedRows = resultListJdbcRepository.deleteByPersonId(oldPersonId.value());
        log.debug("Deleted {} old rows in person_result with person_id {}", deletedRows, oldPersonId);
    }
}
