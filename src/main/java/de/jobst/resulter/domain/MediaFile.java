package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public class MediaFile implements Comparable<MediaFile> {

    @NonNull
    @Setter
    private MediaFileId id;

    @NonNull
    private MediaFileName mediaFileName;

    @NonNull
    private MediaFileName thumbnailFileName;

    @NonNull
    private MediaFileContentType contentType;

    @NonNull
    private MediaFileSize mediaFileSize;

    @Nullable
    @Setter
    private MediaFileDescription description;

    public MediaFile(
            @NonNull MediaFileId id,
            @NonNull MediaFileName mediaFileName,
            @NonNull MediaFileName thumbnailFileName,
            @NonNull MediaFileContentType contentType,
            @NonNull MediaFileSize mediaFileSize,
            @Nullable MediaFileDescription description) {
        this.id = id;
        this.mediaFileName = mediaFileName;
        this.thumbnailFileName = thumbnailFileName;
        this.contentType = contentType;
        this.mediaFileSize = mediaFileSize;
        this.description = description;
    }

    public static MediaFile of(
            @NonNull String fileName,
            @NonNull String thumbnailFileName,
            @NonNull String contentType,
            @NonNull Long fileSize) {
        return MediaFile.of(fileName, thumbnailFileName, contentType, fileSize, null);
    }

    public static MediaFile of(
            @NonNull String fileName,
            @NonNull String thumbnailFileName,
            @NonNull String contentType,
            @NonNull Long fileSize,
            @Nullable String description) {
        return MediaFile.of(null, fileName, thumbnailFileName, contentType, fileSize, description);
    }

    public static MediaFile of(
            @Nullable Long id,
            @NonNull String fileName,
            @NonNull String thumbnailFileName,
            @NonNull String contentType,
            @NonNull Long fileSize,
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
            MediaFileDescription mediaFileDescription) {
        this.mediaFileName = mediaFileName;
        this.contentType = mediaFileContentType;
        this.mediaFileSize = mediaFileSize;
        this.description = mediaFileDescription;
    }

    @Override
    public int compareTo(@NonNull MediaFile o) {
        int val = ObjectUtils.compare(this.mediaFileName.value(), o.mediaFileName.value());
        if (val == 0) {
            val = ObjectUtils.compare(this.contentType.value(), o.contentType.value());
        }
        if (val == 0) {
            val = ObjectUtils.compare(this.mediaFileSize.value(), o.mediaFileSize.value());
        }
        if (val == 0) {
            val = ObjectUtils.compare(this.description.value(), o.description.value());
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
