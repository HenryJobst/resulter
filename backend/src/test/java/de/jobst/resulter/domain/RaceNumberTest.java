package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RaceNumberTest {

    @Test
    void compareTo_bothNull_returnsZero() {
        RaceNumber a = RaceNumber.of(null);
        RaceNumber b = RaceNumber.of(null);
        assertThat(a.compareTo(b)).isEqualTo(0);
    }

    @Test
    void compareTo_thisNullOtherNonNull_returnsMinusOne() {
        RaceNumber nullRn    = RaceNumber.of(null);
        RaceNumber nonNullRn = RaceNumber.of((byte) 1);
        assertThat(nullRn.compareTo(nonNullRn)).isEqualTo(-1);
    }

    @Test
    void compareTo_thisNonNullOtherNull_returnsOne() {
        RaceNumber nonNullRn = RaceNumber.of((byte) 1);
        RaceNumber nullRn    = RaceNumber.of(null);
        assertThat(nonNullRn.compareTo(nullRn)).isEqualTo(1);
    }

    @Test
    void compareTo_bothNonNull_comparesByValue() {
        RaceNumber rn1 = RaceNumber.of((byte) 1);
        RaceNumber rn2 = RaceNumber.of((byte) 2);
        assertThat(rn1.compareTo(rn2)).isLessThan(0);
        assertThat(rn2.compareTo(rn1)).isGreaterThan(0);
        assertThat(rn1.compareTo(RaceNumber.of((byte) 1))).isEqualTo(0);
    }

    @Test
    void empty_returnsRaceNumber1() {
        assertThat(RaceNumber.empty().value()).isEqualTo((byte) 1);
    }
}
