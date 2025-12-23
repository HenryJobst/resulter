package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.SplitTimeTableMetadata;

/**
 * DTO for split-time table metadata.
 */
public record SplitTimeTableMetadataDto(
        int totalRunners,
        int runnersWithCompleteSplits,
        int totalControls,
        boolean reliableData,
        Double winnerTime
) {
    public static SplitTimeTableMetadataDto from(SplitTimeTableMetadata metadata) {
        return new SplitTimeTableMetadataDto(
                metadata.totalRunners(),
                metadata.runnersWithCompleteSplits(),
                metadata.totalControls(),
                metadata.reliableData(),
                metadata.winnerTime()
        );
    }
}
