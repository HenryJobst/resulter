package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.ControlCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnomaliesIndexInformationTest {

    private static AnomaliesIndexInformation info(AnomalyClassification cls) {
        return new AnomaliesIndexInformation(
                1,
                ControlCode.of("31"),
                ControlCode.of("32"),
                new PerformanceIndex(1.20),
                new AnomaliesIndex(1, null, new PerformanceIndex(1.20), new PerformanceIndex(1.10), 1.20 / 1.10, cls, 120.0),
                cls,
                120.0,
                100.0
        );
    }

    @Test
    void isHighSuspicion_returnsTrueOnlyForHighSuspicion() {
        assertThat(info(AnomalyClassification.HIGH_SUSPICION).isHighSuspicion()).isTrue();
        assertThat(info(AnomalyClassification.MODERATE_SUSPICION).isHighSuspicion()).isFalse();
        assertThat(info(AnomalyClassification.NO_SUSPICION).isHighSuspicion()).isFalse();
    }

    @Test
    void isModerateSuspicion_returnsTrueOnlyForModerateSuspicion() {
        assertThat(info(AnomalyClassification.MODERATE_SUSPICION).isModerateSuspicion()).isTrue();
        assertThat(info(AnomalyClassification.HIGH_SUSPICION).isModerateSuspicion()).isFalse();
        assertThat(info(AnomalyClassification.NO_SUSPICION).isModerateSuspicion()).isFalse();
    }
}
