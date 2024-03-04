package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.util.Objects;

public record BlankCertificateId(Long value) implements Comparable<BlankCertificateId> {

    public static BlankCertificateId of(Long value) {
        if (value != null && value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0 or null.");
        }
        return new BlankCertificateId(value);
    }

    public static BlankCertificateId empty() {
        return new BlankCertificateId(0L);
    }

    @Override
    public int compareTo(@NonNull BlankCertificateId o) {
        return value.compareTo(o.value);
    }

    public boolean isPersistent() {
        return !Objects.equals(value, empty().value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
