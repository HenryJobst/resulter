package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.SplitTimeList;
import de.jobst.resulter.domain.SplitTimeListId;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@PrimaryPort
public interface SplitTimeListService {

    Collection<SplitTimeList> findOrCreate(Collection<SplitTimeList> splitTimeLists);

    Optional<SplitTimeList> findById(SplitTimeListId splitTimeListId);

    List<SplitTimeList> findAll();
}
