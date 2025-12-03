package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface SplitTimeListJdbcRepository
    extends CrudRepository<SplitTimeListDbo, Long>, PagingAndSortingRepository<SplitTimeListDbo, Long> {

    Collection<SplitTimeListDbo> findAll();

    Optional<SplitTimeListDbo> findByEventIdAndResultListIdAndClassResultShortNameAndPersonIdAndRaceNumber(
        AggregateReference<EventDbo, Long> eventId,
        AggregateReference<ResultListDbo, Long> resultListId,
        String classResultShortName,
        AggregateReference<PersonDbo, Long> personId,
        Byte raceNumber);

    Collection<SplitTimeListDbo> findByResultListId(AggregateReference<ResultListDbo, Long> resultListId);

    @Query("""
    SELECT COUNT(s) > 0
    FROM split_time_list s
    WHERE s.person_id = :oldPersonId
    """)
    boolean existsByPersonId(@Param("oldPersonId") Long oldPersonId);

    @Modifying
    @Query("""
        UPDATE split_time_list
        SET person_id = :newPersonId
        WHERE person_id = :oldPersonId;
        """)
    int replacePersonIdInSplitTimeList(@Param("oldPersonId") Long oldPersonId,
                                        @Param("newPersonId") Long newPersonId);
}
