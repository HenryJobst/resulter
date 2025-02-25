package de.jobst.resulter.domain;

import com.fasterxml.uuid.Generators;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Entity
@Getter
@Setter
public class EventCertificate {

    @Identity
    @NonNull
    private EventCertificateId id;

    @NonNull
    private EventCertificateName name;

    @Nullable
    private Event event;

    @Nullable
    private EventCertificateLayoutDescription layoutDescription;

    @Nullable
    private MediaFile blankCertificate;

    private boolean primary;

    public EventCertificate(
            @NonNull EventCertificateId id,
            @NonNull EventCertificateName name,
            @Nullable Event event,
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

    public static EventCertificate of(
            Long id,
            String name,
            Event event,
            String eventCertificateLayoutDescription,
            MediaFile blankCertificate,
            boolean primary) {
        return new EventCertificate(
                EventCertificateId.of(id),
                EventCertificateName.of(
                        StringUtils.isEmpty(name)
                                ? Generators.timeBasedEpochGenerator()
                                        .generate()
                                        .toString()
                                : name),
                event,
                EventCertificateLayoutDescription.of(
                        StringUtils.isEmpty(eventCertificateLayoutDescription)
                                ? "{\"paragraphs\" : []}"
                                : eventCertificateLayoutDescription),
                blankCertificate,
                primary);
    }

    public void update(
            EventCertificateName name,
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
