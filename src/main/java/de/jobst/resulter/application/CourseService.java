package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CourseRepository;
import de.jobst.resulter.domain.Course;
import de.jobst.resulter.domain.CourseId;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course findOrCreate(Course course) {
        return courseRepository.findOrCreate(course);
    }

    Optional<Course> findById(CourseId courseId) {
        return courseRepository.findById(courseId);
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Collection<Course> findOrCreate(Collection<Course> courses) {
        return courseRepository.findOrCreate(courses);
    }
}
