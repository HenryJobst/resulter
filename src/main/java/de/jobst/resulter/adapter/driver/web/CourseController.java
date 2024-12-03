package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.CourseDto;
import de.jobst.resulter.application.CourseService;
import de.jobst.resulter.domain.Course;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@Slf4j
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    private static void logError(Exception e) {
        log.error(e.getMessage());
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }

    @GetMapping("/course")
    public ResponseEntity<Page<CourseDto>> searchCourses(@RequestParam Optional<String> filter,
                                                         @PageableDefault(page = 0, size = 5000) Pageable pageable) {
        try {
            // TODO: Handle filter, pageable
            List<Course> courses = courseService.findAll();
            return ResponseEntity.ok(new PageImpl<>(courses.stream().map(CourseDto::from).toList(),
                PageRequest.of(0, courses.size()),
                courses.size()));
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
