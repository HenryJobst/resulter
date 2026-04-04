package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificateStat;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import java.util.List;
import java.util.Map;

public record EventCertificateStatBatchResult(
        List<EventCertificateStat> eventCertificateStats,
        Map<EventId, Event> eventMap,
        Map<PersonId, Person> personMap) {}
