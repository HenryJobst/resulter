package de.jobst.resulter.adapter.driver.web.mapper;

import static de.jobst.resulter.domain.util.ConverterUtils.encodeFileToBase64Binary;

import de.jobst.resulter.adapter.driver.web.dto.MediaFileKeyDto;
import de.jobst.resulter.domain.MediaFile;
import java.nio.file.Path;
import org.apache.commons.lang3.ObjectUtils;

public class MediaFileKeyMapper {

    private MediaFileKeyMapper() {}

    public static MediaFileKeyDto toDto(MediaFile mediaFile, String thumbnailPath) {
        return new MediaFileKeyDto(
                ObjectUtils.isNotEmpty(mediaFile.getId()) ? mediaFile.getId().value() : 0,
                mediaFile.getMediaFileName().value(),
                encodeFileToBase64Binary(
                        Path.of(thumbnailPath + mediaFile.getThumbnailFileName().value())));
    }
}
