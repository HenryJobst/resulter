package de.jobst.resulter.domain;

import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Association;
import org.jmolecules.ddd.annotation.Identity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Objects;

@AggregateRoot
@Getter
public final class Course implements Comparable<Course> {

    @Identity
    @NonNull
    private final CourseId id;

    @Association
    @NonNull
    private final EventId eventId;

    private final CourseName courseName;
    private final CourseLength courseLength;
    private final CourseClimb courseClimb;
    private final NumberOfControls numberOfControls;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Course course)) {
            return false;
        }
        return Objects.equals(id, course.id)
                && Objects.equals(courseName, course.courseName)
                && Objects.equals(eventId, course.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, courseName, eventId);
    }

    public record DomainKey(EventId eventId, CourseName courseName) implements Comparable<DomainKey> {

        @Override
        public int compareTo(@NonNull DomainKey o) {
            int val = courseName.compareTo(o.courseName);
            if (val == 0) {
                val = eventId.compareTo(o.eventId);
            }
            return val;
        }
    }

    public Course(
            @NonNull CourseId id,
            @NonNull EventId eventId,
            @NonNull CourseName courseName,
            @Nullable CourseLength courseLength,
            @Nullable CourseClimb courseClimb,
            @Nullable NumberOfControls numberOfControls) {
        this.id = id;
        this.eventId = eventId;
        this.courseName = courseName;
        this.courseLength = courseLength;
        this.courseClimb = courseClimb;
        this.numberOfControls = numberOfControls;
    }

    public static Course of(
            EventId eventId,
            CourseName courseName,
            CourseLength courseLength,
            CourseClimb courseClimb,
            NumberOfControls numberOfControls) {
        return new Course(CourseId.empty(), eventId, courseName, courseLength, courseClimb, numberOfControls);
    }

    public static Course of(
            EventId eventId, String courseName, Double courseLength, Double courseClimb, Integer numberOfControls) {
        return new Course(
                CourseId.empty(),
                eventId,
                CourseName.of(courseName),
                CourseLength.of(courseLength),
                CourseClimb.of(courseClimb),
                NumberOfControls.of(numberOfControls));
    }

    @Override
    public int compareTo(@NonNull Course o) {
        int val = this.courseName.compareTo(o.courseName);
        if (val == 0) {
            val = this.id.compareTo(o.id);
        }
        return val;
    }

    public DomainKey getDomainKey() {
        return new DomainKey(eventId, courseName);
    }
}
