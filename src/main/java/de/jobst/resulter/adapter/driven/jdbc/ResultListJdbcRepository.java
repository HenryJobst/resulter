package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;

@Repository
@RepositoryRestResource(path = "result_lists")
public interface ResultListJdbcRepository
    extends CrudRepository<ResultListDbo, Long>, PagingAndSortingRepository<ResultListDbo, Long> {

    @NonNull
    Collection<ResultListDbo> findAll();

    Optional<ResultListDbo> findByCreatorAndCreateTime(String creator, ZonedDateTime createTime);
}
