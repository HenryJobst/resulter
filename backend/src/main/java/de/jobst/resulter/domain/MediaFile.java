package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.Nullable;

@Entity
@Getter
public class MediaFile implements Comparable<MediaFile> {

    @Identity
    @Setter
    private MediaFileId id;

    private MediaFileName mediaFileName;

    private final MediaFileName thumbnailFileName;

    private MediaFileContentType contentType;

    private MediaFileSize mediaFileSize;

    @Nullable
    @Setter
    private MediaFileDescription description;

    public MediaFile(
            MediaFileId id,
            MediaFileName mediaFileName,
            MediaFileName thumbnailFileName,
            MediaFileContentType contentType,
            MediaFileSize mediaFileSize,
            @Nullable MediaFileDescription description) {
        this.id = id;
        this.mediaFileName = mediaFileName;
        this.thumbnailFileName = thumbnailFileName;
        this.contentType = contentType;
        this.mediaFileSize = mediaFileSize;
        this.description = description;
    }

    public static MediaFile of(
            String fileName,
            String thumbnailFileName,
            String contentType,
            Long fileSize) {
        return MediaFile.of(fileName, thumbnailFileName, contentType, fileSize, null);
    }

    public static MediaFile of(
        Long id,
        String fileName,
        String thumbnailFileName,
        String contentType,
        Long fileSize) {
        return MediaFile.of(id, fileName, thumbnailFileName, contentType, fileSize, null);
    }

    public static MediaFile of(
            String fileName,
            String thumbnailFileName,
            String contentType,
            Long fileSize,
            @Nullable String description) {
        return MediaFile.of(null, fileName, thumbnailFileName, contentType, fileSize, description);
    }

    public static MediaFile of(
            @Nullable Long id,
            String fileName,
            String thumbnailFileName,
            String contentType,
            Long fileSize,
            @Nullable String description) {
        return new MediaFile(
                id == null ? MediaFileId.empty() : MediaFileId.of(id),
                MediaFileName.of(fileName),
                MediaFileName.of(thumbnailFileName),
                MediaFileContentType.of(contentType),
                MediaFileSize.of(fileSize),
                description == null ? null : MediaFileDescription.of(description));
    }

    public void update(
            MediaFileName mediaFileName,
            MediaFileContentType mediaFileContentType,
            MediaFileSize mediaFileSize,
            @Nullable MediaFileDescription mediaFileDescription) {
        this.mediaFileName = mediaFileName;
        this.contentType = mediaFileContentType;
        this.mediaFileSize = mediaFileSize;
        this.description = mediaFileDescription;
    }

    @Override
    public int compareTo(MediaFile o) {
        int val = ObjectUtils.compare(this.mediaFileName.value(), o.mediaFileName.value());
        if (val == 0) {
            val = ObjectUtils.compare(this.contentType.value(), o.contentType.value());
        }
        if (val == 0) {
            val = ObjectUtils.compare(this.mediaFileSize.value(), o.mediaFileSize.value());
        }
        if (val == 0) {
            val = ObjectUtils.compare(
                    this.description != null ? this.description.value() : null,
                    o.description != null ? o.description.value() : null);
        }
        if (val == 0) {
            val = ObjectUtils.compare(this.thumbnailFileName.value(), o.thumbnailFileName.value());
        }
        if (val == 0) {
            val = ObjectUtils.compare(this.id.value(), o.id.value());
        }
        return val;
    }
}
