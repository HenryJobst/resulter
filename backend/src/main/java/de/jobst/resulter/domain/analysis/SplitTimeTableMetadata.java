package de.jobst.resulter.domain.analysis;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Metadata about the split-time table for data quality assessment.
 */
@ValueObject
public record SplitTimeTableMetadata(
        int totalRunners,
        int runnersWithCompleteSplits,
        int totalControls,
        boolean reliableData  // true if >= 5 runners (statistical validity threshold)
) {}
