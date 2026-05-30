package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MediaFileValueObjectsTest {

    @Test
    void mediaFileName_of_setsValue() {
        assertThat(MediaFileName.of("photo.jpg").value()).isEqualTo("photo.jpg");
    }

    @Test
    void mediaFileName_compareTo_ordersByValue() {
        assertThat(MediaFileName.of("a.jpg").compareTo(MediaFileName.of("b.jpg"))).isLessThan(0);
        assertThat(MediaFileName.of("z.png").compareTo(MediaFileName.of("a.jpg"))).isGreaterThan(0);
        assertThat(MediaFileName.of("x.jpg").compareTo(MediaFileName.of("x.jpg"))).isEqualTo(0);
    }

    @Test
    void mediaFileName_toString_containsValue() {
        assertThat(MediaFileName.of("img.png").toString()).contains("img.png");
    }

    @Test
    void mediaFileSize_of_setsValue() {
        assertThat(MediaFileSize.of(1024L).value()).isEqualTo(1024L);
    }

    @Test
    void mediaFileSize_compareTo_ordersByValue() {
        assertThat(MediaFileSize.of(100L).compareTo(MediaFileSize.of(200L))).isLessThan(0);
    }

    @Test
    void mediaFileSize_toString_containsValue() {
        assertThat(MediaFileSize.of(512L).toString()).contains("512");
    }

    @Test
    void mediaFileDescription_of_setsValue() {
        assertThat(MediaFileDescription.of("Lauf 2025").value()).isEqualTo("Lauf 2025");
    }

    @Test
    void mediaFileDescription_compareTo_ordersByValue() {
        assertThat(MediaFileDescription.of("A").compareTo(MediaFileDescription.of("B"))).isLessThan(0);
    }

    @Test
    void mediaFileDescription_toString_containsValue() {
        assertThat(MediaFileDescription.of("desc").toString()).contains("desc");
    }

    @Test
    void mediaFileContentType_of_setsValue() {
        assertThat(MediaFileContentType.of("image/jpeg").value()).isEqualTo("image/jpeg");
    }

    @Test
    void mediaFileContentType_compareTo_ordersByValue() {
        assertThat(MediaFileContentType.of("image/jpeg").compareTo(MediaFileContentType.of("image/png"))).isLessThan(0);
    }

    @Test
    void mediaFileContentType_toString_containsValue() {
        assertThat(MediaFileContentType.of("image/gif").toString()).contains("image/gif");
    }
}
