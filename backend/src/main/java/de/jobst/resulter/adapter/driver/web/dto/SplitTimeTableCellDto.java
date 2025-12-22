package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.SplitTimeTableCell;

/**
 * DTO for a single cell in the split-time table.
 */
public record SplitTimeTableCellDto(
        String controlCode,
        Double cumulativeTime,
        Integer cumulativePosition,
        Double segmentTime,
        Integer segmentPosition,
        boolean isError,
        String errorSeverity,
        Double errorMagnitude,
        boolean isBestCumulative,
        boolean isBestSegment
) {
    public static SplitTimeTableCellDto from(SplitTimeTableCell cell) {
        return new SplitTimeTableCellDto(
                cell.controlCode(),
                cell.cumulativeTime(),
                cell.cumulativePosition(),
                cell.segmentTime(),
                cell.segmentPosition(),
                cell.isError(),
                cell.errorSeverity().name(),
                cell.errorMagnitude(),
                cell.isBestCumulative(),
                cell.isBestSegment()
        );
    }
}
