package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.ResultList;

import java.util.Collection;

public record ResultListDto(String creator, Collection<ClassResultDto> classResultDtos) {

    static public ResultListDto from(ResultList resultList) {
        return new ResultListDto(resultList.getCreator(),
            resultList.getClassResults().stream().map(ClassResultDto::from).sorted().toList());
    }
}
