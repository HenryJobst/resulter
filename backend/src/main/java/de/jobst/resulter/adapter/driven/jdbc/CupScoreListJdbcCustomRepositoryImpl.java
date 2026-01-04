package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.CupScoreList;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public class CupScoreListJdbcCustomRepositoryImpl implements CupScoreListJdbcCustomRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public CupScoreListJdbcCustomRepositoryImpl(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    @Transactional
    public void deleteAllByDomainKeys(Set<CupScoreList.DomainKey> domainKeys) {
        String sql =
            "DELETE FROM cup_score_list WHERE cup_id = :cupId AND result_list_id = :resultListId AND LOWER(status) = " +
            "LOWER(:status)";

        List<MapSqlParameterSource> batchParams = domainKeys.stream().map(tuple -> {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("cupId", tuple.cupId().value());
            params.addValue("resultListId", tuple.resultListId().value());
            params.addValue("status", tuple.status());
            return params;
        }).toList();

        namedJdbcTemplate.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
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

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("eventId", eventId);

        namedJdbcTemplate.update(sql, params);
    }
}
