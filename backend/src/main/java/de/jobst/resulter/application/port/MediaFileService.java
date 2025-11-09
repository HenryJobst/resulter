package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.*;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@PrimaryPort
public interface MediaFileService {

    MediaFile storeMediaFile(MultipartFile file);

    boolean delete(MediaFileId mediaFileId);

    List<MediaFile> findAll();

    Page<MediaFile> findAll(@Nullable String filter, @NonNull Pageable pageable);

    MediaFile getById(MediaFileId mediaFileId);

    Optional<MediaFile> findById(MediaFileId mediaFileId);

    MediaFile update(
            MediaFileId mediaFileId,
            MediaFileName mediaFileName,
            MediaFileContentType mediaFileContentType,
            MediaFileSize mediaFileSize,
            MediaFileDescription mediaFileDescription);
}
