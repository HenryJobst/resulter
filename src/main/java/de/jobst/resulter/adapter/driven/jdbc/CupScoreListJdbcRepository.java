package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CupScoreListJdbcRepository extends CrudRepository<CupScoreListDbo, Long> {

    List<CupScoreListDbo> findByResultListId(AggregateReference<ResultListDbo, Long> resultListId);
}
