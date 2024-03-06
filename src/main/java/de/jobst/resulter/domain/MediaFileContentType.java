package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import org.springframework.lang.NonNull;

public record MediaFileContentType(String value) implements Comparable<MediaFileContentType> {

    public static MediaFileContentType of(String contentType) {
        ValueObjectChecks.requireNotEmpty(contentType);
        return new MediaFileContentType(contentType);
    }

    @Override
    public int compareTo(@NonNull MediaFileContentType o) {
        return value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
