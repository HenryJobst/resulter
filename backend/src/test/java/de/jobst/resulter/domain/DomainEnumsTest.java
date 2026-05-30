package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Testet die fromValue()-Methoden und Geschäftslogik der Domain-Enums:
 * EventClassification, EventClassStatus, EventForm, EventStatus, Discipline.
 */
class DomainEnumsTest {

    // -------------------------------------------------------------------------
    // EventClassification
    // -------------------------------------------------------------------------

    @Test
    void eventClassification_fromValue_returnsCorrectEnum() {
        assertThat(EventClassification.fromValue("International")).isEqualTo(EventClassification.INTERNATIONAL);
        assertThat(EventClassification.fromValue("National")).isEqualTo(EventClassification.NATIONAL);
        assertThat(EventClassification.fromValue("Regional")).isEqualTo(EventClassification.REGIONAL);
        assertThat(EventClassification.fromValue("Local")).isEqualTo(EventClassification.LOCAL);
        assertThat(EventClassification.fromValue("Club")).isEqualTo(EventClassification.CLUB);
    }

    @Test
    void eventClassification_fromValue_throwsForUnknownValue() {
        assertThatIllegalArgumentException().isThrownBy(() -> EventClassification.fromValue("unknown"));
    }

    @Test
    void eventClassification_value_returnsString() {
        assertThat(EventClassification.INTERNATIONAL.value()).isEqualTo("International");
        assertThat(EventClassification.NATIONAL.value()).isEqualTo("National");
    }

    // -------------------------------------------------------------------------
    // EventClassStatus
    // -------------------------------------------------------------------------

    @Test
    void eventClassStatus_fromValue_returnsCorrectEnum() {
        assertThat(EventClassStatus.fromValue("Normal")).isEqualTo(EventClassStatus.NORMAL);
        assertThat(EventClassStatus.fromValue("Divided")).isEqualTo(EventClassStatus.DIVIDED);
        assertThat(EventClassStatus.fromValue("Joined")).isEqualTo(EventClassStatus.JOINED);
        assertThat(EventClassStatus.fromValue("Invalidated")).isEqualTo(EventClassStatus.INVALIDATED);
        assertThat(EventClassStatus.fromValue("InvalidatedNoFee")).isEqualTo(EventClassStatus.INVALIDATED_NO_FEE);
    }

    @Test
    void eventClassStatus_fromValue_throwsForUnknownValue() {
        assertThatIllegalArgumentException().isThrownBy(() -> EventClassStatus.fromValue("unknown"));
    }

    @Test
    void eventClassStatus_value_returnsString() {
        assertThat(EventClassStatus.NORMAL.value()).isEqualTo("Normal");
        assertThat(EventClassStatus.INVALIDATED_NO_FEE.value()).isEqualTo("InvalidatedNoFee");
    }

    // -------------------------------------------------------------------------
    // EventForm
    // -------------------------------------------------------------------------

    @Test
    void eventForm_fromValue_returnsCorrectEnum() {
        assertThat(EventForm.fromValue("Individual")).isEqualTo(EventForm.INDIVIDUAL);
        assertThat(EventForm.fromValue("Team")).isEqualTo(EventForm.TEAM);
        assertThat(EventForm.fromValue("Relay")).isEqualTo(EventForm.RELAY);
    }

    @Test
    void eventForm_fromValue_throwsForUnknownValue() {
        assertThatIllegalArgumentException().isThrownBy(() -> EventForm.fromValue("unknown"));
    }

    @Test
    void eventForm_value_returnsString() {
        assertThat(EventForm.INDIVIDUAL.value()).isEqualTo("Individual");
        assertThat(EventForm.RELAY.value()).isEqualTo("Relay");
    }

    // -------------------------------------------------------------------------
    // EventStatus
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"Planned", "Applied", "Proposed", "Sanctioned", "Canceled", "Rescheduled"})
    void eventStatus_fromValue_returnsCorrectEnum(String value) {
        assertThat(EventStatus.fromValue(value)).isNotNull();
        assertThat(EventStatus.fromValue(value).value()).isEqualTo(value);
    }

    @Test
    void eventStatus_fromValue_throwsForUnknownValue() {
        assertThatIllegalArgumentException().isThrownBy(() -> EventStatus.fromValue("unknown"));
    }

    @Test
    void eventStatus_getDefault_returnsSanctioned() {
        assertThat(EventStatus.getDefault()).isEqualTo(EventStatus.SANCTIONED);
    }

    // -------------------------------------------------------------------------
    // Discipline
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"Sprint", "Middle", "Long", "Ultralong", "Other"})
    void discipline_fromValue_returnsCorrectEnum(String value) {
        assertThat(Discipline.fromValue(value)).isNotNull();
        assertThat(Discipline.fromValue(value).value()).isEqualTo(value);
    }

    @Test
    void discipline_fromValue_throwsForUnknownValue() {
        assertThatIllegalArgumentException().isThrownBy(() -> Discipline.fromValue("unknown"));
    }

    @Test
    void discipline_getDefault_returnsLong() {
        assertThat(Discipline.getDefault()).isEqualTo(Discipline.LONG);
    }
}
