package de.jobst.resulter.domain;

import java.util.Collection;

public record ClassResults(Collection<ClassResult> classResults) {
    public static ClassResults of(Collection<ClassResult> classResults) {
        return new ClassResults(classResults);
    }
}
