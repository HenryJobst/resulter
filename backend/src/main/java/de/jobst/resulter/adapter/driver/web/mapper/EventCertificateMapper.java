package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.EventCertificateDto;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.MediaFileService;
import de.jobst.resulter.domain.EventCertificate;
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
}
