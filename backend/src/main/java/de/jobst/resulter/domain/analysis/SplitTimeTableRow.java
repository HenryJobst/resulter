package de.jobst.resulter.domain.analysis;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Single row in the split-time table representing one runner.
 */
@ValueObject
public record SplitTimeTableRow(
        Long personId,
        String personName,           // "Lastname, Firstname"
        String className,
        List<SplitTimeTableCell> cells,
        boolean hasIncompleteSplits, // true if runner has missing split data
        boolean notCompeting,        // true if runner is NOT_COMPETING (AK/außer Konkurrenz)
        @Nullable Double finishTime, // cumulative time at finish control
        @Nullable Integer position   // overall position (null for AK runners)
) {
    /**
     * Creates a table row with defensive copy of cells.
     */
    public SplitTimeTableRow {
        cells = List.copyOf(cells);
    }
}
