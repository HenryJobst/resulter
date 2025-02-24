package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Collection;

@ValueObject
public record ClassResults(Collection<ClassResult> value) {
    public static ClassResults of(Collection<ClassResult> classResults) {
        return new ClassResults(classResults);
    }
}
