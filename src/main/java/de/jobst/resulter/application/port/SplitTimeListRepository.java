package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.SplitTimeList;
import de.jobst.resulter.domain.SplitTimeListId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SplitTimeListRepository {

    Optional<SplitTimeList> findById(SplitTimeListId splitTimeListId);

    SplitTimeList save(SplitTimeList splitTimeList);

    List<SplitTimeList> findAll();

    SplitTimeList findOrCreate(SplitTimeList splitTimeList);

    Collection<SplitTimeList> findOrCreate(Collection<SplitTimeList> splitTimeLists);

    void replacePersonId(PersonId oldPersonId, PersonId newPersonId);
}
