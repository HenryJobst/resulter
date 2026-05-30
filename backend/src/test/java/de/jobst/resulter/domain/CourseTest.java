package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourseTest {

    private static Course course(String name) {
        return Course.of(EventId.of(1L), name, null, null, null);
    }

    @Test
    void of_setsFieldsCorrectly() {
        Course c = Course.of(EventId.of(2L), "Blau", 5.3, 120.0, 18);

        assertThat(c.getCourseName().value()).isEqualTo("Blau");
        assertThat(c.getCourseLength().value()).isEqualTo(5.3);
        assertThat(c.getCourseClimb().value()).isEqualTo(120.0);
        assertThat(c.getNumberOfControls().value()).isEqualTo(18);
        assertThat(c.getId().isPersistent()).isFalse();
    }

    @Test
    void of_withNullOptionalFields_storesNullValues() {
        Course c = course("Rot");

        assertThat(c.getCourseLength().value()).isNull();
        assertThat(c.getCourseClimb().value()).isNull();
        assertThat(c.getNumberOfControls().value()).isNull();
    }

    @Test
    void getDomainKey_returnsEventAndName() {
        Course c = Course.of(EventId.of(3L), "Grün", null, null, null);
        Course.DomainKey dk = c.getDomainKey();

        assertThat(dk.eventId()).isEqualTo(EventId.of(3L));
        assertThat(dk.courseName().value()).isEqualTo("Grün");
    }

    @Test
    void compareTo_ordersByCourseName() {
        Course blau = course("Blau");
        Course rot = course("Rot");

        assertThat(blau.compareTo(rot)).isLessThan(0);
        assertThat(rot.compareTo(blau)).isGreaterThan(0);
    }

    @Test
    void domainKey_compareTo_ordersByCourseName() {
        Course.DomainKey blau = new Course.DomainKey(EventId.of(1L), CourseName.of("Blau"));
        Course.DomainKey rot = new Course.DomainKey(EventId.of(1L), CourseName.of("Rot"));

        assertThat(blau.compareTo(rot)).isLessThan(0);
        assertThat(blau.compareTo(blau)).isEqualTo(0);
    }

    @Test
    void domainKey_compareTo_sameName_ordersByEventId() {
        Course.DomainKey e1 = new Course.DomainKey(EventId.of(1L), CourseName.of("Blau"));
        Course.DomainKey e2 = new Course.DomainKey(EventId.of(2L), CourseName.of("Blau"));

        assertThat(e1.compareTo(e2)).isLessThan(0);
    }

    @Test
    void equals_trueForSameIdAndName() {
        Course c1 = course("Blau");
        Course c2 = course("Blau");

        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    void equals_falseForDifferentName() {
        Course c1 = course("Blau");
        Course c2 = course("Rot");

        assertThat(c1).isNotEqualTo(c2);
    }

    @Test
    void equals_trueForSameReference() {
        Course c = course("Blau");
        assertThat(c.equals(c)).isTrue();
    }

    @Test
    void equals_falseForNull() {
        Course c = course("Blau");
        assertThat(c.equals(null)).isFalse();
    }

    @Test
    void equals_falseForDifferentType() {
        Course c = course("Blau");
        assertThat(c.equals("not a course")).isFalse();
    }

    @Test
    void compareTo_sameName_ordersByCourseId() {
        Course c1 = new Course(CourseId.of(1L), EventId.of(1L), CourseName.of("Blau"),
                CourseLength.of(null), CourseClimb.of(null), NumberOfControls.of(null));
        Course c2 = new Course(CourseId.of(2L), EventId.of(1L), CourseName.of("Blau"),
                CourseLength.of(null), CourseClimb.of(null), NumberOfControls.of(null));

        assertThat(c1.compareTo(c2)).isLessThan(0);
        assertThat(c2.compareTo(c1)).isGreaterThan(0);
    }

    @Test
    void of_withValueObjects_setsCorrectValues() {
        EventId eventId = EventId.of(5L);
        CourseName name = CourseName.of("Gelb");
        CourseLength length = CourseLength.of(3.5);
        CourseClimb climb = CourseClimb.of(80.0);
        NumberOfControls controls = NumberOfControls.of(12);

        Course c = Course.of(eventId, name, length, climb, controls);

        assertThat(c.getEventId()).isEqualTo(eventId);
        assertThat(c.getCourseName()).isEqualTo(name);
        assertThat(c.getCourseLength()).isEqualTo(length);
        assertThat(c.getCourseClimb()).isEqualTo(climb);
        assertThat(c.getNumberOfControls()).isEqualTo(controls);
        assertThat(c.getId().isPersistent()).isFalse();
    }
}
