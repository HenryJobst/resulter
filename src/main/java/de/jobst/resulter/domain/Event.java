package de.jobst.resulter.domain;

public record Event(Long id, String name) {
    public static Event of(String name) {
        return new Event(0L, name);
    }
    public static Event of(Long id, String name) {
        return new Event(id, name);
    }
}
