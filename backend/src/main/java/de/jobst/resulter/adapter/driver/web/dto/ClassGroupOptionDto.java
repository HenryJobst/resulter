package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.port.ClassGroupOption;

/**
 * DTO for class grouping option.
 */
public record ClassGroupOptionDto(
        String className,
        int runnerCount
) {
    public static ClassGroupOptionDto from(ClassGroupOption option) {
        return new ClassGroupOptionDto(
                option.className(),
                option.runnerCount()
        );
    }
}
