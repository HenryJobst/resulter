package de.jobst.resulter.application;

import de.jobst.resulter.application.port.EventCertificateBatchResult;
import de.jobst.resulter.application.port.EventCertificateQueryService;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.EventCertificateStatBatchResult;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.MediaFileService;
import de.jobst.resulter.application.port.PersonService;
import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventCertificateStat;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.MediaFile;
import de.jobst.resulter.domain.MediaFileId;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventCertificateQueryServiceImpl implements EventCertificateQueryService {

    private final EventCertificateService eventCertificateService;
    private final EventService eventService;
    private final MediaFileService mediaFileService;
    private final PersonService personService;
    private final ResultListService resultListService;

    public EventCertificateQueryServiceImpl(
            EventCertificateService eventCertificateService,
            EventService eventService,
            MediaFileService mediaFileService,
            PersonService personService,
            ResultListService resultListService) {
        this.eventCertificateService = eventCertificateService;
        this.eventService = eventService;
        this.mediaFileService = mediaFileService;
        this.personService = personService;
        this.resultListService = resultListService;
    }

    @Override
    public EventCertificateBatchResult findAll() {
        List<EventCertificate> certificates = eventCertificateService.findAll();
        return buildBatchResult(certificates, certificates.size(), Pageable.unpaged());
    }

    @Override
    public EventCertificateBatchResult findAll(String filter, Pageable pageable) {
        Page<EventCertificate> page = eventCertificateService.findAll(filter, pageable);
        return buildBatchResult(page.getContent(), page.getTotalElements(), page.getPageable());
    }

    @Override
    public Optional<EventCertificateBatchResult> findById(Long id) {
        return eventCertificateService
                .findById(EventCertificateId.of(id))
                .map(cert -> buildBatchResult(List.of(cert), 1, Pageable.unpaged()));
    }

    @Override
    public EventCertificateStatBatchResult getCertificateStats(EventId eventId) {
        List<EventCertificateStat> stats = resultListService.getCertificateStats(eventId);
        if (stats.isEmpty()) {
            return new EventCertificateStatBatchResult(stats, Map.of(), Map.of());
        }
        Set<EventId> eventIds =
                stats.stream().map(EventCertificateStat::getEvent).collect(Collectors.toSet());
        Set<PersonId> personIds =
                stats.stream().map(EventCertificateStat::getPerson).collect(Collectors.toSet());
        Map<EventId, Event> eventMap = eventService.findAllByIdAsMap(eventIds);
        Map<PersonId, Person> personMap = personService.findAllByIdAsMap(personIds);
        return new EventCertificateStatBatchResult(stats, eventMap, personMap);
    }

    private EventCertificateBatchResult buildBatchResult(
            List<EventCertificate> certificates, long totalElements, Pageable pageable) {
        if (certificates.isEmpty()) {
            return new EventCertificateBatchResult(certificates, totalElements, pageable, Map.of(), Map.of());
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
        return new EventCertificateBatchResult(certificates, totalElements, pageable, eventMap, mediaFileMap);
    }
}
