package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.ResultList;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;

public record ResultListDto(Long id, String creator, String createTime, String status,
                            Collection<ClassResultDto> classResults) {

    static public ResultListDto from(ResultList resultList) {
        assert resultList.getClassResults() != null;
        return new ResultListDto(resultList.getId().value(),
            resultList.getCreator(),
            ObjectUtils.isNotEmpty(resultList.getCreateTime()) ? resultList.getCreateTime().toString() : null,
            resultList.getStatus(),
            resultList.getClassResults().stream().map(ClassResultDto::from).sorted().toList());
    }
}
