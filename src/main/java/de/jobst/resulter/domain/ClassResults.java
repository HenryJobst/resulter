package de.jobst.resulter.domain;

import java.util.Collection;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record ClassResults(Collection<ClassResult> value) {
    public static ClassResults of(Collection<ClassResult> classResults) {
        return new ClassResults(classResults);
    }
}
