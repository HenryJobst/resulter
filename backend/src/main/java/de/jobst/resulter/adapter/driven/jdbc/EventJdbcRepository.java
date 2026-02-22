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
public interface EventJdbcRepository
        extends CrudRepository<EventDbo, Long>,
                PagingAndSortingRepository<EventDbo, Long>,
                QueryByExampleExecutor<EventDbo>,
                EventJdbcRepositoryCustom {

    Collection<EventDbo> findAll();

    Optional<EventDbo> findByName(String name);

    /**
     * Find all event-organisation mappings for given event IDs.
     * Use this for batch loading organisations after loading events.
     */
    @Query("SELECT * FROM event_organisation WHERE event_id IN (:eventIds)")
    List<EventOrganisationDbo> findOrganisationsByEventIds(@Param("eventIds") Collection<Long> eventIds);
}
