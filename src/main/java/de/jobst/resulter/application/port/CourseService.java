package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Course;
import de.jobst.resulter.domain.CourseId;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@PrimaryPort
public interface CourseService {

    Optional<Course> findById(CourseId courseId);

    List<Course> findAll();

    Collection<Course> findOrCreate(Collection<Course> courses);
}
