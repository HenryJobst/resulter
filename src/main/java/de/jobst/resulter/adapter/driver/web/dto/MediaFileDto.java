package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.MediaFile;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

public record MediaFileDto(Long id, String fileName, String contentType, Long fileSize, String description) {

    static public MediaFileDto from(MediaFile mediaFile) {
        return new MediaFileDto(ObjectUtils.isNotEmpty(mediaFile.getId()) ? mediaFile.getId().value() : 0,
            mediaFile.getMediaFileName().value(),
            mediaFile.getContentType().value(),
            mediaFile.getMediaFileSize().value(),
            ObjectUtils.isNotEmpty(mediaFile.getDescription()) ? mediaFile.getDescription().value() : null);
    }

    @NonNull
    public static String mapOrdersDtoToDomain(Sort.Order order) {
        return order.getProperty();
    }

    @NonNull
    public static String mapOrdersDomainToDto(Sort.Order order) {
        return order.getProperty();
    }
}
