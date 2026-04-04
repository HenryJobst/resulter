package de.jobst.resulter.adapter.driver.web.dto;

import java.time.Instant;

public record EventCertificateStatDto(long id, EventKeyDto event, PersonKeyDto person, Instant generated) {}
