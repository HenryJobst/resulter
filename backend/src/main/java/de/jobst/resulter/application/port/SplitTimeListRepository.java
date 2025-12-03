package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.SplitTimeList;
import de.jobst.resulter.domain.SplitTimeListId;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@SecondaryPort
public interface SplitTimeListRepository {

    Optional<SplitTimeList> findById(SplitTimeListId splitTimeListId);

    SplitTimeList save(SplitTimeList splitTimeList);

    List<SplitTimeList> findAll();

    SplitTimeList findOrCreate(SplitTimeList splitTimeList);

    Collection<SplitTimeList> findOrCreate(Collection<SplitTimeList> splitTimeLists);

    void replacePersonId(PersonId oldPersonId, PersonId newPersonId);

    List<SplitTimeList> findByResultListId(ResultListId resultListId);
}
