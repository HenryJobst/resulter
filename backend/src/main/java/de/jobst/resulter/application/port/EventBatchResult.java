package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

public record EventBatchResult(
        List<Event> events,
        long totalElements,
        Pageable resolvedPageable,
        Map<EventId, Boolean> hasSplitTimesMap,
        Map<OrganisationId, Organisation> organisationMap,
        Map<EventCertificateId, EventCertificate> certificateMap) {}
