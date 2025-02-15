package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.MediaFile;
import de.jobst.resulter.domain.MediaFileId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MediaFileRepository {

    MediaFile save(MediaFile mediaFile);

    void delete(MediaFileId mediaFileId);

    List<MediaFile> findAll();

    Page<MediaFile> findAll(String filter, Pageable pageable);

    Optional<MediaFile> findById(MediaFileId mediaFileId);
}
