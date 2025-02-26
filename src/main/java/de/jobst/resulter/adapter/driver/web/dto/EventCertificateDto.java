package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.adapter.driver.web.constraints.FullDtoGroup;
import de.jobst.resulter.adapter.driver.web.constraints.KeyDtoGroup;
import de.jobst.resulter.adapter.driver.web.constraints.ValidId;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.MediaFileService;
import de.jobst.resulter.domain.EventCertificate;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

public record EventCertificateDto(
        @ValidId Long id,
        String name,
        @Validated({KeyDtoGroup.class, FullDtoGroup.class}) EventKeyDto event,
        String layoutDescription,
        @Validated({KeyDtoGroup.class, FullDtoGroup.class}) MediaFileKeyDto blankCertificate,
        @NotNull boolean primary) {

    public static EventCertificateDto from(
            EventCertificate eventCertificate,
            String thumbnailPath,
            EventService eventService,
            MediaFileService mediaFileService) {
        return new EventCertificateDto(
                ObjectUtils.isNotEmpty(eventCertificate.getId())
                        ? eventCertificate.getId().value()
                        : 0,
                eventCertificate.getName().value(),
                ObjectUtils.isNotEmpty(eventCertificate.getEvent())
                        ? EventKeyDto.from(eventService.getById(eventCertificate.getEvent()))
                        : null,
                ObjectUtils.isNotEmpty(eventCertificate.getLayoutDescription())
                        ? eventCertificate.getLayoutDescription().value()
                        : null,
                ObjectUtils.isNotEmpty(eventCertificate.getBlankCertificate())
                        ? MediaFileKeyDto.from(
                                mediaFileService.getById(eventCertificate.getBlankCertificate()), thumbnailPath)
                        : null,
                eventCertificate.isPrimary());
    }

    @NonNull
    public static String mapOrdersDtoToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            case "name" -> "name.value";
            default -> order.getProperty();
        };
    }

    @NonNull
    public static String mapOrdersDomainToDto(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            case "name.value" -> "name";
            default -> order.getProperty();
        };
    }
}
