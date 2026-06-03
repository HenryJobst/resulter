package de.jobst.resulter.springapp.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdditionalStatusCodesTest {

    @Test
    void resolve_returnsEnum_whenCodeMatches() {
        assertThat(AdditionalStatusCodes.resolve(1001)).isEqualTo(AdditionalStatusCodes.UNEXPECTED);
    }

    @Test
    void resolve_returnsNull_whenCodeDoesNotMatch() {
        assertThat(AdditionalStatusCodes.resolve(9999)).isNull();
    }

    @Test
    void valueOf_int_returnsEnum_whenCodeMatches() {
        assertThat(AdditionalStatusCodes.valueOf(1001)).isEqualTo(AdditionalStatusCodes.UNEXPECTED);
    }

    @Test
    void valueOf_int_throws_whenCodeDoesNotMatch() {
        assertThatThrownBy(() -> AdditionalStatusCodes.valueOf(9999))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void value_returnsNumericCode() {
        assertThat(AdditionalStatusCodes.UNEXPECTED.value()).isEqualTo(1001);
    }
}
