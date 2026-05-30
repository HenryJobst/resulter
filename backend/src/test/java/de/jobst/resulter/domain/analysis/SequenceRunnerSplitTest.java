package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.PersonId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SequenceRunnerSplitTest {

    private static SequenceRunnerSplit split(Double splitTime) {
        return new SequenceRunnerSplit(PersonId.of(1L), "H21", 1, splitTime, 0.0, List.of());
    }

    @Test
    void compareTo_ordersBySpitTimeAscending() {
        SequenceRunnerSplit faster = split(90.0);
        SequenceRunnerSplit slower = split(120.0);

        assertThat(faster.compareTo(slower)).isLessThan(0);
        assertThat(slower.compareTo(faster)).isGreaterThan(0);
        assertThat(faster.compareTo(faster)).isEqualTo(0);
    }

    @Test
    void compareTo_bothNullSplitTime_returnsZero() {
        assertThat(split(null).compareTo(split(null))).isEqualTo(0);
    }

    @Test
    void compareTo_nullSplitTimeIsLast() {
        SequenceRunnerSplit withTime = split(100.0);
        SequenceRunnerSplit withoutTime = split(null);

        // null → sorts after non-null (returns 1)
        assertThat(withoutTime.compareTo(withTime)).isGreaterThan(0);
        assertThat(withTime.compareTo(withoutTime)).isLessThan(0);
    }
}
