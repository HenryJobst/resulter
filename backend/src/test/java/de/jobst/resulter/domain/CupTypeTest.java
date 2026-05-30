package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CupTypeTest {

    // -------------------------------------------------------------------------
    // fromValue
    // -------------------------------------------------------------------------

    @Test
    void fromValue_returnsCorrectEnumForEachValue() {
        assertThat(CupType.fromValue("ADD")).isEqualTo(CupType.ADD);
        assertThat(CupType.fromValue("NOR")).isEqualTo(CupType.NOR);
        assertThat(CupType.fromValue("KJ")).isEqualTo(CupType.KJ);
        assertThat(CupType.fromValue("NEBEL")).isEqualTo(CupType.NEBEL);
        assertThat(CupType.fromValue("KRISTALL")).isEqualTo(CupType.KRISTALL);
    }

    @Test
    void fromValue_throwsIllegalArgumentExceptionForUnknownValue() {
        assertThatIllegalArgumentException().isThrownBy(() -> CupType.fromValue("UNKNOWN"));
        assertThatIllegalArgumentException().isThrownBy(() -> CupType.fromValue("nor"));
        assertThatIllegalArgumentException().isThrownBy(() -> CupType.fromValue(""));
    }

    @Test
    void value_returnsStringRepresentation() {
        assertThat(CupType.ADD.value()).isEqualTo("ADD");
        assertThat(CupType.NOR.value()).isEqualTo("NOR");
        assertThat(CupType.KJ.value()).isEqualTo("KJ");
        assertThat(CupType.NEBEL.value()).isEqualTo("NEBEL");
        assertThat(CupType.KRISTALL.value()).isEqualTo("KRISTALL");
    }

    // -------------------------------------------------------------------------
    // isGroupedByOrganisation
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"KJ", "NEBEL", "KRISTALL"})
    void isGroupedByOrganisation_returnsTrueForGroupedTypes(String value) {
        assertThat(CupType.fromValue(value).isGroupedByOrganisation()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"NOR", "ADD"})
    void isGroupedByOrganisation_returnsFalseForNonGroupedTypes(String value) {
        assertThat(CupType.fromValue(value).isGroupedByOrganisation()).isFalse();
    }
}
