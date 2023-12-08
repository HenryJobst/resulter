package de.jobst.resulter.domain;

import java.util.Collection;

public record Organisations(Collection<Organisation> organisations) {
    public static Organisations of(Collection<Organisation> organisations) {
        return new Organisations(organisations);
    }
}
