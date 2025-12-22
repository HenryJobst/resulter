package de.jobst.resulter.application.port;

import java.util.List;

/**
 * Course grouping option for selection.
 */
public record CourseGroupOption(
        Long courseId,
        String courseName,
        List<String> classNames,
        int runnerCount
) {
    /**
     * Creates a course group option with defensive copy of class names.
     */
    public CourseGroupOption {
        classNames = List.copyOf(classNames);
    }
}
