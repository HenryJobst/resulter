package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;

import java.util.Optional;

public interface ResultListRepository {

    Optional<ResultList> findById(ResultListId resultListId);
}
