package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.MediaFileRepository;
import de.jobst.resulter.domain.MediaFile;
import de.jobst.resulter.domain.MediaFileId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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
    public void delete(MediaFileId mediaFileId) {
        mediaFileJdbcRepository.deleteById(mediaFileId.value());
    }

    @Override
    public Page<MediaFile> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        return mediaFileJdbcRepository.findAll(pageable).map(MediaFileDbo::asMediaFile);
    }

    @Override
    public Optional<MediaFile> findById(MediaFileId mediaFileId) {
        return mediaFileJdbcRepository.findById(mediaFileId.value()).map(MediaFileDbo::asMediaFile);
    }
}
