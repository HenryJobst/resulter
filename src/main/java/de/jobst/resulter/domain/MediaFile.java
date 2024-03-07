package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public class MediaFile {

    @NonNull
    @Setter
    private MediaFileId id;
    @NonNull
    private MediaFileName mediaFileName;
    @NonNull
    private MediaFileContentType contentType;
    @NonNull
    private MediaFileSize mediaFileSize;
    @Nullable
    @Setter
    private MediaFileDescription description;

    public MediaFile(@NonNull MediaFileId id,
                     @NonNull MediaFileName mediaFileName,
                     @NonNull MediaFileContentType contentType,
                     @NonNull MediaFileSize mediaFileSize,
                     @Nullable MediaFileDescription description) {
        this.id = id;
        this.mediaFileName = mediaFileName;
        this.contentType = contentType;
        this.mediaFileSize = mediaFileSize;
        this.description = description;
    }

    public static MediaFile of(@NonNull String fileName, @NonNull String contentType, @NonNull Long fileSize) {
        return MediaFile.of(fileName, contentType, fileSize, null);
    }

    public static MediaFile of(@NonNull String fileName,
                               @NonNull String contentType,
                               @NonNull Long fileSize,
                               @Nullable String description) {
        return MediaFile.of(null, fileName, contentType, fileSize, description);
    }

    public static MediaFile of(@Nullable Long id,
                               @NonNull String fileName,
                               @NonNull String contentType,
                               @NonNull Long fileSize,
                               @Nullable String description) {
        return new MediaFile(id == null ? MediaFileId.empty() : MediaFileId.of(id),
            MediaFileName.of(fileName),
            MediaFileContentType.of(contentType),
            MediaFileSize.of(fileSize),
            description == null ? null : MediaFileDescription.of(description));
    }

    public void update(MediaFileName mediaFileName,
                       MediaFileContentType mediaFileContentType,
                       MediaFileSize mediaFileSize,
                       MediaFileDescription mediaFileDescription) {
        this.mediaFileName = mediaFileName;
        this.contentType = mediaFileContentType;
        this.mediaFileSize = mediaFileSize;
        this.description = mediaFileDescription;
    }
}

