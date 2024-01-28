package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Course;
import de.jobst.resulter.domain.CourseId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "true")
public class InMemoryCourseRepository implements CourseRepository {

    private final Map<CourseId, Course> courses = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<Course> savedCourses = new ArrayList<>();

    @Override
    public Course save(Course course) {
        if (ObjectUtils.isEmpty(course.getId()) || course.getId().value() == 0) {
            course.setId(CourseId.of(sequence.incrementAndGet()));
        }
        courses.put(course.getId(), course);
        savedCourses.add(course);
        return course;
    }

    @Override
    public List<Course> findAll() {
        return List.copyOf(courses.values());
    }

    @Override
    public Optional<Course> findById(CourseId CourseId) {
        return Optional.ofNullable(courses.get(CourseId));
    }

    @Override
    public Course findOrCreate(Course course) {
        return courses.values()
            .stream()
            .filter(it -> Objects.equals(it.getCourseName(), course.getCourseName()))
            .findAny()
            .orElseGet(() -> save(course));
    }

    @Override
    public Collection<Course> findOrCreate(Collection<Course> courses) {
        return courses.stream().map(this::findOrCreate).toList();
    }

    @SuppressWarnings("unused")
    public List<Course> savedCourses() {
        return savedCourses;
    }

    @SuppressWarnings("unused")
    public int saveCount() {
        return savedCourses.size();
    }

    @SuppressWarnings("unused")
    public void resetSaveCount() {
        savedCourses.clear();
    }

}
