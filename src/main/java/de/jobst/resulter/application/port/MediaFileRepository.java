package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.MediaFile;
import de.jobst.resulter.domain.MediaFileId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MediaFileRepository {

    MediaFile save(MediaFile mediaFile);

    void delete(MediaFileId mediaFileId);

    Page<MediaFile> findAll(String filter, Pageable pageable);

    Optional<MediaFile> findById(MediaFileId mediaFileId);
}
