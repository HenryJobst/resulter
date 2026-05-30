package de.jobst.resulter.domain.analysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class MentalClassificationTest {

    @Test
    void keys_areCorrect() {
        assertThat(MentalClassification.PANIC.getKey()).isEqualTo("panic");
        assertThat(MentalClassification.ICE_MAN.getKey()).isEqualTo("ice_man");
        assertThat(MentalClassification.RESIGNER.getKey()).isEqualTo("resigner");
        assertThat(MentalClassification.CHAIN_ERROR.getKey()).isEqualTo("chain_error");
    }

    @ParameterizedTest
    @EnumSource(MentalClassification.class)
    void getDescription_neverNullOrBlank(MentalClassification cls) {
        assertThat(cls.getDescription()).isNotBlank();
    }

    @Test
    void toString_returnsKey() {
        assertThat(MentalClassification.PANIC.toString()).isEqualTo("panic");
        assertThat(MentalClassification.ICE_MAN.toString()).isEqualTo("ice_man");
    }

    @Test
    void allValues_haveFourEntries() {
        assertThat(MentalClassification.values()).hasSize(4);
    }
}
