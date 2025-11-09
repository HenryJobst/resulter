package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Person;
import org.apache.commons.lang3.ObjectUtils;

public record PersonKeyDto(Long id, String familyName, String givenName) {

    static public PersonKeyDto from(Person person) {
        return new PersonKeyDto(ObjectUtils.isNotEmpty(person.getId()) ? person.getId().value() : 0,
            person.getPersonName().familyName().value(),
            person.getPersonName().givenName().value());
    }

}
