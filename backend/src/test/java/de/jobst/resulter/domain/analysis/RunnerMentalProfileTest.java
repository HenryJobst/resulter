package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.ControlCode;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.RaceNumber;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerMentalProfileTest {

    private static MistakeReactionPair pair(MentalClassification cls) {
        return new MistakeReactionPair(
                1,
                ControlCode.of("31"), ControlCode.of("32"), new PerformanceIndex(1.50),
                2,
                ControlCode.of("32"), ControlCode.of("33"), new PerformanceIndex(1.20),
                new MentalResilienceIndex(-0.10),
                cls
        );
    }

    private static RunnerMentalProfile profile(List<MistakeReactionPair> pairs, MentalClassification cls, double avgMri) {
        return new RunnerMentalProfile(
                PersonId.of(1L),
                "H21",
                RaceNumber.of((byte) 1),
                10,
                true,
                new PerformanceIndex(1.10),
                pairs,
                avgMri,
                cls
        );
    }

    // -------------------------------------------------------------------------
    // getMistakeCount / hasMistakes
    // -------------------------------------------------------------------------

    @Test
    void getMistakeCount_returnsZeroWhenNoPairs() {
        assertThat(profile(List.of(), MentalClassification.ICE_MAN, 0.0).getMistakeCount()).isEqualTo(0);
    }

    @Test
    void getMistakeCount_returnsListSize() {
        var pairs = List.of(pair(MentalClassification.PANIC), pair(MentalClassification.ICE_MAN));
        assertThat(profile(pairs, MentalClassification.PANIC, -0.05).getMistakeCount()).isEqualTo(2);
    }

    @Test
    void hasMistakes_returnsFalseWhenNoPairs() {
        assertThat(profile(List.of(), MentalClassification.ICE_MAN, 0.0).hasMistakes()).isFalse();
    }

    @Test
    void hasMistakes_returnsTrueWithPairs() {
        assertThat(profile(List.of(pair(MentalClassification.PANIC)), MentalClassification.PANIC, -0.10).hasMistakes()).isTrue();
    }

    // -------------------------------------------------------------------------
    // Reaktions-Zähler
    // -------------------------------------------------------------------------

    @Test
    void getPanicReactionCount_countsOnlyPanic() {
        var pairs = List.of(
                pair(MentalClassification.PANIC),
                pair(MentalClassification.PANIC),
                pair(MentalClassification.ICE_MAN)
        );
        assertThat(profile(pairs, MentalClassification.PANIC, -0.10).getPanicReactionCount()).isEqualTo(2);
    }

    @Test
    void getStableReactionCount_countsOnlyIceMan() {
        var pairs = List.of(
                pair(MentalClassification.ICE_MAN),
                pair(MentalClassification.RESIGNER)
        );
        assertThat(profile(pairs, MentalClassification.RESIGNER, 0.10).getStableReactionCount()).isEqualTo(1);
    }

    @Test
    void getResignationReactionCount_countsOnlyResigner() {
        var pairs = List.of(
                pair(MentalClassification.RESIGNER),
                pair(MentalClassification.RESIGNER),
                pair(MentalClassification.PANIC)
        );
        assertThat(profile(pairs, MentalClassification.RESIGNER, 0.10).getResignationReactionCount()).isEqualTo(2);
    }

    // -------------------------------------------------------------------------
    // compareTo
    // -------------------------------------------------------------------------

    @Test
    void compareTo_ordersByAverageMriAscending() {
        RunnerMentalProfile panic = profile(List.of(), MentalClassification.PANIC, -0.20);
        RunnerMentalProfile resigner = profile(List.of(), MentalClassification.RESIGNER, 0.15);

        assertThat(panic.compareTo(resigner)).isLessThan(0);
        assertThat(resigner.compareTo(panic)).isGreaterThan(0);
        assertThat(panic.compareTo(panic)).isEqualTo(0);
    }

    @Test
    void toString_containsPersonIdAndClass() {
        RunnerMentalProfile p = profile(List.of(), MentalClassification.PANIC, -0.10);
        String s = p.toString();
        assertThat(s).contains("PersonId=1").contains("H21");
    }
}
