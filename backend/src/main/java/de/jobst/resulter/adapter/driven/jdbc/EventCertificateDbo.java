package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.MediaFileId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "event_certificate")
public class EventCertificateDbo {

    @Id
    @With
    @Column("id")
    @Nullable
    private Long id;

    @Column("event_id")
    @Nullable
    private AggregateReference<EventDbo, Long> event;

    @Column("name")
    private String name;

    @Column("background_media_file_id")
    @Nullable
    private AggregateReference<MediaFileDbo, Long> blankCertificate;

    @Column("layout_description")
    @Nullable
    private String layoutDescription;

    @Column("primary")
    private @Nullable Boolean primary;

    public EventCertificateDbo(String name, @Nullable AggregateReference<EventDbo, Long> event) {
        this.id = null;
        this.name = name;
        this.event = event;
    }

    public static EventCertificateDbo from(
            EventCertificate eventCertificate, DboResolvers dboResolvers) {
        EventCertificateDbo eventCertificateDbo;
        if (eventCertificate.getId().isPersistent()) {
            eventCertificateDbo =
                Optional.ofNullable(dboResolvers.getEventCertificateDboResolver())
                    .map(x -> x.findDboById(eventCertificate.getId())).orElseThrow();
            eventCertificateDbo.setName(eventCertificate.getName().value());
            if (null != eventCertificate.getEvent()) {
                eventCertificateDbo.setEvent(
                        AggregateReference.to(eventCertificate.getEvent().value()));
            }
        } else {
            eventCertificateDbo = new EventCertificateDbo(
                    eventCertificate.getName().value(),
                    eventCertificate.getEvent() != null
                            ? AggregateReference.to(eventCertificate.getEvent().value())
                            : null);
        }

        if (ObjectUtils.isNotEmpty(eventCertificate.getBlankCertificate())) {
            eventCertificateDbo.setBlankCertificate(
                    AggregateReference.to(eventCertificate.getBlankCertificate().value()));
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
            Collection<EventCertificateDbo> eventCertificateDbos) {

        return eventCertificateDbos.stream()
                .map(it -> EventCertificate.of(
                        it.id,
                        it.name,
                        it.event != null ? EventId.of(it.event.getId()) : null,
                        it.layoutDescription != null ? it.layoutDescription : null,
                        it.blankCertificate != null ? MediaFileId.of(it.blankCertificate.getId()) : null,
                        it.primary != null ? it.primary : false))
                .toList();
    }

    public static EventCertificate asEventCertificate(
            EventCertificateDbo eventCertificateDbo) {
        return asEventCertificates(List.of(eventCertificateDbo))
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
