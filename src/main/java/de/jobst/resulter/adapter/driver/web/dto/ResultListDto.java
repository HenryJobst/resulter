package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.ResultList;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;

public record ResultListDto(Long id, Long eventId, Long raceId, String creator, String createTime, String status,
                            Collection<ClassResultDto> classResults, Boolean isCertificateAvailable) {

    static public ResultListDto from(ResultList resultList, Event event) {
        assert resultList.getClassResults() != null;
        return new ResultListDto(resultList.getId().value(),
            resultList.getEventId().value(),
            resultList.getRaceId().value(),
            resultList.getCreator(),
            ObjectUtils.isNotEmpty(resultList.getCreateTime()) ? resultList.getCreateTime().toString() : null,
            resultList.getStatus(),
            resultList.getClassResults().stream().map(ClassResultDto::from).sorted().toList(),
            event.getCertificate() != null);
    }
}
