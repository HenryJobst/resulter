package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.ResultListDto;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListScoringService;
import java.util.Collection;
import org.apache.commons.lang3.ObjectUtils;

public class ResultListMapper {

    private ResultListMapper() {}

    public static ResultListDto toDto(
            ResultList resultList,
            Event event,
            int resultListSize,
            Collection<ResultList> allEventResultLists,
            boolean eventHasCup) {
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
                        .map(ClassResultMapper::toDto)
                        .sorted()
                        .toList(),
                event.getCertificate() != null && (resultListSize == 1 || raceNumber == 0),
                eventHasCup
                        && ResultListScoringService.isScorableForCupCalculation(resultList, allEventResultLists, event),
                hasSplitTimes);
    }
}
