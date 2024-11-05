package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.CupScoreList;

import java.util.List;
import java.util.Set;

public interface CupScoreListRepository {

    void deleteAllByDomainKey(Set<CupScoreList.DomainKey> cupScoreList);

    List<CupScoreList> saveAll(List<CupScoreList> cupScoreList);
}
