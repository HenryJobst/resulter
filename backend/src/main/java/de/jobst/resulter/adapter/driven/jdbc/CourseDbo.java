package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ =@PersistenceCreator)
@Table(name = "course")
public class CourseDbo {

    @Id
    @With
    @Column("id")
    @Nullable
    private Long id;

    @Column("event_id")
    private AggregateReference<EventDbo, Long> eventId;

    @Column("name")
    private String name;

    @Nullable
    @Column("length")
    private Double length;

    @Nullable
    @Column("climb")
    private Double climb;

    @Nullable
    @Column("controls")
    private Integer controls;

    public CourseDbo(AggregateReference<EventDbo, Long> eventId,
                     String name,
                     @Nullable Double length,
                     @Nullable Double climb,
                     @Nullable Integer controls) {
        this.id = null;
        this.eventId = eventId;
        this.name = name;
        this.length = length;
        this.climb = climb;
        this.controls = controls;
    }

    public static @Nullable CourseDbo from(@Nullable Course course, DboResolvers dboResolvers) {
        if (null == course) {
            return null;
        }
        CourseDbo courseDbo;
        if (course.getId().isPersistent()) {
            courseDbo = Objects.requireNonNull(dboResolvers.getCourseDboResolver()).findDboById(course.getId());
            courseDbo.setEventId(AggregateReference.to(course.getEventId().value()));
            courseDbo.setName(course.getCourseName().value());
            courseDbo.setLength(course.getCourseLength() != null ? course.getCourseLength().value() : null);
            courseDbo.setClimb(course.getCourseClimb() != null ? course.getCourseClimb().value() : null);
            courseDbo.setControls(course.getNumberOfControls() != null ? course.getNumberOfControls().value() : null);
        } else {
            courseDbo = new CourseDbo(AggregateReference.to(course.getEventId().value()),
                course.getCourseName().value(),
                course.getCourseLength() != null ? course.getCourseLength().value() : null,
                course.getCourseClimb() != null ? course.getCourseClimb().value() : null,
                course.getNumberOfControls() != null ? course.getNumberOfControls().value() : null);
        }
        return courseDbo;
    }

    public Course asCourse() {
        return new Course(id != null ? CourseId.of(id) : CourseId.empty(),
            EventId.of(eventId.getId()),
            CourseName.of(name),
            CourseLength.of(length),
            CourseClimb.of(climb),
            NumberOfControls.of(controls));
    }
}
