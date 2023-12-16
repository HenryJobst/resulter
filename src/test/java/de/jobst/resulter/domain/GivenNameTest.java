package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GivenNameTest {

    @Test
    void testGivenNameWithSlash() {
        assertThat(GivenName.of("hans/paul").value()).isEqualTo("Hans/Paul");
    }
}