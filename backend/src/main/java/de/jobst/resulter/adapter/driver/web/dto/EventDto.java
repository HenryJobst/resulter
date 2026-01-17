package de.jobst.resulter.adapter.driver.web.dto;

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
    Boolean hasSplitTimes,
    DisciplineDto discipline,
    Boolean aggregateScore) implements Comparable<EventDto> {

    public static String mapOrdersDtoToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            case "name" -> "event.name.value";
            case "startTime" -> "startTime.value";
            case "state" -> "state.id";
            case "organisations" -> "organisations";
            case "discipline" -> "discipline.id";
            default -> order.getProperty();
        };
    }

    public static String mapOrdersDomainToDto(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            case "event.name.value" -> "name";
            case "startTime.value" -> "startTime";
            case "state.id" -> "state";
            case "organisations" -> "organisations";
            case "discipline.id" -> "discipline";
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
