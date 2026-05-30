package de.jobst.resulter.domain.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompareUtilsTest {

    @Test
    void compareUtils_canBeInstantiated() {
        assertThat(new CompareUtils()).isNotNull();
    }

    @Test
    void compareNullable_bothNull_returnsZero() {
        assertThat(CompareUtils.compareNullable(null, null)).isEqualTo(0);
    }

    @Test
    void compareNullable_firstNullSecondNotNull_returnsNegative() {
        assertThat(CompareUtils.compareNullable(null, "b")).isLessThan(0);
    }

    @Test
    void compareNullable_firstNotNullSecondNull_returnsPositive() {
        assertThat(CompareUtils.compareNullable("a", null)).isGreaterThan(0);
    }

    @Test
    void compareNullable_firstLessThanSecond_returnsNegative() {
        assertThat(CompareUtils.compareNullable("a", "b")).isLessThan(0);
    }

    @Test
    void compareNullable_firstGreaterThanSecond_returnsPositive() {
        assertThat(CompareUtils.compareNullable("b", "a")).isGreaterThan(0);
    }

    @Test
    void compareNullable_equalNonNullValues_returnsZero() {
        assertThat(CompareUtils.compareNullable("a", "a")).isEqualTo(0);
    }

    @Test
    void compareNullable_worksWithIntegers() {
        assertThat(CompareUtils.compareNullable(1, 2)).isLessThan(0);
        assertThat(CompareUtils.compareNullable(2, 1)).isGreaterThan(0);
        assertThat(CompareUtils.compareNullable(null, 1)).isLessThan(0);
        assertThat(CompareUtils.compareNullable(1, null)).isGreaterThan(0);
    }
}
