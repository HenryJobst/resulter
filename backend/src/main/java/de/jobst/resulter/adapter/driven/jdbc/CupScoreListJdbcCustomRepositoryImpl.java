package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.util.BatchUtils;
import de.jobst.resulter.domain.CupScoreList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

        jdbcClient.sql(sql.toString()).params(params).update();
    }

    @Override
    @Transactional
    public void deleteAllByEventId(Long eventId) {
        String sql =
                """
            DELETE FROM cup_score_list
            WHERE result_list_id IN (
                SELECT id FROM result_list WHERE event_id = :eventId
            )
            """;

        jdbcClient.sql(sql).param("eventId", eventId).update();
    }

    @Override
    public List<CupScoreListDbo> findByResultListIdWithoutCupScores(Long resultListId) {
        String query =
                """
            SELECT id, cup_id, result_list_id, creator, create_time, create_time_zone, status
            FROM cup_score_list
            WHERE result_list_id = :resultListId
            """;

        return jdbcClient
                .sql(query)
                .param("resultListId", resultListId)
                .query(new CupScoreListDboRowMapper())
                .list();
    }

    @Override
    public List<CupScoreListDbo> findByResultListIdAndCupIdWithoutCupScores(Long resultListId, Long cupId) {
        String query =
                """
            SELECT id, cup_id, result_list_id, creator, create_time, create_time_zone, status
            FROM cup_score_list
            WHERE result_list_id = :resultListId AND cup_id = :cupId
            """;

        return jdbcClient
                .sql(query)
                .param("resultListId", resultListId)
                .param("cupId", cupId)
                .query(new CupScoreListDboRowMapper())
                .list();
    }

    @Override
    public List<CupScoreWithListId> findCupScoresByListIds(Collection<Long> cupScoreListIds) {
        if (cupScoreListIds == null || cupScoreListIds.isEmpty()) {
            return Collections.emptyList();
        }

        String query =
                """
            SELECT cup_score_list_id, person_id, organisation_id, class_result_short_name, score
            FROM cup_score
            WHERE cup_score_list_id IN (:listIds)
            ORDER BY cup_score_list_id, score DESC
            """;

        return jdbcClient
                .sql(query)
                .param("listIds", cupScoreListIds)
                .query(new CupScoreWithListIdRowMapper())
                .list();
    }

    /**
     * RowMapper for CupScoreListDbo that does not load the MappedCollection (cupScores).
     */
    private static class CupScoreListDboRowMapper implements RowMapper<CupScoreListDbo> {
        @Override
        public CupScoreListDbo mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            Long cupId = rs.getLong("cup_id");
            Long resultListId = rs.getLong("result_list_id");
            String creator = rs.getString("creator");
            Timestamp createTime = rs.getTimestamp("create_time");
            String createTimeZone = rs.getString("create_time_zone");
            String status = rs.getString("status");

            CupScoreListDbo dbo = new CupScoreListDbo(
                    AggregateReference.to(cupId),
                    AggregateReference.to(resultListId),
                    creator,
                    createTime,
                    createTimeZone);

            // Use @With annotation to set id
            CupScoreListDbo dboWithId = dbo.withId(id);
            dboWithId.setStatus(status);

            // cupScores will be populated later by batch loading
            dboWithId.setCupScores(new HashSet<>());

            return dboWithId;
        }
    }

    /**
     * RowMapper for CupScoreDbo with parent cup_score_list_id.
     */
    private static class CupScoreWithListIdRowMapper implements RowMapper<CupScoreWithListId> {
        @Override
        public CupScoreWithListId mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long cupScoreListId = rs.getLong("cup_score_list_id");
            Long personId = rs.getLong("person_id");
            Long organisationId = rs.getLong("organisation_id");
            String classResultShortName = rs.getString("class_result_short_name");
            Double score = rs.getDouble("score");

            AggregateReference<OrganisationDbo, Long> orgRef =
                    rs.wasNull() ? null : AggregateReference.to(organisationId);

            CupScoreDbo cupScoreDbo =
                    new CupScoreDbo(AggregateReference.to(personId), orgRef, classResultShortName, score);

            return new CupScoreWithListId(cupScoreListId, cupScoreDbo);
        }
    }
}
