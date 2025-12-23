package de.jobst.resulter.domain.analysis;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

/**
 * Winsplits-style split-time table showing runners in rows and controls in columns.
 * Each cell contains cumulative and segment times with positions and error indicators.
 */
@ValueObject
public record SplitTimeTable(
        String groupByType,           // "CLASS" or "COURSE"
        String groupId,               // class name or course ID as string
        List<String> groupNames,      // class names included in this table
        List<String> controlCodes,    // ["S", "101", "102", ..., "F"]
        List<SplitTimeTableRow> rows,
        SplitTimeTableMetadata metadata
) {
    /**
     * Creates a split-time table with defensive copy of lists.
     */
    public SplitTimeTable {
        groupNames = List.copyOf(groupNames);
        controlCodes = List.copyOf(controlCodes);
        rows = List.copyOf(rows);
    }
}
