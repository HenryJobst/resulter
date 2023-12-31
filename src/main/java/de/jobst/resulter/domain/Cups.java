package de.jobst.resulter.domain;

import java.util.Collection;

public record Cups(Collection<Cup> value) {
    public static Cups of(Collection<Cup> cups) {
        return new Cups(cups);
    }
}
