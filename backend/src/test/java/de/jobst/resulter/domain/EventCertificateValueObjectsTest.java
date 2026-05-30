package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EventCertificateValueObjectsTest {

    @Test
    void eventCertificateName_of_setsValue() {
        assertThat(EventCertificateName.of("Urkunde 2025").value()).isEqualTo("Urkunde 2025");
    }

    @Test
    void eventCertificateName_compareTo_ordersByValue() {
        assertThat(EventCertificateName.of("A").compareTo(EventCertificateName.of("B"))).isLessThan(0);
        assertThat(EventCertificateName.of("X").compareTo(EventCertificateName.of("X"))).isEqualTo(0);
    }

    @Test
    void eventCertificateName_toString_containsValue() {
        assertThat(EventCertificateName.of("Test").toString()).contains("Test");
    }

    @Test
    void eventCertificateLayoutDescription_of_setsValue() {
        assertThat(EventCertificateLayoutDescription.of("Layout A").value()).isEqualTo("Layout A");
    }

    @Test
    void eventCertificateLayoutDescription_compareTo_ordersByValue() {
        assertThat(EventCertificateLayoutDescription.of("A").compareTo(EventCertificateLayoutDescription.of("B"))).isLessThan(0);
    }

    @Test
    void eventCertificateLayoutDescription_toString_containsValue() {
        assertThat(EventCertificateLayoutDescription.of("Desc").toString()).contains("Desc");
    }

    @Test
    void eventCertificateStat_of_setsFields() {
        EventCertificateStat stat = EventCertificateStat.of(
                null, EventId.of(1L), PersonId.of(2L), Instant.EPOCH);

        assertThat(stat.getId().isPersistent()).isFalse();
        assertThat(stat.getEvent()).isEqualTo(EventId.of(1L));
        assertThat(stat.getPerson()).isEqualTo(PersonId.of(2L));
    }

    @Test
    void eventCertificateStat_update_replacesFields() {
        EventCertificateStat stat = EventCertificateStat.of(
                null, EventId.of(1L), PersonId.of(2L), Instant.EPOCH);

        Instant now = Instant.now();
        stat.update(EventId.of(3L), PersonId.of(4L), now);

        assertThat(stat.getEvent()).isEqualTo(EventId.of(3L));
        assertThat(stat.getPerson()).isEqualTo(PersonId.of(4L));
        assertThat(stat.getGenerated()).isEqualTo(now);
    }
}
