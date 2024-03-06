package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.MediaFile;
import de.jobst.resulter.domain.MediaFileId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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
    public void delete(MediaFile mediaFile) {
        if (ObjectUtils.isEmpty(mediaFile.getId()) || mediaFile.getId().value() == 0) {
            return;
        }
        mediaFiles.remove(mediaFile.getId());
        savedMediaFiles.remove(mediaFile);
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
