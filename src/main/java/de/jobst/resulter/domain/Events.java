package de.jobst.resulter.domain;

import java.util.Collection;

public record Events(Collection<Event> value) {
    public static Events of(Collection<Event> events) {
        return new Events(events);
    }
}
