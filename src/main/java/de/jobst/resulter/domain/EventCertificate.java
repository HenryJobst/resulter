package de.jobst.resulter.domain;

import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class EventCertificate {

    @NonNull
    @Setter
    private EventCertificateId id;

    @Nullable
    private String textParagraphsAsJson;

    @Nullable
    private BlankCertificate blankCertificate;

    public EventCertificate(@NonNull EventCertificateId id) {
        this.id = id;
    }
}
