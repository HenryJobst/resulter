package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;

@Repository
@RepositoryRestResource(path = "result_lists")
public interface ResultListJdbcRepository
    extends CrudRepository<ResultListDbo, Long>, PagingAndSortingRepository<ResultListDbo, Long> {

    @NonNull
    Collection<ResultListDbo> findAll();

    Optional<ResultListDbo> findByCreatorAndCreateTimeAndCreateTimeZone(String creator,
                                                                        OffsetDateTime createTime,
                                                                        String createTimeZone);

    Collection<ResultListDbo> findByEventId(AggregateReference<EventDbo, Long> eventId);
}
