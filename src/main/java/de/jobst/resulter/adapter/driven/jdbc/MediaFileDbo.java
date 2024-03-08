package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.MediaFile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
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

    public static MediaFileDbo from(MediaFile mediaFile, @NonNull DboResolvers dboResolvers) {
        if (null == mediaFile) {
            return null;
        }
        MediaFileDbo mediaFileDbo;
        if (mediaFile.getId().isPersistent()) {
            mediaFileDbo = dboResolvers.getMediaFileDboResolver().findDboById(mediaFile.getId());
            mediaFileDbo.setFileName(mediaFile.getMediaFileName().value());
            mediaFileDbo.setThumbnailFileName(mediaFile.getThumbnailFileName().value());
            mediaFileDbo.setContentType(mediaFile.getContentType().value());
            mediaFileDbo.setFileSize(mediaFile.getMediaFileSize().value());
        } else {
            mediaFileDbo = new MediaFileDbo(mediaFile.getMediaFileName().value(),
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

    public MediaFile asMediaFile() {
        return MediaFile.of(id, fileName, thumbnailFileName, contentType, fileSize, description);
    }

}
