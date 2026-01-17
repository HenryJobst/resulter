package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.ClassResultShortName;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.RaceNumber;
import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.SplitTimeList;
import de.jobst.resulter.domain.SplitTimeListId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
@Slf4j
public class SplitTimeListRepositoryDataJdbcAdapter implements SplitTimeListRepository {

    private final SplitTimeListJdbcRepository splitTimeListJdbcRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SplitTimeListRepositoryDataJdbcAdapter(
            SplitTimeListJdbcRepository splitTimeListJdbcRepository,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.splitTimeListJdbcRepository = splitTimeListJdbcRepository;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public @Nullable SplitTimeList save(SplitTimeList splitTimeList) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setSplitTimeListDboResolver(
                id -> splitTimeListJdbcRepository.findById(id.value()).orElseThrow());
        SplitTimeListDbo splitTimeListEntity = SplitTimeListDbo.from(splitTimeList, dboResolvers);
        SplitTimeListDbo savedSplitTimeListEntity = splitTimeListJdbcRepository.save(splitTimeListEntity);
        return SplitTimeListDbo.asSplitTimeLists(List.of(savedSplitTimeListEntity)).stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<SplitTimeList> findAll() {
        return SplitTimeListDbo.asSplitTimeLists(splitTimeListJdbcRepository.findAll()).stream()
                .sorted()
                .toList();
    }

    @Override
    public Optional<SplitTimeList> findById(SplitTimeListId splitTimeListId) {
        Optional<SplitTimeListDbo> splitTimeListEntity = splitTimeListJdbcRepository.findById(splitTimeListId.value());
        return splitTimeListEntity.isPresent()
                ? SplitTimeListDbo.asSplitTimeLists(List.of(splitTimeListEntity.orElse(null))).stream()
                        .findFirst()
                : Optional.empty();
    }

    @Override
    public @Nullable SplitTimeList findOrCreate(SplitTimeList splitTimeList) {
        Optional<SplitTimeListDbo> splitTimeListEntity =
                splitTimeListJdbcRepository.findByEventIdAndResultListIdAndClassResultShortNameAndPersonIdAndRaceNumber(
                        AggregateReference.to(splitTimeList.getEventId().value()),
                        AggregateReference.to(splitTimeList.getResultListId().value()),
                        splitTimeList.getClassResultShortName().value(),
                        AggregateReference.to(splitTimeList.getPersonId().value()),
                        splitTimeList.getRaceNumber().value());
        if (splitTimeListEntity.isEmpty()) {
            return save(splitTimeList);
        }
        SplitTimeListDbo entity = splitTimeListEntity.get();
        return SplitTimeListDbo.asSplitTimeLists(List.of(entity)).stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional
    public Collection<@Nullable SplitTimeList> findOrCreate(Collection<SplitTimeList> splitTimeLists) {
        if (splitTimeLists.isEmpty()) {
            return List.of();
        }

        Map<SplitTimeList.DomainKey, SplitTimeList> existingSplitTimeLists =
                batchFindExistingSplitTimeLists(splitTimeLists);

        List<SplitTimeList> results = new ArrayList<>();
        List<SplitTimeList> toCreate = new ArrayList<>();

        for (SplitTimeList splitTimeList : splitTimeLists) {
            SplitTimeList.DomainKey key = splitTimeList.getDomainKey();
            SplitTimeList existing = existingSplitTimeLists.get(key);
            if (existing != null) {
                results.add(existing);
            } else {
                toCreate.add(splitTimeList);
            }
        }

        if (!toCreate.isEmpty()) {
            List<SplitTimeList> created = batchInsertSplitTimeLists(toCreate);
            results.addAll(created);
        }

        return results;
    }

    private Map<SplitTimeList.DomainKey, SplitTimeList> batchFindExistingSplitTimeLists(
            Collection<SplitTimeList> splitTimeLists) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, event_id, result_list_id, class_result_short_name, person_id, race_number FROM split_time_list WHERE ");
        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> conditions = new ArrayList<>();

        int idx = 0;
        for (SplitTimeList splitTimeList : splitTimeLists) {
            Long eventId = splitTimeList.getEventId().value();
            Long resultListId = splitTimeList.getResultListId().value();
            String classResultShortName =
                    splitTimeList.getClassResultShortName().value();
            Long personId = splitTimeList.getPersonId().value();
            Byte raceNumber = splitTimeList.getRaceNumber().value();

            StringBuilder condition = new StringBuilder();
            condition.append("(event_id = :e").append(idx);
            condition.append(" AND result_list_id = :rl").append(idx);
            condition.append(" AND class_result_short_name = :cs").append(idx);
            condition.append(" AND person_id = :p").append(idx);
            condition.append(" AND race_number = :rn").append(idx);
            condition.append(")");

            params.addValue("e" + idx, eventId);
            params.addValue("rl" + idx, resultListId);
            params.addValue("cs" + idx, classResultShortName);
            params.addValue("p" + idx, personId);
            params.addValue("rn" + idx, raceNumber != null ? raceNumber : (byte) 1);

            conditions.add(condition.toString());
            idx++;
        }

        sql.append(String.join(" OR ", conditions));

        List<SplitTimeList> found = namedParameterJdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> {
            Long id = rs.getLong("id");
            Long eventId = rs.getLong("event_id");
            Long resultListId = rs.getLong("result_list_id");
            String classResultShortName = rs.getString("class_result_short_name");
            Long personId = rs.getLong("person_id");
            Byte raceNumber = rs.getByte("race_number");

            return new SplitTimeList(
                    SplitTimeListId.of(id),
                    EventId.of(eventId),
                    ResultListId.of(resultListId),
                    ClassResultShortName.of(classResultShortName),
                    PersonId.of(personId),
                    RaceNumber.of(raceNumber),
                    List.of());
        });

        return found.stream().collect(Collectors.toMap(SplitTimeList::getDomainKey, s -> s));
    }

    private List<SplitTimeList> batchInsertSplitTimeLists(List<SplitTimeList> splitTimeLists) {
        List<SplitTimeList> created = new ArrayList<>();
        for (SplitTimeList splitTimeList : splitTimeLists) {
            SplitTimeList saved = save(splitTimeList);
            created.add(saved);
        }
        return created;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void replacePersonId(PersonId oldPersonId, PersonId newPersonId) {
        if (splitTimeListJdbcRepository.existsByPersonId(oldPersonId.value())) {
            int updatedRows = splitTimeListJdbcRepository.replacePersonIdInSplitTimeList(
                    oldPersonId.value(), newPersonId.value());
            log.debug(
                    "Updated {} rows in split_time_list with person_id {} to person_id {}",
                    updatedRows,
                    oldPersonId,
                    newPersonId);
        }
    }

    @Override
    public List<SplitTimeList> findByResultListId(de.jobst.resulter.domain.ResultListId resultListId) {
        Collection<SplitTimeListDbo> splitTimeListDbos =
                splitTimeListJdbcRepository.findByResultListIdOptimized(resultListId.value());
        return SplitTimeListDbo.asSplitTimeLists(splitTimeListDbos).stream().toList();
    }
}
