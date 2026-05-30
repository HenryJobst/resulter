package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PunchTimeTest {

    @Test
    void of_returnsNullValue() {
        assertThat(PunchTime.of(null).value()).isNull();
    }

    @Test
    void compareTo_bothNull_returnsZero() {
        assertThat(PunchTime.of(null).compareTo(PunchTime.of(null))).isEqualTo(0);
    }

    @Test
    void compareTo_nullIsLessThanNonNull() {
        // null-value sorts before actual values
        assertThat(PunchTime.of(null).compareTo(PunchTime.of(100.0))).isLessThan(0);
    }

    @Test
    void compareTo_nonNullIsGreaterThanNull() {
        assertThat(PunchTime.of(100.0).compareTo(PunchTime.of(null))).isGreaterThan(0);
    }

    @Test
    void compareTo_ordersByValueAscending() {
        assertThat(PunchTime.of(90.0).compareTo(PunchTime.of(100.0))).isLessThan(0);
        assertThat(PunchTime.of(100.0).compareTo(PunchTime.of(90.0))).isGreaterThan(0);
        assertThat(PunchTime.of(100.0).compareTo(PunchTime.of(100.0))).isEqualTo(0);
    }

    @Test
    void toString_containsClassNameAndValue() {
        assertThat(PunchTime.of(123.0).toString()).isEqualTo("PunchTime=123.0");
    }
}
