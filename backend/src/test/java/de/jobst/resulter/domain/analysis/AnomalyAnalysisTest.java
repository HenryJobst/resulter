package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.RaceNumber;
import de.jobst.resulter.domain.ResultListId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnomalyAnalysisTest {

    private static RunnerAnomalyProfile runnerProfile(AnomalyClassification cls, double minAI) {
        return new RunnerAnomalyProfile(
                PersonId.of(1L), "H21", RaceNumber.of((byte) 1),
                10, true, new PerformanceIndex(1.10),
                minAI, 3, List.of(), cls
        );
    }

    private static AnomalyAnalysis analysis(List<RunnerAnomalyProfile> profiles) {
        return new AnomalyAnalysis(ResultListId.of(1L), EventId.of(1L), profiles);
    }

    // -------------------------------------------------------------------------
    // hasData / getRunnerCount
    // -------------------------------------------------------------------------

    @Test
    void hasData_returnsFalseWhenNoProfiles() {
        AnomalyAnalysis a = analysis(List.of());
        assertThat(a.hasData()).isFalse();
        assertThat(a.getRunnerCount()).isEqualTo(0);
    }

    @Test
    void hasData_returnsTrueWithProfiles() {
        AnomalyAnalysis a = analysis(List.of(runnerProfile(AnomalyClassification.NO_SUSPICION, 1.0)));
        assertThat(a.hasData()).isTrue();
        assertThat(a.getRunnerCount()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // getHighSuspicionRunners / getModerateSuspicionRunners
    // -------------------------------------------------------------------------

    @Test
    void getHighSuspicionRunners_filtersCorrectly() {
        var high = runnerProfile(AnomalyClassification.HIGH_SUSPICION, 0.60);
        var moderate = runnerProfile(AnomalyClassification.MODERATE_SUSPICION, 0.80);
        var none = runnerProfile(AnomalyClassification.NO_SUSPICION, 1.05);

        AnomalyAnalysis a = analysis(List.of(high, moderate, none));

        assertThat(a.getHighSuspicionRunners()).containsExactly(high);
        assertThat(a.getModerateSuspicionRunners()).containsExactly(moderate);
    }

    @Test
    void getHighSuspicionRunners_returnsEmptyWhenNoMatch() {
        AnomalyAnalysis a = analysis(List.of(runnerProfile(AnomalyClassification.NO_SUSPICION, 1.0)));
        assertThat(a.getHighSuspicionRunners()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getRunnersSortedByAnomaliesIndex
    // -------------------------------------------------------------------------

    @Test
    void getRunnersSortedByAnomaliesIndex_sortsAscending() {
        var high = runnerProfile(AnomalyClassification.HIGH_SUSPICION, 0.60);
        var none = runnerProfile(AnomalyClassification.NO_SUSPICION, 1.10);

        AnomalyAnalysis a = analysis(List.of(none, high));

        List<RunnerAnomalyProfile> sorted = a.getRunnersSortedByAnomaliesIndex();
        assertThat(sorted).containsExactly(high, none);
    }

    @Test
    void toString_containsResultListId() {
        AnomalyAnalysis a = analysis(List.of());
        assertThat(a.toString()).contains("ResultListId=1");
    }
}
