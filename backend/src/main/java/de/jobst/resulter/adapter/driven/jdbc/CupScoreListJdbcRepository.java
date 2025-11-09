package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CupScoreListJdbcRepository extends CrudRepository<CupScoreListDbo, Long> {

    List<CupScoreListDbo> findByResultListId(AggregateReference<ResultListDbo, Long> resultListId);
    List<CupScoreListDbo> findByResultListIdAndCupId(AggregateReference<ResultListDbo, Long> resultListId,
                                                     AggregateReference<CupDbo, Long> cupId);

    @Query("""
    SELECT COUNT(s) > 0
    FROM cup_score s
    WHERE s.person_id = :oldPersonId
    """)
    boolean existsByPersonId(@Param("oldPersonId") Long oldPersonId);

    @Modifying
    @Query("""
        UPDATE cup_score
        SET person_id = :newPersonId
        WHERE person_id = :oldPersonId;
        """)
    long replacePersonIdInCupScore(@Param("oldPersonId") Long oldPersonId,
                                  @Param("newPersonId") Long newPersonId);
}
