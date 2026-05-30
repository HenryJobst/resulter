package de.jobst.resulter.domain.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThat;

class ValueObjectChecksTest {

    @Test
    void valueObjectChecks_canBeInstantiated() {
        assertThat(new ValueObjectChecks()).isNotNull();
    }

    @Test
    void requireNotNull_withNonNullValue_doesNotThrow() {
        assertThatNoException().isThrownBy(() -> ValueObjectChecks.requireNotNull("value"));
    }

    @Test
    void requireNotNull_withNull_throwsIllegalArgument() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ValueObjectChecks.requireNotNull(null))
                .withMessageContaining("must be not empty");
    }

    @Test
    void requireNotEmpty_withNonEmptyString_doesNotThrow() {
        assertThatNoException().isThrownBy(() -> ValueObjectChecks.requireNotEmpty("text"));
    }

    @Test
    void requireNotEmpty_withEmptyString_throwsIllegalArgument() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ValueObjectChecks.requireNotEmpty(""))
                .withMessageContaining("must be not empty");
    }

    @Test
    void requireNotEmpty_withNullString_throwsIllegalArgument() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ValueObjectChecks.requireNotEmpty(null))
                .withMessageContaining("must be not empty");
    }

    @Test
    void requireGreaterZero_withPositiveValue_doesNotThrow() {
        assertThatNoException().isThrownBy(() -> ValueObjectChecks.requireGreaterZero(1L));
    }

    @Test
    void requireGreaterZero_withZero_throwsIllegalArgument() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ValueObjectChecks.requireGreaterZero(0L))
                .withMessageContaining("greater zero");
    }

    @Test
    void requireGreaterZero_withNegative_throwsIllegalArgument() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ValueObjectChecks.requireGreaterZero(-1L))
                .withMessageContaining("greater zero");
    }
}
