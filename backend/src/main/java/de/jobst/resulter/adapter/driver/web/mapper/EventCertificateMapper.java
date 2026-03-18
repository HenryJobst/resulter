package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.EventCertificateDto;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.MediaFile;
import de.jobst.resulter.domain.MediaFileId;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;

public class EventCertificateMapper {

    public static EventCertificateDto toDto(
            EventCertificate eventCertificate,
            Map<EventId, Event> eventMap,
            Map<MediaFileId, MediaFile> mediaFileMap,
            String thumbnailPath) {
        Event event = ObjectUtils.isNotEmpty(eventCertificate.getEvent())
                ? eventMap.get(eventCertificate.getEvent())
                : null;
        MediaFile mediaFile = ObjectUtils.isNotEmpty(eventCertificate.getBlankCertificate())
                ? mediaFileMap.get(eventCertificate.getBlankCertificate())
                : null;
        return new EventCertificateDto(
                ObjectUtils.isNotEmpty(eventCertificate.getId()) ? eventCertificate.getId().value() : 0,
                eventCertificate.getName().value(),
                event != null ? EventMapper.toKeyDto(event) : null,
                ObjectUtils.isNotEmpty(eventCertificate.getLayoutDescription())
                        ? eventCertificate.getLayoutDescription().value()
                        : null,
                mediaFile != null ? MediaFileKeyMapper.toDto(mediaFile, thumbnailPath) : null,
                eventCertificate.isPrimary());
    }

    public static List<EventCertificateDto> toDtos(
            List<EventCertificate> certificates,
            Map<EventId, Event> eventMap,
            Map<MediaFileId, MediaFile> mediaFileMap,
            String thumbnailPath) {
        return certificates.stream()
                .map(c -> toDto(c, eventMap, mediaFileMap, thumbnailPath))
                .toList();
    }
}
