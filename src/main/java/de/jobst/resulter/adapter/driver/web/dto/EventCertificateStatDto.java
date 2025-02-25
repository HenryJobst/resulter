package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.EventService;
import de.jobst.resulter.application.PersonService;
import de.jobst.resulter.domain.EventCertificateStat;

import java.time.Instant;

public record EventCertificateStatDto(long id, EventKeyDto event, PersonKeyDto person, Instant generated) {

    public static EventCertificateStatDto from(EventCertificateStat eventCertificateStat, EventService eventService,
                                               PersonService personService) {
        return new EventCertificateStatDto(eventCertificateStat.getId().value(),
            EventKeyDto.from(eventService.getById(eventCertificateStat.getEvent())),
            PersonKeyDto.from(personService.getById(eventCertificateStat.getPerson())),
            eventCertificateStat.getGenerated());
    }
}
