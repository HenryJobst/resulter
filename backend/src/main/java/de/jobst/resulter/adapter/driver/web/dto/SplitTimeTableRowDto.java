package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.SplitTimeTableRow;

import java.util.List;

/**
 * DTO for a single row in the split-time table.
 */
public record SplitTimeTableRowDto(
        Long personId,
        String personName,
        String className,
        List<SplitTimeTableCellDto> cells,
        boolean hasIncompleteSplits
) {
    public static SplitTimeTableRowDto from(SplitTimeTableRow row) {
        return new SplitTimeTableRowDto(
                row.personId(),
                row.personName(),
                row.className(),
                row.cells().stream()
                        .map(SplitTimeTableCellDto::from)
                        .toList(),
                row.hasIncompleteSplits()
        );
    }
}
