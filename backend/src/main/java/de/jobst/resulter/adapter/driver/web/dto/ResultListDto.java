package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.ResultList;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;

public record ResultListDto(
        Long id,
        Long eventId,
        Long raceId,
        String creator,
        String createTime,
        String status,
        Collection<ClassResultDto> classResults,
        Boolean isCertificateAvailable,
        Boolean isCupScoreAvailable,
        Boolean isSplitTimeAvailable) {

    public static ResultListDto from(ResultList resultList, Event event, int resultListSize) {
        assert resultList.getClassResults() != null;
        byte raceNumber = resultList.getClassResults().stream()
                .flatMap(x -> x.personResults().value().stream())
                .flatMap(y -> y.personRaceResults().value().stream())
                .map(z -> z.getRaceNumber().value())
                .distinct()
                .findFirst()
                .orElse((byte) 1);
        boolean hasSplitTimes = resultList.getClassResults().stream()
                .flatMap(x -> x.personResults().value().stream())
                .flatMap(y -> y.personRaceResults().value().stream())
                .anyMatch(z -> z.getSplitTimeListId() != null);
        return new ResultListDto(
                resultList.getId().value(),
                resultList.getEventId().value(),
                resultList.getRaceId().value(),
                resultList.getCreator(),
                ObjectUtils.isNotEmpty(resultList.getCreateTime())
                        ? resultList.getCreateTime().toString()
                        : null,
                resultList.getStatus(),
                resultList.getClassResults().stream()
                        .map(ClassResultDto::from)
                        .sorted()
                        .toList(),
                event.getCertificate() != null && (resultListSize == 1 || raceNumber == 0),
                raceNumber > 0,
                hasSplitTimes);
    }
}
