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

    @NonNull
    private EventCertificateName name;

    @NonNull
    private Event event;

    @Nullable
    private EventCertificateLayoutDescription layoutDescription;

    @Nullable
    private MediaFile blankCertificate;

    private boolean primary;

    public EventCertificate(@NonNull EventCertificateId id,
                            @NonNull EventCertificateName name,
                            @NonNull Event event,
                            @Nullable EventCertificateLayoutDescription layoutDescription,
                            @Nullable MediaFile blankCertificate,
                            boolean primary) {
        this.id = id;
        this.name = name;
        this.event = event;
        this.layoutDescription = layoutDescription;
        this.blankCertificate = blankCertificate;
        this.primary = primary;
    }

    public static EventCertificate of(Long id,
                                      String name,
                                      Event event,
                                      String eventCertificateLayoutDescription,
                                      MediaFile blankCertificate,
                                      boolean primary) {
        return new EventCertificate(EventCertificateId.of(id),
            EventCertificateName.of(name),
            event,
            EventCertificateLayoutDescription.of(eventCertificateLayoutDescription),
            blankCertificate,
            primary);
    }

    public void update(EventCertificateName name,
                       Event event,
                       EventCertificateLayoutDescription eventCertificateLayoutDescription,
                       MediaFile mediaFile,
                       boolean primary) {
        this.name = name;
        this.event = event;
        this.layoutDescription = eventCertificateLayoutDescription;
        this.blankCertificate = mediaFile;
        this.primary = primary;
    }
}
