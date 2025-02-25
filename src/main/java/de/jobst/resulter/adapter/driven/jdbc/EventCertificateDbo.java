package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.MediaFile;
import de.jobst.resulter.domain.MediaFileId;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "event_certificate")
public class EventCertificateDbo {

    @Id
    @With
    @Column("id")
    private Long id;

    @Column("event_id")
    private AggregateReference<EventDbo, Long> event;

    @Column("name")
    private String name;

    @Column("background_media_file_id")
    private AggregateReference<MediaFileDbo, Long> blankCertificate;

    @Column("layout_description")
    private String layoutDescription;

    @Column("primary")
    private Boolean primary;

    public EventCertificateDbo(String name, AggregateReference<EventDbo, Long> event) {
        this.id = null;
        this.name = name;
        this.event = event;
    }

    public static EventCertificateDbo from(
            @NonNull EventCertificate eventCertificate, @NonNull DboResolvers dboResolvers) {
        EventCertificateDbo eventCertificateDbo;
        if (eventCertificate.getId().isPersistent()) {
            eventCertificateDbo = dboResolvers.getEventCertificateDboResolver().findDboById(eventCertificate.getId());
            eventCertificateDbo.setName(eventCertificate.getName().value());
            if (null != eventCertificate.getEvent()) {
                eventCertificateDbo.setEvent(AggregateReference.to(
                        eventCertificate.getEvent().value()));
            }
        } else {
            eventCertificateDbo = new EventCertificateDbo(
                    eventCertificate.getName().value(),
                    eventCertificate.getEvent() != null
                            ? AggregateReference.to(
                                    eventCertificate.getEvent().value())
                            : null);
        }

        if (ObjectUtils.isNotEmpty(eventCertificate.getBlankCertificate())) {
            eventCertificateDbo.setBlankCertificate(AggregateReference.to(
                    eventCertificate.getBlankCertificate().value()));
        } else {
            eventCertificateDbo.setBlankCertificate(null);
        }

        if (ObjectUtils.isNotEmpty(eventCertificate.getLayoutDescription())) {
            eventCertificateDbo.setLayoutDescription(
                    eventCertificate.getLayoutDescription().value());
        } else {
            eventCertificateDbo.setLayoutDescription(null);
        }

        eventCertificateDbo.setPrimary(eventCertificate.isPrimary());

        return eventCertificateDbo;
    }

    public static List<EventCertificate> asEventCertificates(
            @NonNull Collection<EventCertificateDbo> eventCertificateDbos, Function<Long, MediaFile> mediaFileResolver) {

        return eventCertificateDbos.stream()
                .map(it -> EventCertificate.of(
                        it.id,
                        it.name,
                        it.event != null ? EventId.of(it.event.getId()) : null,
                        it.layoutDescription != null ? it.layoutDescription : null,
                        it.blankCertificate != null ? MediaFileId.of(it.blankCertificate.getId()) : null,
                        it.primary))
                .toList();
    }

    public static EventCertificate asEventCertificate(
            @NonNull EventCertificateDbo eventCertificateDbo, Function<Long, MediaFile> mediaFileResolver) {
        return asEventCertificates(List.of(eventCertificateDbo), mediaFileResolver)
                .getFirst();
    }

    public static String mapOrdersDomainToDbo(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            case "name.value" -> "name";
            case "event.id.value" -> "event.id";
            case "event.name.value" -> "event.name";
            default -> order.getProperty();
        };
    }

    public static String mapOrdersDboToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            case "name" -> "name.value";
            case "event.id" -> "event.id.value";
            case "event.name" -> "event.name.value";
            default -> order.getProperty();
        };
    }
}
