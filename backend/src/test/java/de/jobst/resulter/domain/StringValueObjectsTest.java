package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class StringValueObjectsTest {

    // -------------------------------------------------------------------------
    // CupName
    // -------------------------------------------------------------------------

    @Test
    void cupName_of_setsValue() {
        assertThat(CupName.of("NOR Cup").value()).isEqualTo("NOR Cup");
    }

    @Test
    void cupName_of_throwsForBlank() {
        assertThatIllegalArgumentException().isThrownBy(() -> CupName.of(""));
    }

    @Test
    void cupName_compareTo_alphabetical() {
        assertThat(CupName.of("A Cup").compareTo(CupName.of("B Cup"))).isLessThan(0);
        assertThat(CupName.of("B Cup").compareTo(CupName.of("A Cup"))).isGreaterThan(0);
        assertThat(CupName.of("A Cup").compareTo(CupName.of("A Cup"))).isEqualTo(0);
    }

    @Test
    void cupName_toString_containsClassNameAndValue() {
        assertThat(CupName.of("NOR Cup").toString()).isEqualTo("CupName=NOR Cup");
    }

    // -------------------------------------------------------------------------
    // EventName
    // -------------------------------------------------------------------------

    @Test
    void eventName_of_setsValue() {
        assertThat(EventName.of("A-Lauf").value()).isEqualTo("A-Lauf");
    }

    @Test
    void eventName_of_throwsForBlank() {
        assertThatIllegalArgumentException().isThrownBy(() -> EventName.of(""));
    }

    @Test
    void eventName_compareTo_alphabetical() {
        assertThat(EventName.of("A-Lauf").compareTo(EventName.of("B-Lauf"))).isLessThan(0);
        assertThat(EventName.of("A-Lauf").compareTo(EventName.of("A-Lauf"))).isEqualTo(0);
    }

    @Test
    void eventName_toString_containsClassNameAndValue() {
        assertThat(EventName.of("A-Lauf").toString()).isEqualTo("EventName=A-Lauf");
    }

    // -------------------------------------------------------------------------
    // ControlCode
    // -------------------------------------------------------------------------

    @Test
    void controlCode_of_setsValue() {
        assertThat(ControlCode.of("31").value()).isEqualTo("31");
    }

    @Test
    void controlCode_of_allowsNull() {
        assertThat(ControlCode.of(null).value()).isNull();
    }

    @Test
    void controlCode_compareTo_alphabetical() {
        assertThat(ControlCode.of("31").compareTo(ControlCode.of("32"))).isLessThan(0);
        assertThat(ControlCode.of("32").compareTo(ControlCode.of("31"))).isGreaterThan(0);
        assertThat(ControlCode.of("31").compareTo(ControlCode.of("31"))).isEqualTo(0);
    }

    @Test
    void controlCode_compareTo_nullSortsLast() {
        assertThat(ControlCode.of("31").compareTo(ControlCode.of(null))).isLessThan(0);
        assertThat(ControlCode.of(null).compareTo(ControlCode.of("31"))).isGreaterThan(0);
    }

    @Test
    void controlCode_toString_containsClassNameAndValue() {
        assertThat(ControlCode.of("31").toString()).isEqualTo("ControlCode=31");
    }

    // -------------------------------------------------------------------------
    // ClassResultShortName
    // -------------------------------------------------------------------------

    @Test
    void classResultShortName_of_setsValue() {
        assertThat(ClassResultShortName.of("H21").value()).isEqualTo("H21");
    }

    @Test
    void classResultShortName_compareTo_alphabetical() {
        assertThat(ClassResultShortName.of("H19").compareTo(ClassResultShortName.of("H21"))).isLessThan(0);
        assertThat(ClassResultShortName.of("H21").compareTo(ClassResultShortName.of("H21"))).isEqualTo(0);
    }

    @Test
    void classResultShortName_toString_containsClassNameAndValue() {
        assertThat(ClassResultShortName.of("H21").toString()).isEqualTo("ClassResultShortName=H21");
    }

    // -------------------------------------------------------------------------
    // Country — dritte of()-Variante mit CountryCode/CountryName
    // -------------------------------------------------------------------------

    @Test
    void country_of_countryCodeAndName_createsWithEmptyId() {
        CountryCode code = CountryCode.of("NOR");
        CountryName name = CountryName.of("Norway");
        Country c = Country.of(code, name);
        assertThat(c.getId().isPersistent()).isFalse();
        assertThat(c.getCode()).isEqualTo(code);
    }

    @Test
    void country_equals_sameObjectIsTrue() {
        Country c = Country.of("NOR", "Norway");
        assertThat(c.equals(c)).isTrue();
    }

    @Test
    void country_equals_nullIsFalse() {
        Country c = Country.of("NOR", "Norway");
        assertThat(c.equals(null)).isFalse();
    }

    @Test
    void country_equals_differentTypeIsFalse() {
        Country c = Country.of("NOR", "Norway");
        assertThat(c.equals("NOR")).isFalse();
    }
}
