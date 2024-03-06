package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
@Setter
public class EventCertificate {

    @NonNull
    private EventCertificateId id;

    @Nullable
    private String textParagraphsAsJson;

    @Nullable
    private MediaFile blankCertificate;

    public EventCertificate(@NonNull EventCertificateId id,
                            @Nullable String textParagraphsAsJson,
                            @Nullable MediaFile blankCertificate) {
        this.id = id;
        this.textParagraphsAsJson = textParagraphsAsJson;
        this.blankCertificate = blankCertificate;
    }
}
