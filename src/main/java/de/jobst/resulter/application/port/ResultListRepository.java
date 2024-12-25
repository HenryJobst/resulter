package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ResultListRepository {

    ResultList save(ResultList resultList);

    List<ResultList> findAll();

    ResultList findOrCreate(ResultList resultList);

    Collection<ResultList> findOrCreate(Collection<ResultList> resultLists);

    ResultList update(ResultList resultList);

    Collection<ResultList> findByEventId(EventId id);

    Optional<ResultList> findById(ResultListId resultListId);

    ResultList findByResultListIdAndClassResultShortNameAndPersonId(ResultListId resultListId,
                                                                    ClassResultShortName classResultShortName,
                                                                    PersonId personId);

    void replacePersonId(PersonId oldPersonId, PersonId newPersonId);
}
