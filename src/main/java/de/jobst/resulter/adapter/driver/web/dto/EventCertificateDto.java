package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.EventCertificate;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

public record EventCertificateDto(Long id, String name, EventKeyDto event, String layoutDescription,
                                  MediaFileKeyDto blankCertificate) {

    static public EventCertificateDto from(EventCertificate eventCertificate) {
        return new EventCertificateDto(ObjectUtils.isNotEmpty(eventCertificate.getId()) ?
                                       eventCertificate.getId().value() :
                                       0,
            eventCertificate.getName().value(),
            EventKeyDto.from(eventCertificate.getEvent()),
            ObjectUtils.isNotEmpty(eventCertificate.getLayoutDescription()) ?
            eventCertificate.getLayoutDescription().value() :
            null,
            ObjectUtils.isNotEmpty(eventCertificate.getBlankCertificate()) ?
            MediaFileKeyDto.from(eventCertificate.getBlankCertificate()) :
            null);
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
