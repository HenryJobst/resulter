package de.jobst.resulter.application;

import de.jobst.resulter.application.port.MediaFileRepository;
import de.jobst.resulter.domain.*;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class MediaFileService {

    @Value("#{'${resulter.media-file-path}'}")
    private String mediaFilePath;
    @Value("#{'${resulter.media-file-path-thumbnails}'}")
    private String mediaFileThumbnailsPath;
    @Value("#{'${resulter.media-file-thumbnails-size}'}")
    private int mediaFileThumbnailSize;

    private final MediaFileRepository mediaFileRepository;

    public MediaFileService(MediaFileRepository mediaFileRepository) {
        this.mediaFileRepository = mediaFileRepository;
    }

    public MediaFile storeMediaFile(MultipartFile file) throws IOException, MimeTypeException {
        FilePathAndName filePathAndName = getFilePathAndName(file, mediaFilePath);

        File originalFile = new File(filePathAndName.filePath());
        file.transferTo(originalFile);

        File thumbnailDir = new File(mediaFileThumbnailsPath);

        File thumbnailFile = Thumbnails.of(originalFile)
            .size(mediaFileThumbnailSize, mediaFileThumbnailSize)
            .outputFormat("jpg")
            .asFiles(thumbnailDir, Rename.SUFFIX_DOT_THUMBNAIL)
            .getFirst();

        MediaFile mediaFile =
            MediaFile.of(filePathAndName.fileName(), thumbnailFile.getName(), getContentType(file), file.getSize());

        return mediaFileRepository.save(mediaFile);
    }

    @NonNull
    private FilePathAndName getFilePathAndName(MultipartFile file, String mediaFilePath)
        throws IOException, MimeTypeException {
        String fileName = StringUtils.cleanPath(getFilename(file));
        String filePath = Optional.ofNullable(mediaFilePath).orElseThrow() + fileName;
        return new FilePathAndName(fileName, filePath);
    }

    private record FilePathAndName(String fileName, String filePath) {}

    private String getFilename(MultipartFile file) throws IOException, MimeTypeException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            // Generieren eines eindeutigen Dateinamens
            String generatedFilename = UUID.randomUUID().toString();
            // Optional: Hinzufügen einer Dateierweiterung basierend auf dem ContentType
            String extension = getExtensionByMimeType(getContentType(file));
            generatedFilename += extension != null ? "." + extension : "";
            return generatedFilename;
        }
        return originalFilename;
    }

    private String getContentType(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType != null) {
            return contentType;
        }

        Tika tika = new Tika();
        return tika.detect(file.getInputStream());
    }

    private String getExtensionByMimeType(String mimeType) throws MimeTypeException {
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        MimeType type = allTypes.forName(mimeType);
        return type.getExtension();
    }


    public boolean delete(MediaFileId mediaFileId) {
        if (mediaFileId.isPersistent()) {
            mediaFileRepository.delete(mediaFileId);
            return true;
        }
        return false;
    }

    public Page<MediaFile> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        return mediaFileRepository.findAll(filter, pageable);
    }

    public Optional<MediaFile> findById(MediaFileId mediaFileId) {
        return mediaFileRepository.findById(mediaFileId);
    }

    public MediaFile update(MediaFileId mediaFileId,
                            MediaFileName mediaFileName,
                            MediaFileContentType mediaFileContentType,
                            MediaFileSize mediaFileSize,
                            MediaFileDescription mediaFileDescription) {
        Optional<MediaFile> optionalMediaFile = findById(mediaFileId);
        if (optionalMediaFile.isEmpty()) {
            return null;
        }
        MediaFile mediaFile = optionalMediaFile.get();
        mediaFile.update(mediaFileName, mediaFileContentType, mediaFileSize, mediaFileDescription);
        return mediaFileRepository.save(mediaFile);
    }

}
