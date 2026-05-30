package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BirthDateTest {

    private static final LocalDate DATE_1990 = LocalDate.of(1990, 1, 1);
    private static final LocalDate DATE_2000 = LocalDate.of(2000, 1, 1);

    @Test
    void compareTo_bothNull_returnsMinusOne() {
        // BirthDate(null).compareTo(BirthDate(null)) → value==null branch → -1
        BirthDate a = BirthDate.of(null);
        BirthDate b = BirthDate.of(null);
        assertThat(a.compareTo(b)).isEqualTo(-1);
    }

    @Test
    void compareTo_thisNullOtherNonNull_returnsMinusOne() {
        BirthDate a = BirthDate.of(null);
        BirthDate b = BirthDate.of(DATE_1990);
        assertThat(a.compareTo(b)).isEqualTo(-1);
    }

    @Test
    void compareTo_thisNonNullOtherNull_returnsOne() {
        BirthDate a = BirthDate.of(DATE_1990);
        BirthDate b = BirthDate.of(null);
        assertThat(a.compareTo(b)).isEqualTo(1);
    }

    @Test
    void compareTo_bothNonNull_comparesByDate() {
        BirthDate earlier = BirthDate.of(DATE_1990);
        BirthDate later   = BirthDate.of(DATE_2000);
        assertThat(earlier.compareTo(later)).isLessThan(0);
        assertThat(later.compareTo(earlier)).isGreaterThan(0);
        assertThat(earlier.compareTo(BirthDate.of(DATE_1990))).isEqualTo(0);
    }
}
