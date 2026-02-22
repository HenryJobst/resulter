package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.EventCertificateStatDto;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.PersonService;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificateStat;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class EventCertificateStatMapper {

    private final EventService eventService;
    private final PersonService personService;

    public EventCertificateStatMapper(EventService eventService, PersonService personService) {
        this.eventService = eventService;
        this.personService = personService;
    }

    public EventCertificateStatDto toDto(EventCertificateStat eventCertificateStat) {
        return new EventCertificateStatDto(
                eventCertificateStat.getId().value(),
                EventMapper.toKeyDto(eventService.getById(eventCertificateStat.getEvent())),
                PersonKeyMapper.toDto(personService.getById(eventCertificateStat.getPerson())),
                eventCertificateStat.getGenerated());
    }

    public List<EventCertificateStatDto> toDtos(List<EventCertificateStat> eventCertificateStats) {
        if (eventCertificateStats.isEmpty()) {
            return List.of();
        }

        Set<EventId> eventIds = eventCertificateStats.stream()
                .map(EventCertificateStat::getEvent)
                .collect(java.util.stream.Collectors.toSet());
        Set<PersonId> personIds = eventCertificateStats.stream()
                .map(EventCertificateStat::getPerson)
                .collect(java.util.stream.Collectors.toSet());

        Map<EventId, Event> eventsById = eventService.findAllByIdAsMap(eventIds);
        Map<PersonId, Person> personsById = personService.findAllByIdAsMap(personIds);

        return eventCertificateStats.stream()
                .map(eventCertificateStat -> new EventCertificateStatDto(
                        eventCertificateStat.getId().value(),
                        EventMapper.toKeyDto(eventsById.get(eventCertificateStat.getEvent())),
                        PersonKeyMapper.toDto(personsById.get(eventCertificateStat.getPerson())),
                        eventCertificateStat.getGenerated()))
                .toList();
    }
}
