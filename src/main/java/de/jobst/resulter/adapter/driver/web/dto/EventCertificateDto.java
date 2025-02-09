package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.adapter.driver.web.constraints.FullDtoGroup;
import de.jobst.resulter.adapter.driver.web.constraints.KeyDtoGroup;
import de.jobst.resulter.adapter.driver.web.constraints.ValidId;
import de.jobst.resulter.domain.EventCertificate;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

public record EventCertificateDto(@ValidId Long id,
                                  String name,
                                  @Validated({KeyDtoGroup.class, FullDtoGroup.class})
                                  EventKeyDto event,
                                  String layoutDescription,
                                  @Validated({KeyDtoGroup.class, FullDtoGroup.class})
                                  MediaFileKeyDto blankCertificate,
                                  @NotNull
                                  boolean primary) {

    static public EventCertificateDto from(EventCertificate eventCertificate, String thumbnailPath) {
        return new EventCertificateDto(ObjectUtils.isNotEmpty(eventCertificate.getId()) ?
                                       eventCertificate.getId().value() :
                                       0,
            eventCertificate.getName().value(),
            ObjectUtils.isNotEmpty(eventCertificate.getEvent()) ?
            EventKeyDto.from(eventCertificate.getEvent()) : null,
            ObjectUtils.isNotEmpty(eventCertificate.getLayoutDescription()) ?
            eventCertificate.getLayoutDescription().value() :
            null,
            ObjectUtils.isNotEmpty(eventCertificate.getBlankCertificate()) ?
            MediaFileKeyDto.from(eventCertificate.getBlankCertificate(), thumbnailPath) :
            null,
            eventCertificate.isPrimary());
    }

    @NonNull
    public static String mapOrdersDtoToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            case "name" -> "event.name.value";
            default -> order.getProperty();
        };
    }

    @NonNull
    public static String mapOrdersDomainToDto(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            case "event.name.value" -> "name";
            default -> order.getProperty();
        };
    }
}
