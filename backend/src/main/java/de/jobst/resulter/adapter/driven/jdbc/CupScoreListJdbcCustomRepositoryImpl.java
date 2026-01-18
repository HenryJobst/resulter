package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.util.BatchUtils;
import de.jobst.resulter.domain.CupScoreList;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class CupScoreListJdbcCustomRepositoryImpl implements CupScoreListJdbcCustomRepository {

    private final JdbcClient jdbcClient;

    public CupScoreListJdbcCustomRepositoryImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    @Transactional
    public void deleteAllByDomainKeys(Set<CupScoreList.DomainKey> domainKeys) {
        BatchUtils.processInBatches(domainKeys, this::deleteBatch);
    }

    private void deleteBatch(List<CupScoreList.DomainKey> batch) {
        StringBuilder sql = new StringBuilder("DELETE FROM cup_score_list WHERE ");
        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();

        int idx = 0;
        for (CupScoreList.DomainKey tuple : batch) {
            String condition = "(cup_id = :c" + idx
                    + " AND result_list_id = :r" + idx
                    + " AND LOWER(status) = LOWER(:s" + idx + "))";
            conditions.add(condition);

            params.put("c" + idx, tuple.cupId().value());
            params.put("r" + idx, tuple.resultListId().value());
            params.put("s" + idx, tuple.status());
            idx++;
        }

        sql.append(String.join(" OR ", conditions));

        jdbcClient.sql(sql.toString())
                .params(params)
                .update();
    }

    @Override
    @Transactional
    public void deleteAllByEventId(Long eventId) {
        String sql = """
            DELETE FROM cup_score_list
            WHERE result_list_id IN (
                SELECT id FROM result_list WHERE event_id = :eventId
            )
            """;

        jdbcClient.sql(sql)
            .param("eventId", eventId)
            .update();
    }
}
