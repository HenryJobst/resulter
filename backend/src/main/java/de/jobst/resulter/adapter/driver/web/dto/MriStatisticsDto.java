package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.MriStatistics;

/**
 * DTO for aggregate Mental Resilience Index statistics.
 */
public record MriStatisticsDto(
        Integer totalRunners,
        Integer runnersWithMistakes,
        Integer totalMistakes,
        Integer panicReactions,
        Integer iceManReactions,
        Integer resignerReactions,
        Double averageMRI,
        Double medianMRI
) {
    /**
     * Converts domain statistics to DTO.
     *
     * @param statistics domain statistics
     * @return DTO
     */
    public static MriStatisticsDto from(MriStatistics statistics) {
        return new MriStatisticsDto(
                statistics.totalRunners(),
                statistics.runnersWithMistakes(),
                statistics.totalMistakes(),
                statistics.panicReactions(),
                statistics.iceManReactions(),
                statistics.resignerReactions(),
                statistics.averageMRI(),
                statistics.medianMRI()
        );
    }
}
