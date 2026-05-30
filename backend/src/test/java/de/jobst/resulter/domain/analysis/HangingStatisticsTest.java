package de.jobst.resulter.domain.analysis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HangingStatisticsTest {

    @Test
    void getHangingRunnerPercentage_calculatesCorrectly() {
        HangingStatistics stats = new HangingStatistics(10, 4, 8, 1, 3, 0.80, 0.82);
        assertThat(stats.getHangingRunnerPercentage()).isEqualTo(40.0);
    }

    @Test
    void getHangingRunnerPercentage_returnsZeroWhenNoRunners() {
        HangingStatistics stats = new HangingStatistics(0, 0, 0, 0, 0, null, null);
        assertThat(stats.getHangingRunnerPercentage()).isEqualTo(0.0);
    }

    @Test
    void getHangingRunnerPercentage_returnsHundredWhenAllHanging() {
        HangingStatistics stats = new HangingStatistics(5, 5, 10, 3, 2, 0.75, 0.78);
        assertThat(stats.getHangingRunnerPercentage()).isEqualTo(100.0);
    }

    @Test
    void toString_includesKeyValues() {
        HangingStatistics stats = new HangingStatistics(10, 4, 8, 1, 3, 0.80, 0.82);
        String s = stats.toString();
        assertThat(s).contains("10");
        assertThat(s).contains("40");
    }

    @Test
    void toString_withNullIndices_containsNA() {
        HangingStatistics stats = new HangingStatistics(3, 1, 2, 0, 1, null, null);
        String s = stats.toString();
        assertThat(s).contains("N/A");
    }
}
