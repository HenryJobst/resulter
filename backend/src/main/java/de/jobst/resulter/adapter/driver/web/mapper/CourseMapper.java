package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.CourseDto;
import de.jobst.resulter.domain.Course;
import java.util.List;

public class CourseMapper {

    public static CourseDto toDto(Course course) {
        return CourseDto.from(course);
    }

    public static List<CourseDto> toDtos(List<Course> courses) {
        return courses.stream().map(CourseMapper::toDto).toList();
    }
}
