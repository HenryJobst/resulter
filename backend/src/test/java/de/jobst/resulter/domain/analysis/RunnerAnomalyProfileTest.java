package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.RaceNumber;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerAnomalyProfileTest {

    private static RunnerAnomalyProfile profile(double minAI) {
        return new RunnerAnomalyProfile(
                PersonId.of(1L), "H21", RaceNumber.of((byte) 1),
                10, true, new PerformanceIndex(1.10),
                minAI, 3, List.of(), AnomalyClassification.MODERATE_SUSPICION
        );
    }

    @Test
    void compareTo_lowerMinAIIsLess() {
        RunnerAnomalyProfile more = profile(0.60);
        RunnerAnomalyProfile less = profile(0.90);

        assertThat(more.compareTo(less)).isLessThan(0);
        assertThat(less.compareTo(more)).isGreaterThan(0);
        assertThat(more.compareTo(more)).isEqualTo(0);
    }

    @Test
    void toString_containsPersonIdAndClass() {
        RunnerAnomalyProfile p = profile(0.75);
        String s = p.toString();
        assertThat(s).contains("PersonId=1").contains("H21");
    }
}
