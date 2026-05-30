package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CountryTest {

    @Test
    void of_codeAndName_createsWithEmptyId() {
        Country c = Country.of("NOR", "Norway");
        assertThat(c.getId().isPersistent()).isFalse();
        assertThat(c.getCode().value()).isEqualTo("NOR");
        assertThat(c.getName()).isNotNull();
        assertThat(c.getName().value()).isEqualTo("Norway");
    }

    @Test
    void of_idCodeName_createsWithId() {
        Country c = Country.of(5L, "GER", "Germany");
        assertThat(c.getId().value()).isEqualTo(5L);
        assertThat(c.getCode().value()).isEqualTo("GER");
    }

    @Test
    void of_nullId_usesEmptyId() {
        Country c = Country.of(null, "SWE", "Sweden");
        assertThat(c.getId().isPersistent()).isFalse();
    }

    @Test
    void of_codeAndNullName_setsNullNameValue() {
        Country c = Country.of("FIN", null);
        assertThat(c.getName().value()).isNull();
    }

    @Test
    void equals_sameFields_returnsTrue() {
        Country a = Country.of(1L, "NOR", "Norway");
        Country b = Country.of(1L, "NOR", "Norway");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void equals_differentCode_returnsFalse() {
        Country a = Country.of(1L, "NOR", "Norway");
        Country b = Country.of(1L, "GER", "Norway");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void equals_differentId_returnsFalse() {
        Country a = Country.of(1L, "NOR", "Norway");
        Country b = Country.of(2L, "NOR", "Norway");
        assertThat(a).isNotEqualTo(b);
    }
}
