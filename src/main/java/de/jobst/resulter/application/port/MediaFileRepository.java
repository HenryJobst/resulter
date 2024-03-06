package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.MediaFile;

public interface MediaFileRepository {

    MediaFile save(MediaFile mediaFile);

    void delete(MediaFile mediaFile);
}
