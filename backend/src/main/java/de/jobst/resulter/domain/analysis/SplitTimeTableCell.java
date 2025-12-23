package de.jobst.resulter.domain.analysis;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

/**
 * Single cell in the split-time table for a runner at a specific control.
 */
@ValueObject
public record SplitTimeTableCell(
    String controlCode,

    // Cumulative data (total time from start to this control)
    @Nullable Double cumulativeTime, // null if missing split
    @Nullable Integer cumulativePosition, // rank at this control based on cumulative time, null if missing split

    // Segment data (time from previous control to this control)
    @Nullable Double segmentTime, // null if missing or for start control
    @Nullable Integer segmentPosition, // rank for this segment, null if no rank for this segment

    // Error detection (individual PI-based)
    boolean isError,             // true if segment-PI > Normal-PI + threshold
    ErrorSeverity errorSeverity, // severity level for visual highlighting
    @Nullable Double errorMagnitude, // how much worse than normal (segment-PI - Normal-PI), null if no split

    // Best time indicators
    boolean isBestCumulative,    // true if this is the best cumulative time at this control
    boolean isBestSegment        // true if this is the best segment time to this control
) {}
