package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.MediaFile;
import de.jobst.resulter.domain.MediaFileId;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

public record EventCertificateBatchResult(
        List<EventCertificate> eventCertificates,
        long totalElements,
        Pageable resolvedPageable,
        Map<EventId, Event> eventMap,
        Map<MediaFileId, MediaFile> mediaFileMap) {}
