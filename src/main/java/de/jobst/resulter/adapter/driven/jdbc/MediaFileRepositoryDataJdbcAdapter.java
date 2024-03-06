package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.MediaFileRepository;
import de.jobst.resulter.domain.MediaFile;
import de.jobst.resulter.domain.MediaFileId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class MediaFileRepositoryDataJdbcAdapter implements MediaFileRepository {

    private final MediaFileJdbcRepository mediaFileJdbcRepository;

    public MediaFileRepositoryDataJdbcAdapter(MediaFileJdbcRepository mediaFileJdbcRepository) {
        this.mediaFileJdbcRepository = mediaFileJdbcRepository;
    }

    @Transactional
    public DboResolver<MediaFileId, MediaFileDbo> getIdResolver() {
        return (MediaFileId id) -> findDboById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Optional<MediaFileDbo> findDboById(MediaFileId mediaFileId) {
        return mediaFileJdbcRepository.findById(mediaFileId.value());
    }

    @Override
    @Transactional
    public MediaFile save(MediaFile mediaFile) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setMediaFileDboResolver(getIdResolver());
        MediaFileDbo mediaFileDbo = MediaFileDbo.from(mediaFile, dboResolvers);
        MediaFileDbo savedMediaFileEntity = mediaFileJdbcRepository.save(mediaFileDbo);
        return savedMediaFileEntity.asMediaFile();
    }

    @Override
    public void delete(MediaFile mediaFile) {
        mediaFileJdbcRepository.deleteById(mediaFile.getId().value());
    }
}
