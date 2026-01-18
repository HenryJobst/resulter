package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.CourseDto;
import de.jobst.resulter.domain.Course;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public CourseDto toDto(Course course) {
        return CourseDto.from(course);
    }

    public List<CourseDto> toDtos(List<Course> courses) {
        return courses.stream().map(this::toDto).toList();
    }
}
