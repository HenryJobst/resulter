package de.jobst.resulter.domain.util;

import de.jobst.resulter.domain.PersonId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassResultShortNameScoreSummaryTest {

    @Test
    void constructor_setsScoreAndId() {
        PersonId id = PersonId.of(42L);
        ClassResultShortNameScoreSummary summary = new ClassResultShortNameScoreSummary(10.0, id);

        assertThat(summary.getScore()).isEqualTo(10.0);
        assertThat(summary.getId()).isEqualTo(id);
    }

    @Test
    void sumScore_addsToExistingScore() {
        PersonId id = PersonId.of(1L);
        ClassResultShortNameScoreSummary summary = new ClassResultShortNameScoreSummary(5.0, id);

        summary.sumScore(3.0);

        assertThat(summary.getScore()).isEqualTo(8.0);
    }

    @Test
    void sumScore_multipleAdditions_accumulatesCorrectly() {
        PersonId id = PersonId.of(1L);
        ClassResultShortNameScoreSummary summary = new ClassResultShortNameScoreSummary(0.0, id);

        summary.sumScore(2.5);
        summary.sumScore(3.5);
        summary.sumScore(1.0);

        assertThat(summary.getScore()).isEqualTo(7.0);
    }

    @Test
    void toString_containsScoreAndId() {
        PersonId id = PersonId.of(7L);
        ClassResultShortNameScoreSummary summary = new ClassResultShortNameScoreSummary(12.5, id);

        String result = summary.toString();

        assertThat(result).contains("12");
        assertThat(result).contains("7");
    }
}
