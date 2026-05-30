package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MediaFileTest {

    private static MediaFile file(String name, String content, long size) {
        return MediaFile.of(name, "thumb_" + name, content, size);
    }

    @Test
    void of_setsFieldsCorrectly() {
        MediaFile mf = MediaFile.of("photo.jpg", "thumb.jpg", "image/jpeg", 2048L);

        assertThat(mf.getMediaFileName().value()).isEqualTo("photo.jpg");
        assertThat(mf.getThumbnailFileName().value()).isEqualTo("thumb.jpg");
        assertThat(mf.getContentType().value()).isEqualTo("image/jpeg");
        assertThat(mf.getMediaFileSize().value()).isEqualTo(2048L);
        assertThat(mf.getDescription()).isNull();
        assertThat(mf.getId().isPersistent()).isFalse();
    }

    @Test
    void of_withIdAndDescription_setsAllFields() {
        MediaFile mf = MediaFile.of(5L, "photo.jpg", "thumb.jpg", "image/jpeg", 1024L, "Ein Bild");

        assertThat(mf.getId()).isEqualTo(MediaFileId.of(5L));
        assertThat(mf.getDescription().value()).isEqualTo("Ein Bild");
    }

    @Test
    void of_withId_nullDescriptionStaysNull() {
        MediaFile mf = MediaFile.of(3L, "photo.jpg", "thumb.jpg", "image/jpeg", 512L);

        assertThat(mf.getId()).isEqualTo(MediaFileId.of(3L));
        assertThat(mf.getDescription()).isNull();
    }

    @Test
    void update_replacesAllMutableFields() {
        MediaFile mf = file("old.png", "image/png", 100L);

        mf.update(
                MediaFileName.of("new.jpg"),
                MediaFileContentType.of("image/jpeg"),
                MediaFileSize.of(200L),
                MediaFileDescription.of("Updated"));

        assertThat(mf.getMediaFileName().value()).isEqualTo("new.jpg");
        assertThat(mf.getContentType().value()).isEqualTo("image/jpeg");
        assertThat(mf.getMediaFileSize().value()).isEqualTo(200L);
        assertThat(mf.getDescription().value()).isEqualTo("Updated");
    }

    @Test
    void compareTo_ordersByFileName() {
        MediaFile a = file("alpha.jpg", "image/jpeg", 100L);
        MediaFile b = file("beta.jpg", "image/jpeg", 100L);

        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameFileName_ordersByContentType() {
        MediaFile jpeg = MediaFile.of("photo.jpg", "thumb.jpg", "image/jpeg", 100L);
        MediaFile png = MediaFile.of("photo.jpg", "thumb.jpg", "image/png", 100L);

        assertThat(jpeg.compareTo(png)).isLessThan(0); // jpeg < png lexicographically
    }

    @Test
    void compareTo_sameFileAndContent_ordersBySize() {
        MediaFile small = MediaFile.of("photo.jpg", "thumb.jpg", "image/jpeg", 100L);
        MediaFile large = MediaFile.of("photo.jpg", "thumb.jpg", "image/jpeg", 200L);

        assertThat(small.compareTo(large)).isLessThan(0);
    }

    @Test
    void compareTo_sameFileContentSize_ordersByDescription() {
        MediaFile withDesc = MediaFile.of(null, "photo.jpg", "thumb.jpg", "image/jpeg", 100L, "Alpha");
        MediaFile noDesc = MediaFile.of(null, "photo.jpg", "thumb.jpg", "image/jpeg", 100L, null);

        assertThat(withDesc.compareTo(noDesc)).isNotEqualTo(0);
    }

    @Test
    void compareTo_sameFileContentSizeAndDescription_ordersByThumbnail() {
        MediaFile a = MediaFile.of(null, "photo.jpg", "thumb_a.jpg", "image/jpeg", 100L, "desc");
        MediaFile b = MediaFile.of(null, "photo.jpg", "thumb_b.jpg", "image/jpeg", 100L, "desc");

        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameFileContentSizeDescriptionThumbnail_ordersByID() {
        MediaFile id1 = new MediaFile(MediaFileId.of(1L), MediaFileName.of("photo.jpg"),
                MediaFileName.of("thumb.jpg"), MediaFileContentType.of("image/jpeg"),
                MediaFileSize.of(100L), null);
        MediaFile id2 = new MediaFile(MediaFileId.of(2L), MediaFileName.of("photo.jpg"),
                MediaFileName.of("thumb.jpg"), MediaFileContentType.of("image/jpeg"),
                MediaFileSize.of(100L), null);

        assertThat(id1.compareTo(id2)).isLessThan(0);
        assertThat(id2.compareTo(id1)).isGreaterThan(0);
    }

    @Test
    void compareTo_completelyEqualFiles_returnsZero() {
        MediaFile mf1 = new MediaFile(MediaFileId.of(1L), MediaFileName.of("photo.jpg"),
                MediaFileName.of("thumb.jpg"), MediaFileContentType.of("image/jpeg"),
                MediaFileSize.of(100L), MediaFileDescription.of("desc"));
        MediaFile mf2 = new MediaFile(MediaFileId.of(1L), MediaFileName.of("photo.jpg"),
                MediaFileName.of("thumb.jpg"), MediaFileContentType.of("image/jpeg"),
                MediaFileSize.of(100L), MediaFileDescription.of("desc"));

        assertThat(mf1.compareTo(mf2)).isEqualTo(0);
    }

    @Test
    void of_withNullId_usesEmptyMediaFileId() {
        MediaFile mf = MediaFile.of(null, "photo.jpg", "thumb.jpg", "image/jpeg", 100L, null);

        assertThat(mf.getId().isPersistent()).isFalse();
    }
}
