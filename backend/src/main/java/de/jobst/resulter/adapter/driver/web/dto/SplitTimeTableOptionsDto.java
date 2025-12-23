package de.jobst.resulter.adapter.driver.web.dto;

import java.util.List;

/**
 * DTO for split-time table options (available classes and courses).
 */
public record SplitTimeTableOptionsDto(
        List<ClassGroupOptionDto> classes,
        List<CourseGroupOptionDto> courses
) {}
