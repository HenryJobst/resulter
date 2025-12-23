package de.jobst.resulter.domain.analysis;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

/**
 * Metadata about the split-time table for data quality assessment.
 */
@ValueObject
public record SplitTimeTableMetadata(
        int totalRunners,
        int runnersWithCompleteSplits,
        int totalControls,
        boolean reliableData,        // true if >= 5 runners (statistical validity threshold)
        @Nullable Double winnerTime  // finish time of winner (minimum among competing runners)
) {}
