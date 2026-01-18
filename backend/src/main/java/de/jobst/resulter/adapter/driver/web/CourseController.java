package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.CourseDto;
import de.jobst.resulter.adapter.driver.web.mapper.CourseMapper;
import de.jobst.resulter.application.port.CourseService;
import de.jobst.resulter.domain.Course;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CourseController {

    private final CourseService courseService;
    private final CourseMapper courseMapper;

    public CourseController(CourseService courseService, CourseMapper courseMapper) {
        this.courseService = courseService;
        this.courseMapper = courseMapper;
    }

    @GetMapping("/course/all")
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        List<Course> courses = courseService.findAll();
        return ResponseEntity.ok(courseMapper.toDtos(courses));
    }

    @GetMapping("/course")
    public ResponseEntity<Page<CourseDto>> searchCourses(
            @RequestParam Optional<String> ignoredFilter, Pageable pageable) {
        // TODO: Handle filter, pageable
        List<Course> courses = courseService.findAll();
        return ResponseEntity.ok(new PageImpl<>(
                courseMapper.toDtos(courses),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
                courses.size()));
    }
}
