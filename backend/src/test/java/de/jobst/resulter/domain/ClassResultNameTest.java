package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassResultNameTest {

    @Test
    void of_setsValue() {
        assertThat(ClassResultName.of("Herren Elite").value()).isEqualTo("Herren Elite");
    }

    @Test
    void compareTo_ordersByValueAscending() {
        ClassResultName a = ClassResultName.of("A-Klasse");
        ClassResultName b = ClassResultName.of("B-Klasse");

        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
        assertThat(a.compareTo(a)).isEqualTo(0);
    }

    @Test
    void compareTo_nullIsLessThanNonNull() {
        ClassResultName withNull = ClassResultName.of(null);
        ClassResultName withValue = ClassResultName.of("A");

        assertThat(withNull.compareTo(withValue)).isLessThan(0);
        assertThat(withValue.compareTo(withNull)).isGreaterThan(0);
    }

    @Test
    void compareTo_bothNull_returnsZero() {
        assertThat(ClassResultName.of(null).compareTo(ClassResultName.of(null))).isEqualTo(0);
    }

    @Test
    void toString_containsClassNameAndValue() {
        assertThat(ClassResultName.of("H21").toString()).isEqualTo("ClassResultName=H21");
    }
}
