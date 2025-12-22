package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.analysis.SplitTimeTable;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

import java.util.List;

/**
 * Service for generating Winsplits-style split-time tables.
 * Provides tables grouped by class or course with cumulative/segment times,
 * positions, and individual PI-based error detection.
 */
@PrimaryPort
public interface SplitTimeTableService {

    /**
     * Generate split-time table grouped by class.
     *
     * @param resultListId The result list ID
     * @param className    The class name to filter by
     * @return Split-time table for the specified class
     */
    SplitTimeTable generateByClass(ResultListId resultListId, String className);

    /**
     * Generate split-time table grouped by course.
     * Includes all classes that ran the specified course.
     *
     * @param resultListId The result list ID
     * @param courseId     The course ID
     * @return Split-time table for all classes on the specified course
     */
    SplitTimeTable generateByCourse(ResultListId resultListId, Long courseId);

    /**
     * Get available classes for a result list.
     *
     * @param resultListId The result list ID
     * @return List of class options with runner counts
     */
    List<ClassGroupOption> getAvailableClasses(ResultListId resultListId);

    /**
     * Get available courses for a result list.
     *
     * @param resultListId The result list ID
     * @return List of course options with associated classes and runner counts
     */
    List<CourseGroupOption> getAvailableCourses(ResultListId resultListId);
}
