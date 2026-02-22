package de.jobst.resulter.adapter.driven.jdbc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CupJdbcRepository
        extends CrudRepository<CupDbo, Long>,
                PagingAndSortingRepository<CupDbo, Long>,
                QueryByExampleExecutor<CupDbo>,
                CupJdbcRepositoryCustom {

    Optional<CupDbo> findByName(String name);

    @Query("SELECT c.* FROM cup c JOIN cup_event ce ON c.id = ce.cup_id and ce.event_id = :eventId")
    List<CupDbo> findByEventId(@Param("eventId") Long eventId);

    /**
     * Find all cup-event mappings for given cup IDs.
     * Use this for batch loading events after loading cups.
     */
    @Query("SELECT * FROM cup_event WHERE cup_id IN (:cupIds)")
    List<CupEventDbo> findEventsByCupIds(@Param("cupIds") Collection<Long> cupIds);
}
