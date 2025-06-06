package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.ResultListId;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResultListJdbcRepository extends CrudRepository<ResultListDbo, Long> {

    @NonNull
    Collection<ResultListDbo> findAll();

    @Query("""
           SELECT DISTINCT 
               rl.event_id, rl.id AS result_list_id, rl.race_id, rl.create_time, rl.create_time_zone, rl.status AS result_list_status,
               cl.short_name AS class_list_short_name, cl.name AS class_list_name, cl.gender AS class_gender, cl.course_id,
               pr.person_id, pr.organisation_id,
               prr.start_time, prr.start_time_zone, prr.punch_time, prr.position, prr.race_number, prr.state
           FROM person_race_result prr
               INNER JOIN result_list rl ON rl.id = prr.result_list_id and rl.id = :resultListId
               INNER JOIN class_result cl ON prr.result_list_id = cl.result_list_id and prr.class_result_short_name = cl.short_name
               INNER JOIN person_result pr ON prr.result_list_id = pr.result_list_id and prr.class_result_short_name = pr.class_result_short_name and prr.person_id = pr.person_id
           WHERE prr.state != 'DID_NOT_START'
           ORDER BY rl.id, cl.short_name, prr.race_number, prr.position, pr.person_id;
           """)
    Collection<PersonRaceResultJdbcDto> findPersonRaceResultsByResultListId(@Param("resultListId") Long resultListId);

    @Query("""
           SELECT
               rl.event_id, rl.id AS result_list_id, rl.race_id, rl.create_time, rl.create_time_zone, rl.status AS result_list_status,
               cl.short_name AS class_list_short_name, cl.name AS class_list_name, cl.gender AS class_gender, cl.course_id,
               pr.person_id, pr.organisation_id,
               prr.start_time, prr.start_time_zone, prr.punch_time, prr.position, prr.race_number, prr.state
           FROM person_race_result prr
               INNER JOIN result_list rl ON rl.id = prr.result_list_id and rl.event_id = :eventId
               INNER JOIN class_result cl ON prr.result_list_id = cl.result_list_id and prr.class_result_short_name = cl.short_name
               INNER JOIN person_result pr ON prr.result_list_id = pr.result_list_id and prr.class_result_short_name = pr.class_result_short_name and prr.person_id = pr.person_id
           WHERE prr.state != 'DID_NOT_START'
           ORDER BY rl.id, cl.short_name, prr.race_number, prr.position, pr.person_id;
           """)
    Collection<PersonRaceResultJdbcDto> findPersonRaceResultsByEventId(@Param("eventId") Long eventId);

    @Query("""
           SELECT
               rl.event_id, rl.id AS result_list_id, rl.race_id, rl.create_time, rl.create_time_zone, rl.status AS result_list_status,
               cl.short_name AS class_list_short_name, cl.name AS class_list_name, cl.gender AS class_gender, cl.course_id,
               pr.person_id, pr.organisation_id,
               prr.start_time, prr.start_time_zone, prr.punch_time, prr.position, prr.race_number, prr.state
           FROM person_race_result prr
               INNER JOIN result_list rl ON rl.id = prr.result_list_id and rl.id = :resultListId
               INNER JOIN class_result cl ON prr.result_list_id = cl.result_list_id and prr.class_result_short_name = cl.short_name and cl.short_name = :classResultShortName
               INNER JOIN person_result pr ON prr.result_list_id = pr.result_list_id and prr.class_result_short_name = pr.class_result_short_name and prr.person_id = pr.person_id and pr.person_id = :personId
           WHERE prr.state != 'DID_NOT_START'
           ORDER BY rl.id, prr.race_number, cl.short_name, prr.race_number, prr.position, pr.person_id;
           """)
    List<PersonRaceResultJdbcDto> findPersonRaceResultByResultListIdAndClassResultShortNameAndPersonId(
        @Param("resultListId") Long resultListId,
        @Param("classResultShortName") String classResultShortName,
        @Param("personId") Long personId);

    @Query("""
           SELECT
           rl.id as value
           FROM result_list rl
           WHERE rl.event_id = :eventId
           AND rl.race_id = :raceId
           AND rl.creator = :creator
           AND rl.create_time = :createTime
           AND rl.create_time_zone = :createTimeZone
           """)
    Optional<ResultListId> findResultListIdByDomainKey(@Param("eventId") Long eventId,
                                                       @Param("raceId") Long raceId,
                                                       @Param("creator") String creator,
                                                       @Param("createTime") Timestamp createTime,
                                                       @Param("createTimeZone") String createTimeZone);

    @Query("""
    SELECT COUNT(p) > 0
    FROM person_result p
    WHERE p.person_id = :oldPersonId
    """)
    boolean existsByPersonId(@Param("oldPersonId") Long oldPersonId);

    @Modifying
    @Query("""
        UPDATE person_result
        SET person_id = :newPersonId
        WHERE person_id = :oldPersonId;
        """)
    long replacePersonIdInPersonResult(@Param("oldPersonId") Long oldPersonId,
                                       @Param("newPersonId") Long newPersonId);

    @Query("""
    SELECT COUNT(p) > 0
    FROM person_race_result p
    WHERE p.person_id = :oldPersonId
    """)
    boolean existsByPersonIdInPersonRaceResult(@Param("oldPersonId") Long oldPersonId);

    @Modifying
    @Query("""
        UPDATE person_race_result
        SET person_id = :newPersonId
        WHERE person_id = :oldPersonId;
        """)
    long replacePersonIdInPersonRaceResult(@Param("oldPersonId") Long oldPersonId,
                                           @Param("newPersonId") Long newPersonId);

}
