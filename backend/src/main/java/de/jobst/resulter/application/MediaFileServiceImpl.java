package de.jobst.resulter.application;

import de.jobst.resulter.application.port.MediaFileRepository;
import de.jobst.resulter.application.port.MediaFileService;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Value("#{'${resulter.media-file-path}'}")
    private String mediaFilePath;

    @Value("#{'${resulter.media-file-path-thumbnails}'}")
    private String mediaFileThumbnailsPath;

    @Value("#{'${resulter.media-file-thumbnails-size}'}")
    private int mediaFileThumbnailSize;

    private final MediaFileRepository mediaFileRepository;

    public MediaFileServiceImpl(MediaFileRepository mediaFileRepository) {
        this.mediaFileRepository = mediaFileRepository;
    }

    @Override
    public MediaFile storeMediaFile(MultipartFile file) {
        FilePathAndName filePathAndName;
        try {
            filePathAndName = getFilePathAndName(file, mediaFilePath);
        } catch (IOException | MimeTypeException e) {
            throw new RuntimeException(e);
        }

        File originalFile = filePathAndName.filePath().toFile();
        try {
            file.transferTo(originalFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File thumbnailDir = ensureDirectory(mediaFileThumbnailsPath).toFile();

        File thumbnailFile;
        try {
            thumbnailFile = Thumbnails.of(originalFile)
                    .size(mediaFileThumbnailSize, mediaFileThumbnailSize)
                    .outputFormat("jpg")
                    .asFiles(thumbnailDir, Rename.SUFFIX_DOT_THUMBNAIL)
                    .getFirst();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MediaFile mediaFile;
        try {
            mediaFile = MediaFile.of(
                    filePathAndName.fileName(), thumbnailFile.getName(), getContentType(file), file.getSize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mediaFileRepository.save(mediaFile);
    }

    private FilePathAndName getFilePathAndName(MultipartFile file, String mediaFilePath)
            throws IOException, MimeTypeException {
        Path basePath = ensureDirectory(Optional.of(mediaFilePath).orElseThrow());
        String fileName = sanitizeFilename(getFilename(file));
        Path filePath = basePath.resolve(fileName).normalize();

        if (!filePath.startsWith(basePath)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        return new FilePathAndName(fileName, filePath);
    }

    private static String sanitizeFilename(String fileName) {
        String cleaned = StringUtils.cleanPath(fileName);
        String onlyName = Paths.get(cleaned).getFileName().toString();

        if (onlyName.isBlank() || onlyName.contains("..")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        return onlyName;
    }

    private static Path ensureDirectory(String directoryPath) {
        Path directory = Paths.get(directoryPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return directory;
    }

    private record FilePathAndName(String fileName, Path filePath) {}

    private String getFilename(MultipartFile file) throws IOException, MimeTypeException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            // Generieren eines eindeutigen Dateinamens
            String generatedFilename = UUID.randomUUID().toString();
            // Optional: Hinzufügen einer Dateierweiterung basierend auf dem ContentType
            String extension = getExtensionByMimeType(getContentType(file));
            generatedFilename += "." + extension;
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
        String extension = type.getExtension();
        return extension.startsWith(".") ? extension.substring(1) : extension;
    }

    @Override
    public boolean delete(MediaFileId mediaFileId) {
        if (mediaFileId.isPersistent()) {
            mediaFileRepository.delete(mediaFileId);
            return true;
        }
        return false;
    }

    @Override
    public List<MediaFile> findAll() {
        return mediaFileRepository.findAll();
    }

    @Override
    public Page<MediaFile> findAll(@Nullable String filter, Pageable pageable) {
        return mediaFileRepository.findAll(filter, pageable);
    }

    @Override
    public MediaFile getById(MediaFileId mediaFileId) {
        return mediaFileRepository.findById(mediaFileId).orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Optional<MediaFile> findById(MediaFileId mediaFileId) {
        return mediaFileRepository.findById(mediaFileId);
    }

    @Override
    public List<MediaFile> findAllById(Collection<MediaFileId> mediaFileIds) {
        return mediaFileRepository.findAllById(mediaFileIds);
    }

    @Override
    public Map<MediaFileId, MediaFile> findAllByIdAsMap(Set<MediaFileId> mediaFileIds) {
        return mediaFileRepository.findAllById(mediaFileIds).stream()
                .collect(Collectors.toMap(MediaFile::getId, mediaFile -> mediaFile));
    }

    @Override
    public @Nullable MediaFile update(
            MediaFileId mediaFileId,
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
