package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class OrganisationTypeTest {

    @ParameterizedTest
    @CsvSource({
        "IOF,           IOF",
        "IOFRegion,     IOF_REGION",
        "NationalFederation, NATIONAL_FEDERATION",
        "NationalRegion, NATIONAL_REGION",
        "Club,          CLUB",
        "School,        SCHOOL",
        "Company,       COMPANY",
        "Military,      MILITARY",
        "Other,         OTHER"
    })
    void fromValue_parsesAllValues(String input, String expectedName) {
        OrganisationType type = OrganisationType.fromValue(input);
        assertThat(type.name()).isEqualTo(expectedName);
    }

    @Test
    void fromValue_throwsForUnknown() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> OrganisationType.fromValue("CLUB")); // wrong case
    }

    @Test
    void value_returnsOriginalString() {
        assertThat(OrganisationType.CLUB.value()).isEqualTo("Club");
        assertThat(OrganisationType.OTHER.value()).isEqualTo("Other");
    }
}
