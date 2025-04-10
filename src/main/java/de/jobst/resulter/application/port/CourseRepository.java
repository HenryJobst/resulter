package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Course;
import de.jobst.resulter.domain.CourseId;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@SecondaryPort
public interface CourseRepository {

    Course save(Course event);

    List<Course> findAll();

    Optional<Course> findById(CourseId CourseId);

    Course findOrCreate(Course course);

    Collection<Course> findOrCreate(Collection<Course> courses);
}
