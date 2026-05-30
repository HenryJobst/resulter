package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourseValueObjectsTest {

    // -------------------------------------------------------------------------
    // CourseName
    // -------------------------------------------------------------------------

    @Test
    void courseName_of_setsValue() {
        assertThat(CourseName.of("Blau").value()).isEqualTo("Blau");
    }

    @Test
    void courseName_compareTo_ordersByValue() {
        assertThat(CourseName.of("A").compareTo(CourseName.of("B"))).isLessThan(0);
        assertThat(CourseName.of("B").compareTo(CourseName.of("A"))).isGreaterThan(0);
        assertThat(CourseName.of("X").compareTo(CourseName.of("X"))).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // CourseLength
    // -------------------------------------------------------------------------

    @Test
    void courseLength_of_setsValue() {
        assertThat(CourseLength.of(5.3).value()).isEqualTo(5.3);
    }

    @Test
    void courseLength_of_allowsNull() {
        assertThat(CourseLength.of(null).value()).isNull();
    }

    @Test
    void courseLength_compareTo_nullsLast() {
        CourseLength withValue = CourseLength.of(5.0);
        CourseLength withNull = CourseLength.of(null);

        assertThat(withValue.compareTo(withNull)).isLessThan(0);
        assertThat(withNull.compareTo(withValue)).isGreaterThan(0);
    }

    // -------------------------------------------------------------------------
    // CourseClimb
    // -------------------------------------------------------------------------

    @Test
    void courseClimb_of_setsValue() {
        assertThat(CourseClimb.of(120.0).value()).isEqualTo(120.0);
    }

    @Test
    void courseClimb_of_allowsNull() {
        assertThat(CourseClimb.of(null).value()).isNull();
    }

    @Test
    void courseClimb_compareTo_nullsLast() {
        CourseClimb withValue = CourseClimb.of(50.0);
        CourseClimb withNull = CourseClimb.of(null);

        assertThat(withValue.compareTo(withNull)).isLessThan(0);
    }

    // -------------------------------------------------------------------------
    // NumberOfControls
    // -------------------------------------------------------------------------

    @Test
    void numberOfControls_of_setsValue() {
        assertThat(NumberOfControls.of(15).value()).isEqualTo(15);
    }

    @Test
    void numberOfControls_of_allowsNull() {
        assertThat(NumberOfControls.of(null).value()).isNull();
    }

    @Test
    void numberOfControls_compareTo_sameInstance_returnsZero() {
        NumberOfControls n = NumberOfControls.of(15);
        assertThat(n.compareTo(n)).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // Gender
    // -------------------------------------------------------------------------

    @Test
    void gender_of_parsesValidValues() {
        assertThat(Gender.of("M")).isEqualTo(Gender.M);
        assertThat(Gender.of("F")).isEqualTo(Gender.F);
        assertThat(Gender.of("B")).isEqualTo(Gender.B);
    }

    @Test
    void gender_of_returnsUForNull() {
        assertThat(Gender.of(null)).isEqualTo(Gender.U);
    }

    @Test
    void gender_of_returnsUForBlank() {
        assertThat(Gender.of("")).isEqualTo(Gender.U);
        assertThat(Gender.of("  ")).isEqualTo(Gender.U);
    }

    @Test
    void gender_of_returnsUForUnknown() {
        assertThat(Gender.of("X")).isEqualTo(Gender.U);
        assertThat(Gender.of("male")).isEqualTo(Gender.U);
    }

    // -------------------------------------------------------------------------
    // DateTime
    // -------------------------------------------------------------------------

    @Test
    void dateTime_empty_hasNullValue() {
        assertThat(DateTime.empty().value()).isNull();
    }

    @Test
    void dateTime_compareTo_bothNull_returnsZero() {
        assertThat(DateTime.empty().compareTo(DateTime.empty())).isEqualTo(0);
    }

    @Test
    void dateTime_compareTo_nullIsLessThanNonNull() {
        DateTime withNull = DateTime.empty();
        DateTime withValue = DateTime.of(java.time.ZonedDateTime.now());

        assertThat(withNull.compareTo(withValue)).isLessThan(0);
        assertThat(withValue.compareTo(withNull)).isGreaterThan(0);
    }
}
