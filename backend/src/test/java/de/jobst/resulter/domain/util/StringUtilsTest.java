package de.jobst.resulter.domain.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @Test
    void stringUtils_canBeInstantiated() {
        assertThat(new StringUtils()).isNotNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void formatAsName_blankOrNullInput_returnsEmpty(String input) {
        assertThat(StringUtils.formatAsName(input)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "max,       Max",
        "HANS,      Hans",
        "müller,    Müller",
        "anna-maria, Anna-Maria",
        "o'brien,   O'Brien",
        "abc/def,   Abc/Def",
        "abc+def,   Abc+Def",
    })
    void formatAsName_variousInputs_formatsCorrectly(String input, String expected) {
        assertThat(StringUtils.formatAsName(input)).isEqualTo(expected.strip());
    }

    @Test
    void formatAsName_multipleSpaces_collapsedToOne() {
        assertThat(StringUtils.formatAsName("max   mueller")).isEqualTo("Max Mueller");
    }

    @Test
    void formatAsName_spaceBeforeJoiner_removesTrailingSpace() {
        assertThat(StringUtils.formatAsName("anna - maria")).isEqualTo("Anna-Maria");
    }

    @Test
    void formatAsName_spaceAfterJoiner_isSkipped() {
        assertThat(StringUtils.formatAsName("anna- maria")).isEqualTo("Anna-Maria");
    }

    @Test
    void formatAsName_singleWord_capitalized() {
        assertThat(StringUtils.formatAsName("claude")).isEqualTo("Claude");
    }

    @Test
    void formatAsName_alreadyCapitalized_unchanged() {
        assertThat(StringUtils.formatAsName("Max Mueller")).isEqualTo("Max Mueller");
    }

    @Test
    void formatAsName_trailingSpaces_trimmed() {
        assertThat(StringUtils.formatAsName("  max  ")).isEqualTo("Max");
    }

    @Test
    void formatAsName_apostropheJoiner_capitalizesAfter() {
        assertThat(StringUtils.formatAsName("d'angelo")).isEqualTo("D'Angelo");
    }

    @Test
    void formatAsName_slashJoiner_capitalizesAfter() {
        assertThat(StringUtils.formatAsName("d/h21")).isEqualTo("D/H21");
    }
}
