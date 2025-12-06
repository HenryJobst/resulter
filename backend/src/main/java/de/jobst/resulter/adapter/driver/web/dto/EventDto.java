package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.domain.Event;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;

public record EventDto(
        Long id,
        String name,
        String startTime,
        EventStatusDto state,
        List<OrganisationKeyDto> organisations,
        EventCertificateKeyDto certificate,
        Boolean hasSplitTimes) implements Comparable<EventDto> {

    public static EventDto from(
            Event event,
            OrganisationService organisationService,
            EventCertificateService eventCertificateService,
            Boolean hasSplitTimes) {
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
                        ? EventCertificateKeyDto.from(eventCertificateService.getById(event.getCertificate()))
                        : null,
                hasSplitTimes);
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

    @Override
    public int compareTo(@NonNull EventDto o) {
        int val = (Objects.nonNull(this.startTime) && Objects.nonNull(o.startTime)
                   ? this.startTime.compareTo(o.startTime)
                   : (Objects.equals(this.startTime, o.startTime) ? 0 : (Objects.nonNull(this.startTime) ? -1 : 1)));
        if (val == 0) {
            val = this.name.compareTo(o.name);
        }
        if (val == 0) {
            val = this.id().compareTo(o.id());
        }
        return val;
    }
}
