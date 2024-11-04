package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.CupScoreList;

import java.util.List;

public interface CupScoreListRepository {

    List<CupScoreList> saveAll(List<CupScoreList> cupScoreList);
}
