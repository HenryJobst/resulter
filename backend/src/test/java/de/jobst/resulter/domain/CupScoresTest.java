package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CupScoresTest {

    private static CupScore score(long personId, double points) {
        return new CupScore(PersonId.of(personId), OrganisationId.of(1L), ClassResultShortName.of("H21"), points);
    }

    // -------------------------------------------------------------------------
    // of() und Grundstruktur
    // -------------------------------------------------------------------------

    @Test
    void of_withEmptyMap_createsEmptyCupScores() {
        CupScores scores = CupScores.of(new HashMap<>());

        assertThat(scores.values()).isEmpty();
    }

    @Test
    void of_withInitialEntry_makesItAccessible() {
        CupScore norScore = score(1L, 12.0);
        CupScores scores = CupScores.of(new HashMap<>(Map.of(CupType.NOR, norScore)));

        assertThat(scores.get(CupType.NOR)).isEqualTo(norScore);
    }

    // -------------------------------------------------------------------------
    // add()
    // -------------------------------------------------------------------------

    @Test
    void add_insertsNewScore() {
        CupScores scores = CupScores.of(new HashMap<>());
        CupScore norScore = score(1L, 10.0);

        scores.add(CupType.NOR, norScore);

        assertThat(scores.get(CupType.NOR)).isEqualTo(norScore);
    }

    @Test
    void add_replacesExistingScore() {
        CupScores scores = CupScores.of(new HashMap<>(Map.of(CupType.NOR, score(1L, 10.0))));
        CupScore newScore = score(2L, 12.0);

        scores.add(CupType.NOR, newScore);

        assertThat(scores.get(CupType.NOR)).isEqualTo(newScore);
    }

    // -------------------------------------------------------------------------
    // get()
    // -------------------------------------------------------------------------

    @Test
    void get_returnsNullWhenKeyAbsent() {
        CupScores scores = CupScores.of(new HashMap<>());

        assertThat(scores.get(CupType.NOR)).isNull();
    }

    // -------------------------------------------------------------------------
    // values()
    // -------------------------------------------------------------------------

    @Test
    void values_returnsAllAddedScores() {
        CupScore norScore = score(1L, 12.0);
        CupScore kjScore = score(2L, 8.0);
        CupScores scores = CupScores.of(new HashMap<>(Map.of(CupType.NOR, norScore, CupType.KJ, kjScore)));

        assertThat(scores.values()).containsExactlyInAnyOrder(norScore, kjScore);
    }
}
