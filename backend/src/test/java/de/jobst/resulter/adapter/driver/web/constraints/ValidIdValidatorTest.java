package de.jobst.resulter.adapter.driver.web.constraints;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidIdValidatorTest {

    private final ValidIdValidator validator = new ValidIdValidator();

    @Test
    void isValid_withNull_returnsTrue() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void isValid_withPositiveValue_returnsTrue() {
        assertThat(validator.isValid(1L, null)).isTrue();
        assertThat(validator.isValid(100L, null)).isTrue();
    }

    @Test
    void isValid_withZero_returnsFalse() {
        assertThat(validator.isValid(0L, null)).isFalse();
    }

    @Test
    void isValid_withNegativeValue_returnsFalse() {
        assertThat(validator.isValid(-1L, null)).isFalse();
    }
}
