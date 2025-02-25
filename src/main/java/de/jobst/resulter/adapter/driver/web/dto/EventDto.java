package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.EventCertificateService;
import de.jobst.resulter.application.OrganisationService;
import de.jobst.resulter.domain.Event;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.List;

public record EventDto(
        Long id,
        String name,
        String startTime,
        EventStatusDto state,
        List<OrganisationKeyDto> organisations,
        EventCertificateKeyDto certificate) {

    public static EventDto from(Event event, OrganisationService organisationService, EventCertificateService eventCertificateService) {
        return new EventDto(
                ObjectUtils.isNotEmpty(event.getId()) ? event.getId().value() : 0,
                event.getName().value(),
                ObjectUtils.isNotEmpty(event.getStartTime())
                                && ObjectUtils.isNotEmpty(event.getStartTime().value())
                        ? event.getStartTime().value().toString()
                        : null,
                EventStatusDto.from(event.getEventState()),
                event.getOrganisationIds().stream()
                        .map(x -> {
                            var organisation = organisationService.getById(x);
                            return OrganisationKeyDto.from(organisation);
                        })
                        .toList(),
                ObjectUtils.isNotEmpty(event.getCertificate())
                        ? EventCertificateKeyDto.from(
                            eventCertificateService.getById(
                            event.getCertificate()))
                        : null);
    }

    @NonNull
    public static String mapOrdersDtoToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            case "name" -> "event.name.value";
            case "startTime" -> "startTime.value";
            case "state" -> "state.id";
            case "organisations" -> "organisations";
            default -> order.getProperty();
        };
    }

    @NonNull
    public static String mapOrdersDomainToDto(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            case "event.name.value" -> "name";
            case "startTime.value" -> "startTime";
            case "state.id" -> "state";
            case "organisations" -> "organisations";
            default -> order.getProperty();
        };
    }
}
