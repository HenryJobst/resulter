package de.jobst.resulter.domain.analysis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class MriStatisticsTest {

    // -------------------------------------------------------------------------
    // Validierung im Compact Constructor
    // -------------------------------------------------------------------------

    @Test
    void constructor_throwsWhenTotalRunnersNegative() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MriStatistics(-1, 0, 0, 0, 0, 0, null, null));
    }

    @Test
    void constructor_throwsWhenRunnersWithMistakesExceedsTotal() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MriStatistics(5, 6, 0, 0, 0, 0, null, null));
    }

    @Test
    void constructor_throwsWhenTotalMistakesNegative() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MriStatistics(5, 2, -1, 0, 0, 0, null, null));
    }

    @Test
    void constructor_throwsWhenReactionCountsNegative() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MriStatistics(5, 2, 3, -1, 0, 0, null, null));
    }

    // -------------------------------------------------------------------------
    // Berechnete Prozentsätze
    // -------------------------------------------------------------------------

    @Test
    void getPercentageWithMistakes_calculatesCorrectly() {
        MriStatistics stats = new MriStatistics(10, 4, 5, 1, 2, 1, -0.05, 0.0);
        assertThat(stats.getPercentageWithMistakes()).isEqualTo(40.0);
    }

    @Test
    void getPercentageWithMistakes_returnsZeroWhenNoRunners() {
        MriStatistics stats = new MriStatistics(0, 0, 0, 0, 0, 0, null, null);
        assertThat(stats.getPercentageWithMistakes()).isEqualTo(0.0);
    }

    @Test
    void getPercentagePanic_calculatesCorrectly() {
        // 2 panic out of 4 total reactions
        MriStatistics stats = new MriStatistics(10, 4, 5, 2, 1, 1, null, null);
        assertThat(stats.getPercentagePanic()).isEqualTo(50.0);
    }

    @Test
    void getPercentageIceMan_calculatesCorrectly() {
        MriStatistics stats = new MriStatistics(10, 4, 5, 1, 2, 1, null, null);
        assertThat(stats.getPercentageIceMan()).isEqualTo(50.0);
    }

    @Test
    void getPercentageResigner_calculatesCorrectly() {
        MriStatistics stats = new MriStatistics(10, 4, 5, 1, 1, 2, null, null);
        assertThat(stats.getPercentageResigner()).isEqualTo(50.0);
    }

    @Test
    void getPercentages_returnZeroWhenNoReactions() {
        MriStatistics stats = new MriStatistics(5, 0, 0, 0, 0, 0, null, null);
        assertThat(stats.getPercentagePanic()).isEqualTo(0.0);
        assertThat(stats.getPercentageIceMan()).isEqualTo(0.0);
        assertThat(stats.getPercentageResigner()).isEqualTo(0.0);
    }

    @Test
    void getTotalReactions_sumsPanicIceManResigner() {
        MriStatistics stats = new MriStatistics(10, 4, 5, 2, 1, 3, null, null);
        assertThat(stats.getTotalReactions()).isEqualTo(6);
    }

    @Test
    void hasMistakes_returnsTrueWhenMistakesExist() {
        MriStatistics stats = new MriStatistics(10, 4, 5, 1, 1, 1, -0.1, 0.0);
        assertThat(stats.hasMistakes()).isTrue();
    }

    @Test
    void hasMistakes_returnsFalseWhenNoMistakes() {
        MriStatistics stats = new MriStatistics(10, 0, 0, 0, 0, 0, null, null);
        assertThat(stats.hasMistakes()).isFalse();
    }
}
