package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.EventCertificate;
import org.apache.commons.lang3.ObjectUtils;

public record EventCertificateKeyDto(Long id, String name) {

    static public EventCertificateKeyDto from(EventCertificate eventCertificate) {
        return new EventCertificateKeyDto(ObjectUtils.isNotEmpty(eventCertificate.getId()) ?
                                          eventCertificate.getId().value() :
                                          0, eventCertificate.getName().value());
    }
}
