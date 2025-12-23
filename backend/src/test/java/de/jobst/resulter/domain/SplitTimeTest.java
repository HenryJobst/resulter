package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SplitTimeTest {

    private final SplitTimeListId splitTimeListId = SplitTimeListId.of(1L);

    @Test
    void compareTo_isEqual() {
        assertThat(SplitTime.of("1", 10.0, splitTimeListId).compareTo(SplitTime.of("1", 10.0,
            splitTimeListId))).isEqualTo(0);
    }

    @Test
    void compareTo_isLowerByControlCode() {
        assertThat(SplitTime.of("1", 10.0, splitTimeListId).compareTo(SplitTime.of("2", 10.0, splitTimeListId))).isEqualTo(-1);
    }

    @Test
    void compareTo_isLowerByPunchTime() {
        assertThat(SplitTime.of("1", 10.0, splitTimeListId).compareTo(SplitTime.of("1", 20.0, splitTimeListId))).isEqualTo(-1);
    }

    @Test
    void compareTo_isGreaterByControlCode() {
        assertThat(SplitTime.of("2", 10.0, splitTimeListId).compareTo(SplitTime.of("1", 10.0, splitTimeListId))).isEqualTo(1);
    }

    @Test
    void compareTo_isGreaterByPunchTime() {
        assertThat(SplitTime.of("1", 20.0, splitTimeListId).compareTo(SplitTime.of("1", 10.0, splitTimeListId))).isEqualTo(1);
    }
}
