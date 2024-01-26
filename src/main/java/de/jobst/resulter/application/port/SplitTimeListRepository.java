package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.SplitTimeList;
import de.jobst.resulter.domain.SplitTimeListId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SplitTimeListRepository {

    Optional<SplitTimeList> findById(SplitTimeListId splitTimeListId);

    SplitTimeList save(SplitTimeList splitTimeList);

    List<SplitTimeList> findAll();

    SplitTimeList findOrCreate(SplitTimeList splitTimeList);

    Collection<SplitTimeList> findOrCreate(Collection<SplitTimeList> splitTimeLists);

    void deleteAllByResultListId(Set<ResultListId> resultListIds);

    void deleteAllByEventId(EventId id);
}
