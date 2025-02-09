package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.adapter.driver.web.constraints.ValidId;
import de.jobst.resulter.domain.Event;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;

public record EventKeyDto(@ValidId Long id,
                          String name) implements Comparable<EventKeyDto> {

    static public EventKeyDto from(Event event) {
        return new EventKeyDto(ObjectUtils.isNotEmpty(event.getId()) ? event.getId().value() : 0,
            event.getName().value());
    }

    @Override
    public int compareTo(@NonNull EventKeyDto o) {
        int val = this.name().compareTo(o.name());
        if (val == 0) {
            val = this.id().compareTo(o.id());
        }
        return val;
    }
}
