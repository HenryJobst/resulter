package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.PersonId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerSplitTest {

    private static RunnerSplit split(double splitTime) {
        return new RunnerSplit(PersonId.of(1L), "H21", 1, splitTime, 0.0, false);
    }

    @Test
    void compareTo_ordersBySpitTimeAscending() {
        RunnerSplit faster = split(90.0);
        RunnerSplit slower = split(120.0);

        assertThat(faster.compareTo(slower)).isLessThan(0);
        assertThat(slower.compareTo(faster)).isGreaterThan(0);
        assertThat(faster.compareTo(faster)).isEqualTo(0);
    }
}
