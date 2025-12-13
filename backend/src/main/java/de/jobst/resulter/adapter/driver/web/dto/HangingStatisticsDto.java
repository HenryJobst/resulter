package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.HangingStatistics;
import org.jspecify.annotations.Nullable;

public record HangingStatisticsDto(
        int totalRunners,
        int runnersWithHanging,
        int totalHangingSegments,
        int highHangingRunners,
        int moderateHangingRunners,
        @Nullable Double averageHangingIndex,
        @Nullable Double medianHangingIndex
) {

    public static HangingStatisticsDto from(HangingStatistics stats) {
        return new HangingStatisticsDto(
                stats.totalRunners(),
                stats.runnersWithHanging(),
                stats.totalHangingSegments(),
                stats.highHangingRunners(),
                stats.moderateHangingRunners(),
                stats.averageHangingIndex(),
                stats.medianHangingIndex()
        );
    }
}
