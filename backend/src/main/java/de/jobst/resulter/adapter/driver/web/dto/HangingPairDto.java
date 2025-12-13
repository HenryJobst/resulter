package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.HangingPair;

public record HangingPairDto(
        int legNumber,
        String fromControl,
        String toControl,
        Long busDriverId,
        String busDriverClassName,
        Integer busDriverRaceNumber,
        double timeDeltaSeconds,
        double passengerPI,
        double busDriverPI,
        double hangingIndex,
        double improvementPercent,
        double passengerActualTime,
        double busDriverActualTime,
        double referenceTime
) {

    public static HangingPairDto from(HangingPair pair) {
        return new HangingPairDto(
                pair.legNumber(),
                pair.fromControl().value(),
                pair.toControl().value(),
                pair.busDriverId().value(),
                pair.busDriverClassName(),
                pair.busDriverRaceNumber().value().intValue(),
                pair.timeDeltaSeconds(),
                pair.passengerSegmentPI().value(),
                pair.busDriverSegmentPI().value(),
                pair.hangingIndex().value(),
                pair.getImprovementPercent(),
                pair.passengerActualTime(),
                pair.busDriverActualTime(),
                pair.referenceTime()
        );
    }
}
