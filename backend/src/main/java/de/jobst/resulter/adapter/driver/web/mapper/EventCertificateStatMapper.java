package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.EventCertificateStatDto;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.PersonService;
import de.jobst.resulter.domain.EventCertificateStat;
import java.util.List;
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
        return eventCertificateStats.stream().map(this::toDto).toList();
    }
}
