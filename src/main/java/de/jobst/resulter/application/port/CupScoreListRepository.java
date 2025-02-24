package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.CupScoreList;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.ResultListId;
import org.jmolecules.ddd.annotation.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CupScoreListRepository {

    void deleteAllByDomainKey(Set<CupScoreList.DomainKey> cupScoreList);

    List<CupScoreList> saveAll(List<CupScoreList> cupScoreList);

    List<CupScoreList> findAllByResultListId(ResultListId resultListId);
    List<CupScoreList> findAllByResultListIdAndCupId(ResultListId resultListId, CupId cupId);

    void replacePersonId(PersonId oldPersonId, PersonId newPersonId);
}
