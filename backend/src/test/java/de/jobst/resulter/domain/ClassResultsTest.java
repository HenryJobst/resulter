package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClassResultsTest {

    @Test
    void of_wrapsCollectionInRecord() {
        ClassResult cr = ClassResult.of("H21", "H21", null, null, null);
        ClassResults results = ClassResults.of(List.of(cr));
        assertThat(results.value()).containsExactly(cr);
    }

    @Test
    void of_emptyCollection_returnsEmpty() {
        ClassResults results = ClassResults.of(List.of());
        assertThat(results.value()).isEmpty();
    }
}
