package de.jobst.resulter.domain;

public record Event(EventId id, EventName name) {
    public static Event of(String name) {
        return new Event(EventId.of(0L), EventName.of(name));
    }
    public static Event of(long id, String name) {
        return new Event(EventId.of(id), EventName.of(name));
    }
}
