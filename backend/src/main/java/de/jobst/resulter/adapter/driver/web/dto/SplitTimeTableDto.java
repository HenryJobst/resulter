package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.*;

import java.util.List;

/**
 * DTO for Winsplits-style split-time table.
 */
public record SplitTimeTableDto(
        String groupByType,
        String groupId,
        List<String> groupNames,
        List<String> controlCodes,
        List<SplitTimeTableRowDto> rows,
        SplitTimeTableMetadataDto metadata
) {
    public static SplitTimeTableDto from(SplitTimeTable table) {
        return new SplitTimeTableDto(
                table.groupByType(),
                table.groupId(),
                table.groupNames(),
                table.controlCodes(),
                table.rows().stream()
                        .map(SplitTimeTableRowDto::from)
                        .toList(),
                SplitTimeTableMetadataDto.from(table.metadata())
        );
    }
}
