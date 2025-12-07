package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Person;
import org.apache.commons.lang3.ObjectUtils;

public record PersonKeyDto(Long id, String familyName, String givenName) {

    static public PersonKeyDto from(Person person) {
        return new PersonKeyDto(ObjectUtils.isNotEmpty(person.id()) ? person.id().value() : 0,
            person.personName().familyName().value(),
            person.personName().givenName().value());
    }

}
