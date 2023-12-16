package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FamilyNameTest {

    @Test
    void testFamiliyNameUppercaseFirst() {
        assertThat(FamilyName.of("müller").value()).isEqualTo("Müller");
    }

    @Test
    void testFamilyNameLowercaseRest() {
        assertThat(FamilyName.of("MÜLLER").value()).isEqualTo("Müller");
    }

    @Test
    void testFamilyNameUppercaseDoubleFirst() {
        assertThat(FamilyName.of("müller-schmidt").value()).isEqualTo("Müller-Schmidt");
    }

    @Test
    void testFamilyNameLowercaseDoubleRest() {
        assertThat(FamilyName.of("MÜLLER-SCHMIDT").value()).isEqualTo("Müller-Schmidt");
    }

    @Test
    void testFamilyNameUppercaseDoubleFirstWithSpace() {
        assertThat(FamilyName.of("müller schmidt").value()).isEqualTo("Müller Schmidt");
    }

    @Test
    void testFamilyNameLowercaseDoubleRestWithSpace() {
        assertThat(FamilyName.of("MÜLLER SCHMIDT").value()).isEqualTo("Müller Schmidt");
    }
}