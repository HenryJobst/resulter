package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.SplitTime;
import de.jobst.resulter.domain.SplitTimeListId;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class SplitTimeListJdbcRepositoryImpl implements SplitTimeListJdbcRepositoryCustom {

    private final JdbcClient jdbcClient;

    public SplitTimeListJdbcRepositoryImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Collection<SplitTimeListDbo> findByResultListIdOptimized(Long resultListId) {

        List<SplitTimeListDbo> lists = jdbcClient
                .sql(
                        """
                SELECT
                    id,
                    event_id,
                    result_list_id,
                    person_id,
                    class_result_short_name,
                    race_number
                FROM split_time_list
                WHERE result_list_id = :resultListId
                """)
                .param("resultListId", resultListId)
                .query(splitTimeListRowMapper())
                .list();

        if (lists.isEmpty()) {
            return List.of();
        }

        Set<Long> listIds = lists.stream().map(SplitTimeListDbo::getId).collect(Collectors.toUnmodifiableSet());

        Map<Long, List<SplitTimeDbo>> splitTimesByListId = jdbcClient
                .sql(
                        """
                SELECT
                    split_time_list_id,
                    control_code,
                    punch_time
                FROM split_time
                WHERE split_time_list_id IN (:ids)
                """)
                .param("ids", listIds)
                .query(splitTimeRowMapper())
                .list()
                .stream()
                .collect(Collectors.groupingBy(SplitTimeDbo::getSplitTimeListId));

        lists.forEach(
                list -> list.setSplitTimes(new HashSet<>(splitTimesByListId.getOrDefault(list.getId(), List.of()))));

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
                    Set.of());
            dbo.setId(rs.getLong("id"));
            return dbo;
        };
    }

    private RowMapper<SplitTimeDbo> splitTimeRowMapper() {
        return (rs, rowNum) -> SplitTimeDbo.from(SplitTime.of(
                rs.getString("control_code"),
                rs.getDouble("punch_time"),
                SplitTimeListId.of(rs.getLong("split_time_list_id"))));
    }
}
