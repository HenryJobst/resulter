package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RepositoryRestResource(path = "cups")
public interface CupJdbcRepository extends CrudRepository<CupDbo, Long>, PagingAndSortingRepository<CupDbo, Long> {

    Optional<CupDbo> findByName(String name);

    @Query("SELECT c FROM CupDbo c JOIN c.events e WHERE e.id = :eventId")
    List<CupDbo> findByEventId(@Param("eventId") Long eventId);
}
