package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.EventCertificateDto;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.MediaFileService;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.MediaFile;
import de.jobst.resulter.domain.MediaFileId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
public class EventCertificateMapper {

    private final EventService eventService;
    private final MediaFileService mediaFileService;

    public EventCertificateMapper(EventService eventService, MediaFileService mediaFileService) {
        this.eventService = eventService;
        this.mediaFileService = mediaFileService;
    }

    @Deprecated(since = "4.6.2", forRemoval = true)
    public EventCertificateDto toDto(EventCertificate eventCertificate, String thumbnailPath) {
        return new EventCertificateDto(
                ObjectUtils.isNotEmpty(eventCertificate.getId())
                        ? eventCertificate.getId().value()
                        : 0,
                eventCertificate.getName().value(),
                ObjectUtils.isNotEmpty(eventCertificate.getEvent())
                        ? EventMapper.toKeyDto(eventService.getById(eventCertificate.getEvent()))
                        : null,
                ObjectUtils.isNotEmpty(eventCertificate.getLayoutDescription())
                        ? eventCertificate.getLayoutDescription().value()
                        : null,
                ObjectUtils.isNotEmpty(eventCertificate.getBlankCertificate())
                        ? MediaFileKeyMapper.toDto(
                                mediaFileService.getById(eventCertificate.getBlankCertificate()), thumbnailPath)
                        : null,
                eventCertificate.isPrimary());
    }

    public EventCertificateDto toDto(
            EventCertificate eventCertificate,
            Map<EventId, Event> eventMap,
            Map<MediaFileId, MediaFile> mediaFileMap,
            String thumbnailPath) {
        return new EventCertificateDto(
                ObjectUtils.isNotEmpty(eventCertificate.getId())
                        ? eventCertificate.getId().value()
                        : 0,
                eventCertificate.getName().value(),
                ObjectUtils.isNotEmpty(eventCertificate.getEvent())
                        ? EventMapper.toKeyDto(eventMap.get(eventCertificate.getEvent()))
                        : null,
                ObjectUtils.isNotEmpty(eventCertificate.getLayoutDescription())
                        ? eventCertificate.getLayoutDescription().value()
                        : null,
                ObjectUtils.isNotEmpty(eventCertificate.getBlankCertificate())
                        ? MediaFileKeyMapper.toDto(
                                mediaFileMap.get(eventCertificate.getBlankCertificate()), thumbnailPath)
                        : null,
                eventCertificate.isPrimary());
    }

    public List<EventCertificateDto> toDtos(List<EventCertificate> certificates, String thumbnailPath) {
        if (certificates.isEmpty()) {
            return List.of();
        }

        Set<EventId> eventIds = certificates.stream()
                .map(EventCertificate::getEvent)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toSet());

        Set<MediaFileId> mediaFileIds = certificates.stream()
                .map(EventCertificate::getBlankCertificate)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toSet());

        Map<EventId, Event> eventMap = eventService.findAllByIdAsMap(eventIds);
        Map<MediaFileId, MediaFile> mediaFileMap = mediaFileService.findAllByIdAsMap(mediaFileIds);

        return certificates.stream()
                .map(c -> toDto(c, eventMap, mediaFileMap, thumbnailPath))
                .toList();
    }
}
