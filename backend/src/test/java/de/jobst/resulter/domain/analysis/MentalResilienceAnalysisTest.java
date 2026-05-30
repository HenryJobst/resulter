package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.ControlCode;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.RaceNumber;
import de.jobst.resulter.domain.ResultListId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MentalResilienceAnalysisTest {

    private static MistakeReactionPair reactionPair(MentalClassification cls) {
        return new MistakeReactionPair(
                1,
                ControlCode.of("31"), ControlCode.of("32"), new PerformanceIndex(1.50),
                2,
                ControlCode.of("32"), ControlCode.of("33"), new PerformanceIndex(1.20),
                new MentalResilienceIndex(-0.10),
                cls
        );
    }

    private static RunnerMentalProfile runner(MentalClassification cls, double avgMri) {
        return new RunnerMentalProfile(
                PersonId.of(1L), "H21", RaceNumber.of((byte) 1),
                10, true, new PerformanceIndex(1.10),
                List.of(reactionPair(cls)), avgMri, cls
        );
    }

    private static MriStatistics statsWithMistakes(int mistakes) {
        return new MriStatistics(10, mistakes > 0 ? 2 : 0, mistakes, 1, 1, 0, -0.05, 0.0);
    }

    private static MentalResilienceAnalysis analysis(List<RunnerMentalProfile> profiles, MriStatistics stats) {
        return new MentalResilienceAnalysis(ResultListId.of(1L), EventId.of(1L), profiles, stats);
    }

    // -------------------------------------------------------------------------
    // hasData / getRunnerCount
    // -------------------------------------------------------------------------

    @Test
    void hasData_returnsFalseWhenNoProfiles() {
        MentalResilienceAnalysis a = analysis(List.of(), statsWithMistakes(0));
        assertThat(a.hasData()).isFalse();
        assertThat(a.getRunnerCount()).isEqualTo(0);
    }

    @Test
    void hasData_returnsTrueWithProfiles() {
        MentalResilienceAnalysis a = analysis(
                List.of(runner(MentalClassification.PANIC, -0.10)),
                statsWithMistakes(2));
        assertThat(a.hasData()).isTrue();
        assertThat(a.getRunnerCount()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // hasMistakes
    // -------------------------------------------------------------------------

    @Test
    void hasMistakes_delegatesToStatistics() {
        assertThat(analysis(List.of(), statsWithMistakes(3)).hasMistakes()).isTrue();
        assertThat(analysis(List.of(), statsWithMistakes(0)).hasMistakes()).isFalse();
    }

    // -------------------------------------------------------------------------
    // getPanicRunners / getIceManRunners / getResignerRunners
    // -------------------------------------------------------------------------

    @Test
    void getPanicRunners_filtersCorrectly() {
        var panic = runner(MentalClassification.PANIC, -0.10);
        var ice = runner(MentalClassification.ICE_MAN, 0.0);
        var resign = runner(MentalClassification.RESIGNER, 0.10);

        MentalResilienceAnalysis a = analysis(List.of(panic, ice, resign), statsWithMistakes(3));

        assertThat(a.getPanicRunners()).containsExactly(panic);
        assertThat(a.getIceManRunners()).containsExactly(ice);
        assertThat(a.getResignerRunners()).containsExactly(resign);
    }

    @Test
    void getPanicRunners_returnsEmptyWhenNoMatch() {
        MentalResilienceAnalysis a = analysis(
                List.of(runner(MentalClassification.ICE_MAN, 0.0)),
                statsWithMistakes(1));
        assertThat(a.getPanicRunners()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getRunnersSortedByMRI
    // -------------------------------------------------------------------------

    @Test
    void getRunnersSortedByMRI_sortsPanicFirst() {
        var panicProfile = new RunnerMentalProfile(
                PersonId.of(1L), "H21", RaceNumber.of((byte) 1), 10, true,
                new PerformanceIndex(1.10), List.of(), -0.20, MentalClassification.PANIC);
        var resignProfile = new RunnerMentalProfile(
                PersonId.of(2L), "H21", RaceNumber.of((byte) 1), 10, true,
                new PerformanceIndex(1.10), List.of(), 0.15, MentalClassification.RESIGNER);

        MentalResilienceAnalysis a = analysis(List.of(resignProfile, panicProfile), statsWithMistakes(2));

        List<RunnerMentalProfile> sorted = a.getRunnersSortedByMRI();
        assertThat(sorted).containsExactly(panicProfile, resignProfile);
    }

    @Test
    void toString_containsResultListId() {
        MentalResilienceAnalysis a = analysis(List.of(), statsWithMistakes(0));
        assertThat(a.toString()).contains("ResultListId=1");
    }
}
