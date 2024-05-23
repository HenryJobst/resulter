package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.EventCertificateStat;

import java.time.Instant;

public record EventCertificateStatDto(long id, EventKeyDto event, PersonKeyDto person, Instant generated) {

    public static EventCertificateStatDto from(EventCertificateStat eventCertificateStat) {
        return new EventCertificateStatDto(eventCertificateStat.getId().value(),
            EventKeyDto.from(eventCertificateStat.getEvent()),
            PersonKeyDto.from(eventCertificateStat.getPerson()),
            eventCertificateStat.getGenerated());
    }
}
