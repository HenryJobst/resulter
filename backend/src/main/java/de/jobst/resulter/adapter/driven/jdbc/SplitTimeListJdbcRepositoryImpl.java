package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.SplitTime;
import de.jobst.resulter.domain.SplitTimeListId;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class SplitTimeListJdbcRepositoryImpl
    implements SplitTimeListJdbcRepositoryCustom {

    private final NamedParameterJdbcTemplate jdbc;

    public SplitTimeListJdbcRepositoryImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Collection<SplitTimeListDbo> findByResultListIdOptimized(Long resultListId) {

        // 1. Load parent rows
        List<SplitTimeListDbo> lists = jdbc.query("""
                                                  SELECT
                                                      id,
                                                      event_id,
                                                      result_list_id,
                                                      person_id,
                                                      class_result_short_name,
                                                      race_number
                                                  FROM split_time_list
                                                  WHERE result_list_id = :resultListId
                                                  """, Map.of("resultListId", resultListId), splitTimeListRowMapper());

        if (lists.isEmpty()) {
            return List.of();
        }

        // 2. Load all children in one query
        Set<Long> listIds =
            lists.stream().map(SplitTimeListDbo::getId)
                .filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());

        Map<Long, List<SplitTimeDbo>> splitTimesByListId = jdbc.query("""
                                                                      SELECT
                                                                          split_time_list_id,
                                                                          control_code,
                                                                          punch_time
                                                                      FROM split_time
                                                                      WHERE split_time_list_id IN (:ids)
                                                                      """, Map.of("ids", listIds), splitTimeRowMapper())
            .stream()
            .collect(Collectors.groupingBy(SplitTimeDbo::getSplitTimeListId));

        // 3. Attach children to parents
        lists.forEach(list -> list.setSplitTimes(new HashSet<>(splitTimesByListId.getOrDefault(list.getId(),
            List.of()))));

        return lists;
    }

    private RowMapper<SplitTimeListDbo> splitTimeListRowMapper() {
        return (rs, rowNum) -> {
            SplitTimeListDbo dbo = new SplitTimeListDbo(
                AggregateReference.to(rs.getLong("event_id")),
                AggregateReference.to(rs.getLong("result_list_id")),
                rs.getString("class_result_short_name"),
                AggregateReference.to(rs.getLong("person_id")),
                rs.getByte("race_number"),
                Set.of()
            );
            dbo.setId(rs.getLong("id"));
            return dbo;
        };
    }

    private RowMapper<SplitTimeDbo> splitTimeRowMapper() {
        return (rs, rowNum) -> SplitTimeDbo.from(
            SplitTime.of(
            rs.getString("control_code"),
            rs.getDouble("punch_time"),
            SplitTimeListId.of(
            rs.getLong("split_time_list_id"))));
    }
}
