package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.EventCertificateStatDto;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificateStat;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import java.util.List;
import java.util.Map;

public class EventCertificateStatMapper {

    public static EventCertificateStatDto toDto(
            EventCertificateStat eventCertificateStat,
            Map<EventId, Event> eventMap,
            Map<PersonId, Person> personMap) {
        return new EventCertificateStatDto(
                eventCertificateStat.getId().value(),
                EventMapper.toKeyDto(eventMap.get(eventCertificateStat.getEvent())),
                PersonKeyMapper.toDto(personMap.get(eventCertificateStat.getPerson())),
                eventCertificateStat.getGenerated());
    }

    public static List<EventCertificateStatDto> toDtos(
            List<EventCertificateStat> eventCertificateStats,
            Map<EventId, Event> eventMap,
            Map<PersonId, Person> personMap) {
        return eventCertificateStats.stream()
                .map(stat -> toDto(stat, eventMap, personMap))
                .toList();
    }
}
