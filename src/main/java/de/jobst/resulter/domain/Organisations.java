package de.jobst.resulter.domain;

import java.util.Collection;

public record Organisations(Collection<Organisation> value) {
    public static Organisations of(Collection<Organisation> organisations) {
        return new Organisations(organisations);
    }
}
