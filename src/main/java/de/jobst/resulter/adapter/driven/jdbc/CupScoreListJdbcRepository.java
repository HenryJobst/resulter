package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CupScoreListJdbcRepository extends CrudRepository<CupScoreListDbo, Long> {

}
