package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ResultListRepository {

    Optional<ResultList> findById(ResultListId resultListId);

    ResultList save(ResultList resultList);

    List<ResultList> findAll();

    ResultList findOrCreate(ResultList resultList);

    @Transactional
    Collection<ResultList> findOrCreate(Collection<ResultList> resultLists);
}
