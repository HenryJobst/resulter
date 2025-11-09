package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.CupScoreList;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;

public record CupScoreListDto(Long id, Long cupId, Long resultListId, String creator, String createTime, String status,
                              Collection<CupScoreDto> cupScores) {

    static public CupScoreListDto from(CupScoreList cupScoreList) {
        return new CupScoreListDto(cupScoreList.getId().value(),
            cupScoreList.getCupId().value(),
            cupScoreList.getResultListId().value(),
            cupScoreList.getCreator(),
            ObjectUtils.isNotEmpty(cupScoreList.getCreateTime()) ? cupScoreList.getCreateTime().toString() : null,
            cupScoreList.getStatus(),
            cupScoreList.getCupScores().stream().map(CupScoreDto::from).sorted().toList());
    }
}
