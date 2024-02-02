package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Course;
import org.apache.commons.lang3.ObjectUtils;

public record CourseDto(Long id, String name, Double length, Double climb, Integer controls) {

    static public CourseDto from(Course course) {
        return new CourseDto(ObjectUtils.isNotEmpty(course.getId()) ? course.getId().value() : 0,
            course.getCourseName().value(),
            course.getCourseLength().value(),
            course.getCourseClimb().value(),
            course.getNumberOfControls().value());
    }
}
