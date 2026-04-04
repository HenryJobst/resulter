package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.adapter.driver.web.constraints.ValidId;
import java.time.ZonedDateTime;
import java.util.Objects;

public record EventKeyDto(@ValidId Long id, String name, ZonedDateTime startTime) implements Comparable<EventKeyDto> {

    @Override
    public int compareTo(EventKeyDto o) {
        int val = (Objects.nonNull(this.startTime) && Objects.nonNull(o.startTime)
                ? this.startTime.compareTo(o.startTime)
                : (this.startTime == o.startTime ? 0 : (Objects.nonNull(this.startTime) ? -1 : 1)));
        if (val == 0) {
            val = this.name.compareTo(o.name);
        }
        if (val == 0) {
            val = this.id().compareTo(o.id());
        }
        return val;
    }
}
