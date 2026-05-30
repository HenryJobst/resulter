package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventCertificateTest {

    @Test
    void of_withAllFields_setsCorrectValues() {
        EventCertificate cert = EventCertificate.of(
                5L, "Urkunde 2025", EventId.of(1L), "{\"paragraphs\":[]}", null, true);

        assertThat(cert.getId()).isEqualTo(EventCertificateId.of(5L));
        assertThat(cert.getName().value()).isEqualTo("Urkunde 2025");
        assertThat(cert.getEvent()).isEqualTo(EventId.of(1L));
        assertThat(cert.isPrimary()).isTrue();
    }

    @Test
    void of_withEmptyName_generatesUuidName() {
        EventCertificate cert = EventCertificate.of(1L, "", null, null, null, false);

        assertThat(cert.getName().value()).isNotBlank();
        assertThat(cert.getName().value()).doesNotContain("\"");
    }

    @Test
    void of_withNullName_generatesUuidName() {
        EventCertificate cert = EventCertificate.of(1L, null, null, null, null, false);

        assertThat(cert.getName().value()).isNotBlank();
    }

    @Test
    void of_withNullLayoutDescription_usesDefaultJson() {
        EventCertificate cert = EventCertificate.of(1L, "Test", null, null, null, false);

        assertThat(cert.getLayoutDescription().value()).isEqualTo("{\"paragraphs\" : []}");
    }

    @Test
    void of_withEmptyLayoutDescription_usesDefaultJson() {
        EventCertificate cert = EventCertificate.of(1L, "Test", null, "", null, false);

        assertThat(cert.getLayoutDescription().value()).isEqualTo("{\"paragraphs\" : []}");
    }

    @Test
    void update_replacesAllMutableFields() {
        EventCertificate cert = EventCertificate.of(1L, "Original", null, null, null, false);

        EventCertificateName newName = EventCertificateName.of("Neue Urkunde");
        EventId newEvent = EventId.of(10L);
        EventCertificateLayoutDescription newLayout = EventCertificateLayoutDescription.of("{\"paragraphs\":[{}]}");
        MediaFileId newMedia = MediaFileId.of(5L);

        cert.update(newName, newEvent, newLayout, newMedia, true);

        assertThat(cert.getName()).isEqualTo(newName);
        assertThat(cert.getEvent()).isEqualTo(newEvent);
        assertThat(cert.getLayoutDescription()).isEqualTo(newLayout);
        assertThat(cert.getBlankCertificate()).isEqualTo(newMedia);
        assertThat(cert.isPrimary()).isTrue();
    }
}
