package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.MediaFile;
import org.apache.commons.lang3.ObjectUtils;

public record MediaFileKeyDto(Long id, String fileName) {

    static public MediaFileKeyDto from(MediaFile mediaFile) {
        return new MediaFileKeyDto(ObjectUtils.isNotEmpty(mediaFile.getId()) ? mediaFile.getId().value() : 0,
            mediaFile.getMediaFileName().value());
    }
}
