package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.MediaFile;
import de.jobst.resulter.domain.MediaFileId;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
@SecondaryPort
public interface MediaFileRepository {

    MediaFile save(MediaFile mediaFile);

    void delete(MediaFileId mediaFileId);

    List<MediaFile> findAll();

    Page<MediaFile> findAll(String filter, Pageable pageable);

    Optional<MediaFile> findById(MediaFileId mediaFileId);
}
