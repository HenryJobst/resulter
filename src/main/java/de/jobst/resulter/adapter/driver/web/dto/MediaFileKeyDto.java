package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.MediaFile;
import org.apache.commons.lang3.ObjectUtils;

import java.nio.file.Path;

import static de.jobst.resulter.domain.util.ConverterUtils.encodeFileToBase64Binary;

public record MediaFileKeyDto(Long id, String fileName, String thumbnailContent) {

    static public MediaFileKeyDto from(MediaFile mediaFile, String thumbnailPath) {
        return new MediaFileKeyDto(ObjectUtils.isNotEmpty(mediaFile.getId()) ? mediaFile.getId().value() : 0,
            mediaFile.getMediaFileName().value(),
            encodeFileToBase64Binary(Path.of(thumbnailPath + mediaFile.getThumbnailFileName().value())));
    }
}
