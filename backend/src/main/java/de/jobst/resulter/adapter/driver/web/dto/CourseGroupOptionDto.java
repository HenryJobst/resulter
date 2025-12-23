package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.port.CourseGroupOption;

import java.util.List;

/**
 * DTO for course grouping option.
 */
public record CourseGroupOptionDto(
        Long courseId,
        String courseName,
        List<String> classNames,
        int runnerCount
) {
    public static CourseGroupOptionDto from(CourseGroupOption option) {
        return new CourseGroupOptionDto(
                option.courseId(),
                option.courseName(),
                option.classNames(),
                option.runnerCount()
        );
    }
}
