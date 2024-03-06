package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaFileJdbcRepository
    extends CrudRepository<MediaFileDbo, Long>, PagingAndSortingRepository<MediaFileDbo, Long> {}
