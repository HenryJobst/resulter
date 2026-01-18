package de.jobst.resulter.adapter.driven.inmemory;

import de.jobst.resulter.application.port.MediaFileRepository;
import de.jobst.resulter.domain.MediaFile;
import de.jobst.resulter.domain.MediaFileId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "true")
public class InMemoryMediaFileRepository implements MediaFileRepository {

    private final Map<MediaFileId, MediaFile> mediaFiles = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<MediaFile> savedMediaFiles = new ArrayList<>();

    @Override
    public MediaFile save(MediaFile mediaFile) {
        if (ObjectUtils.isEmpty(mediaFile.getId()) || mediaFile.getId().value() == 0) {
            mediaFile.setId(MediaFileId.of(sequence.incrementAndGet()));
        }
        mediaFiles.put(mediaFile.getId(), mediaFile);
        savedMediaFiles.add(mediaFile);
        return mediaFile;
    }

    @Override
    public void delete(MediaFileId mediaFileId) {
        if (ObjectUtils.isEmpty(mediaFileId) || mediaFileId.value() == 0) {
            return;
        }
        mediaFiles.remove(mediaFileId);
        savedMediaFiles.removeIf(mediaFile -> mediaFile.getId().equals(mediaFileId));
    }

    @Override
    public List<MediaFile> findAll() {
        return mediaFiles.values().stream().toList();
    }

    @Override
    public Page<MediaFile> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(mediaFiles.values()), pageable, mediaFiles.size());
    }

    @Override
    public Optional<MediaFile> findById(MediaFileId mediaFileId) {
        return Optional.ofNullable(mediaFiles.get(mediaFileId));
    }

    @Override
    public List<MediaFile> findAllById(Collection<MediaFileId> mediaFileIds) {
        return mediaFileIds.stream()
                .map(mediaFiles::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    public List<MediaFile> savedMediaFiles() {
        return savedMediaFiles;
    }

    @SuppressWarnings("unused")
    public int saveCount() {
        return savedMediaFiles.size();
    }

    @SuppressWarnings("unused")
    public void resetSaveCount() {
        savedMediaFiles.clear();
    }
}
