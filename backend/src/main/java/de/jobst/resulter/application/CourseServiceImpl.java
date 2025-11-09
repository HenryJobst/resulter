package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CourseRepository;
import de.jobst.resulter.application.port.CourseService;
import de.jobst.resulter.domain.Course;
import de.jobst.resulter.domain.CourseId;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public Optional<Course> findById(CourseId courseId) {
        return courseRepository.findById(courseId);
    }

    @Override
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @Override
    public Collection<Course> findOrCreate(Collection<Course> courses) {
        return courseRepository.findOrCreate(courses);
    }
}
