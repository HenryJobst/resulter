package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.MediaFile;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ =@PersistenceCreator)
@Table(name = "media_file")
public class MediaFileDbo {

    @Id
    @With
    @Column("id")
    private Long id;

    @Column("file_name")
    private String fileName;

    @Column("thumbnail_file_name")
    private String thumbnailFileName;

    @Column("content_type")
    private String contentType;

    @Column("file_size")
    private Long fileSize;

    @Column("description")
    private String description;

    public MediaFileDbo(String fileName, String thumbnailFileName, String contentType, Long fileSize) {
        this.fileName = fileName;
        this.thumbnailFileName = thumbnailFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public static @Nullable MediaFileDbo from(@Nullable MediaFile mediaFile, DboResolvers dboResolvers) {
        if (null == mediaFile) {
            return null;
        }
        MediaFileDbo mediaFileDbo;
        if (mediaFile.getId().isPersistent() && dboResolvers.getMediaFileDboResolver() != null) {
            mediaFileDbo = dboResolvers.getMediaFileDboResolver().findDboById(mediaFile.getId());
            mediaFileDbo.setFileName(mediaFile.getMediaFileName().value());
            mediaFileDbo.setThumbnailFileName(mediaFile.getThumbnailFileName().value());
            mediaFileDbo.setContentType(mediaFile.getContentType().value());
            mediaFileDbo.setFileSize(mediaFile.getMediaFileSize().value());
        } else {
            mediaFileDbo = new MediaFileDbo(
                    mediaFile.getMediaFileName().value(),
                    mediaFile.getThumbnailFileName().value(),
                    mediaFile.getContentType().value(),
                    mediaFile.getMediaFileSize().value());
        }

        if (mediaFile.getDescription() != null) {
            mediaFileDbo.setDescription(mediaFile.getDescription().value());
        } else {
            mediaFileDbo.setDescription(null);
        }

        return mediaFileDbo;
    }

    public static MediaFile asMediaFile(MediaFileDbo mediaFile) {
        return mediaFile.asMediaFile();
    }

    public MediaFile asMediaFile() {
        return MediaFile.of(id, fileName, thumbnailFileName, contentType, fileSize, description);
    }

    public static String mapOrdersDomainToDbo(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            case "fileName.value" -> "fileName";
            case "description.value" -> "description";
            case "contentType.value" -> "contentType";
            default -> order.getProperty();
        };
    }

    public static String mapOrdersDboToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            case "fileName" -> "fileName.value";
            case "description" -> "description.value";
            case "contentType" -> "contentType.value";
            default -> order.getProperty();
        };
    }
}
