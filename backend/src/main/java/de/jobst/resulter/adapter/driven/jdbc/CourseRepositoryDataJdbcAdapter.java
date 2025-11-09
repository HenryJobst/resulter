package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.CourseRepository;
import de.jobst.resulter.domain.Course;
import de.jobst.resulter.domain.CourseId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class CourseRepositoryDataJdbcAdapter implements CourseRepository {

    private final CourseJdbcRepository courseJdbcRepository;

    public CourseRepositoryDataJdbcAdapter(CourseJdbcRepository courseJdbcRepository) {
        this.courseJdbcRepository = courseJdbcRepository;
    }

    @Override
    public Course save(Course course) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setCourseDboResolver(id -> courseJdbcRepository.findById(id.value()).orElseThrow());
        CourseDbo courseEntity = CourseDbo.from(course, dboResolvers);
        CourseDbo savedCourseEntity = courseJdbcRepository.save(courseEntity);
        return savedCourseEntity.asCourse();
    }

    @Override
    public List<Course> findAll() {
        return courseJdbcRepository.findAll().stream().map(CourseDbo::asCourse).sorted().toList();
    }

    @Override
    public Optional<Course> findById(CourseId courseId) {
        Optional<CourseDbo> courseEntity = courseJdbcRepository.findById(courseId.value());
        return courseEntity.map(CourseDbo::asCourse);
    }

    @Override
    public Course findOrCreate(Course course) {
        Optional<CourseDbo> courseEntity =
            courseJdbcRepository.findByEventIdAndName(AggregateReference.to(course.getEventId().value()),
                course.getCourseName().value());
        if (courseEntity.isEmpty()) {
            return save(course);
        }
        CourseDbo entity = courseEntity.get();
        return entity.asCourse();
    }

    @Override
    @Transactional
    public Collection<Course> findOrCreate(Collection<Course> courses) {
        var eventIds = courses.stream().map(Course::getEventId).collect(Collectors.toSet());
        assert eventIds.size() == 1;
        if (eventIds.isEmpty()) {
            return List.of();
        }
        Collection<CourseDbo> existingCourses =
            courseJdbcRepository.findAllByEventIdAndNameIn(AggregateReference.to(eventIds.stream()
                .findFirst()
                .orElseThrow()
                .value()), courses.stream().map(x -> x.getCourseName().value()).sorted().toList());
        Set<String> existingCourseNames = existingCourses.stream().map(CourseDbo::getName).collect(Collectors.toSet());
        Iterable<CourseDbo> savedEntities = courseJdbcRepository.saveAll(courses.stream()
            .filter(x -> !existingCourseNames.contains(x.getCourseName().value()))
            .map(y -> CourseDbo.from(y, DboResolvers.empty()))
            .toList());
        return Stream.concat(StreamSupport.stream(savedEntities.spliterator(), false).map(CourseDbo::asCourse),
            existingCourses.stream().map(CourseDbo::asCourse)).toList();
    }
}
