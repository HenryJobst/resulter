package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.ResultListId;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResultListJdbcRepository extends CrudRepository<ResultListDbo, Long> {

    @NonNull
    Collection<ResultListDbo> findAll();

    @Query("""
           SELECT DISTINCT
           rl.event_id, rl.id as result_list_id, rl.race_id, rl.create_time, rl.create_time_zone, rl.status as result_list_status,
           cl.short_name as class_list_short_name, cl.name as class_list_name, cl.gender as class_gender, cl.course_id,
           pr.person_id, pr.organisation_id,
           prr.start_time, prr.start_time_zone, prr.punch_time, prr.position, prr.race_number, prr.state
           FROM result_list rl
           LEFT JOIN class_result cl ON rl.id = cl.result_list_id
           LEFT JOIN person_result pr ON rl.id = cl.result_list_id and pr.class_result_short_name = cl.short_name
           LEFT JOIN person_race_result prr ON rl.id = prr.result_list_id and prr.class_result_short_name = pr.class_result_short_name and prr.person_id = pr.person_id
           WHERE rl.id = :resultListId
           ORDER BY prr.race_number
           """)
    List<PersonRaceResultJdbcDto> findByResultListId(@Param("resultListId") Long resultListId);

    @Query("""
           SELECT
           rl.event_id, rl.id as result_list_id, rl.race_id, rl.create_time, rl.create_time_zone, rl.status as result_list_status,
           cl.short_name as class_list_short_name, cl.name as class_list_name, cl.gender as class_gender, cl.course_id,
           pr.person_id, pr.organisation_id,
           prr.start_time, prr.start_time_zone, prr.punch_time, prr.position, prr.race_number, prr.state
           FROM result_list rl
           LEFT JOIN class_result cl ON rl.id = cl.result_list_id
           LEFT JOIN person_result pr ON rl.id = cl.result_list_id and pr.class_result_short_name = cl.short_name
           LEFT JOIN person_race_result prr ON rl.id = prr.result_list_id and prr.class_result_short_name = pr.class_result_short_name and prr.person_id = pr.person_id
           WHERE rl.event_id = :eventId
           AND prr.state != 'DID_NOT_START'
           ORDER BY rl.id, cl.short_name, prr.race_number, prr.position, pr.person_id
           """)
    Collection<PersonRaceResultJdbcDto> findPersonRaceResultsByEventId(@Param("eventId") Long eventId);

    @Query("""
           SELECT DISTINCT
           rl.event_id, rl.id as result_list_id, rl.race_id, rl.create_time, rl.create_time_zone, rl.status as result_list_status,
           cl.short_name as class_list_short_name, cl.name as class_list_name, cl.gender as class_gender, cl.course_id,
           pr.person_id, pr.organisation_id,
           prr.start_time, prr.start_time_zone, prr.punch_time, prr.position, prr.race_number, prr.state
           FROM result_list rl
           LEFT JOIN class_result cl ON rl.id = cl.result_list_id and cl.short_name = :classResultShortName
           LEFT JOIN person_result pr ON rl.id = cl.result_list_id and pr.class_result_short_name = cl.short_name and pr.person_id = :personId
           LEFT JOIN person_race_result prr ON rl.id = prr.result_list_id and prr.class_result_short_name = pr.class_result_short_name and prr.person_id = pr.person_id
           WHERE rl.id = :resultListId
           AND cl.short_name = :classResultShortName
           AND pr.person_id = :personId
           ORDER BY prr.race_number
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
                                                       @Param("createTime") OffsetDateTime createTime,
                                                       @Param("createTimeZone") String createTimeZone);
}
