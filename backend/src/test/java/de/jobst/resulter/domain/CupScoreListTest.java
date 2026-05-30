package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CupScoreListTest {

    @Test
    void getDomainKey_returnsCorrectFields() {
        CupScoreList csl = new CupScoreList(
                CupScoreListId.empty(),
                CupId.of(1L),
                ResultListId.of(2L),
                List.of(),
                null,
                null
        );

        CupScoreList.DomainKey dk = csl.getDomainKey();

        assertThat(dk.cupId()).isEqualTo(CupId.of(1L));
        assertThat(dk.resultListId()).isEqualTo(ResultListId.of(2L));
        assertThat(dk.status()).isEqualTo("COMPLETE");
    }
}
