package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.PunchTime;
import de.jobst.resulter.domain.RaceNumber;
import de.jobst.resulter.domain.ResultListId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HangingAnalysisTest {

    private static HangingStatistics emptyStats() {
        return new HangingStatistics(5, 0, 0, 0, 0, null, null);
    }

    private static RunnerHangingProfile hangingProfile() {
        return new RunnerHangingProfile(
                PersonId.of(1L), "H21", RaceNumber.of((byte) 1), PunchTime.of(0.0),
                10, true, new PerformanceIndex(1.10), List.of(), 0.80,
                HangingClassification.MODERATE_HANGING, 5
        );
    }

    @Test
    void getRunnerCount_returnsZeroForEmptyProfiles() {
        HangingAnalysis a = new HangingAnalysis(ResultListId.of(1L), EventId.of(1L), List.of(), emptyStats());
        assertThat(a.getRunnerCount()).isEqualTo(0);
    }

    @Test
    void getRunnerCount_returnsProfileSize() {
        HangingAnalysis a = new HangingAnalysis(ResultListId.of(1L), EventId.of(1L),
                List.of(hangingProfile()), emptyStats());
        assertThat(a.getRunnerCount()).isEqualTo(1);
    }

    @Test
    void toString_containsResultListId() {
        HangingAnalysis a = new HangingAnalysis(ResultListId.of(42L), EventId.of(1L), List.of(), emptyStats());
        assertThat(a.toString()).contains("42");
    }
}
