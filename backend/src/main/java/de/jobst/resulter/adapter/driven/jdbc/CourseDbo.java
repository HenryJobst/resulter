package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.jspecify.annotations.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ =@PersistenceCreator)
@Table(name = "course")
public class CourseDbo {

    @Id
    @With
    @Column("id")
    private Long id;

    @Column("event_id")
    private AggregateReference<@NonNull EventDbo, @NonNull Long> eventId;

    @Column("name")
    private String name;

    @Column("length")
    private Double length;

    @Column("climb")
    private Double climb;

    @Column("controls")
    private Integer controls;

    public CourseDbo(AggregateReference<@NonNull EventDbo, @NonNull Long> eventId,
                     String name,
                     Double length,
                     Double climb,
                     Integer controls) {
        this.id = null;
        this.eventId = eventId;
        this.name = name;
        this.length = length;
        this.climb = climb;
        this.controls = controls;
    }

    public static CourseDbo from(Course course, @NonNull DboResolvers dboResolvers) {
        if (null == course) {
            return null;
        }
        CourseDbo courseDbo;
        if (course.getId().isPersistent()) {
            courseDbo = dboResolvers.getCourseDboResolver().findDboById(course.getId());
            courseDbo.setEventId(AggregateReference.to(course.getEventId().value()));
            courseDbo.setName(course.getCourseName().value());
            courseDbo.setLength(course.getCourseLength().value());
            courseDbo.setClimb(course.getCourseClimb().value());
            courseDbo.setControls(course.getNumberOfControls().value());
        } else {
            courseDbo = new CourseDbo(AggregateReference.to(course.getEventId().value()),
                course.getCourseName().value(),
                course.getCourseLength().value(),
                course.getCourseClimb().value(),
                course.getNumberOfControls().value());
        }
        return courseDbo;
    }

    public Course asCourse() {
        return new Course(CourseId.of(id),
            EventId.of(eventId.getId()),
            CourseName.of(name),
            CourseLength.of(length),
            CourseClimb.of(climb),
            NumberOfControls.of(controls));
    }
}
