package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.ControlCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class MistakeReactionPairTest {

    private static MistakeReactionPair pair(int mistakeLeg, MentalClassification classification) {
        return new MistakeReactionPair(
                mistakeLeg,
                ControlCode.of("31"),
                ControlCode.of("32"),
                new PerformanceIndex(1.50),
                mistakeLeg + 1,
                ControlCode.of("32"),
                ControlCode.of("33"),
                new PerformanceIndex(1.20),
                new MentalResilienceIndex(-0.10),
                classification
        );
    }

    @Test
    void constructor_throwsWhenReactionLegNotImmediatelyAfterMistakeLeg() {
        assertThatIllegalArgumentException().isThrownBy(() -> new MistakeReactionPair(
                2,
                ControlCode.of("31"), ControlCode.of("32"), new PerformanceIndex(1.50),
                4,
                ControlCode.of("32"), ControlCode.of("33"), new PerformanceIndex(1.20),
                new MentalResilienceIndex(-0.10),
                MentalClassification.PANIC
        ));
    }

    @Test
    void constructor_acceptsConsecutiveLegs() {
        MistakeReactionPair p = pair(3, MentalClassification.ICE_MAN);
        assertThat(p.mistakeLegNumber()).isEqualTo(3);
        assertThat(p.reactionLegNumber()).isEqualTo(4);
    }

    @Test
    void getMistakeSeverity_returnsModerateForPI130to149() {
        MistakeReactionPair p = new MistakeReactionPair(
                1, ControlCode.of("31"), ControlCode.of("32"), new PerformanceIndex(1.40),
                2, ControlCode.of("32"), ControlCode.of("33"), new PerformanceIndex(1.10),
                new MentalResilienceIndex(0.0), MentalClassification.ICE_MAN);
        assertThat(p.getMistakeSeverity()).isEqualTo("moderate");
    }

    @Test
    void getMistakeSeverity_returnsMajorForPI150to199() {
        MistakeReactionPair p = new MistakeReactionPair(
                1, ControlCode.of("31"), ControlCode.of("32"), new PerformanceIndex(1.75),
                2, ControlCode.of("32"), ControlCode.of("33"), new PerformanceIndex(1.10),
                new MentalResilienceIndex(0.0), MentalClassification.ICE_MAN);
        assertThat(p.getMistakeSeverity()).isEqualTo("major");
    }

    @Test
    void getMistakeSeverity_returnsSevereForPI200plus() {
        MistakeReactionPair p = new MistakeReactionPair(
                1, ControlCode.of("31"), ControlCode.of("32"), new PerformanceIndex(2.50),
                2, ControlCode.of("32"), ControlCode.of("33"), new PerformanceIndex(1.10),
                new MentalResilienceIndex(0.0), MentalClassification.ICE_MAN);
        assertThat(p.getMistakeSeverity()).isEqualTo("severe");
    }

    @Test
    void isPanicReaction_returnsTrueOnlyForPanic() {
        assertThat(pair(1, MentalClassification.PANIC).isPanicReaction()).isTrue();
        assertThat(pair(1, MentalClassification.ICE_MAN).isPanicReaction()).isFalse();
        assertThat(pair(1, MentalClassification.RESIGNER).isPanicReaction()).isFalse();
    }

    @Test
    void isStableReaction_returnsTrueOnlyForIceMan() {
        assertThat(pair(1, MentalClassification.ICE_MAN).isStableReaction()).isTrue();
        assertThat(pair(1, MentalClassification.PANIC).isStableReaction()).isFalse();
        assertThat(pair(1, MentalClassification.RESIGNER).isStableReaction()).isFalse();
    }

    @Test
    void isResignationReaction_returnsTrueOnlyForResigner() {
        assertThat(pair(1, MentalClassification.RESIGNER).isResignationReaction()).isTrue();
        assertThat(pair(1, MentalClassification.PANIC).isResignationReaction()).isFalse();
        assertThat(pair(1, MentalClassification.ICE_MAN).isResignationReaction()).isFalse();
    }
}
