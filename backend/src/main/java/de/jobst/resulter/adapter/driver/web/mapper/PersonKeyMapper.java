package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.PersonKeyDto;
import de.jobst.resulter.domain.Person;
import org.apache.commons.lang3.ObjectUtils;

public class PersonKeyMapper {

    private PersonKeyMapper() {}

    public static PersonKeyDto toDto(Person person) {
        return new PersonKeyDto(
                ObjectUtils.isNotEmpty(person.id()) ? person.id().value() : 0,
                person.personName().familyName().value(),
                person.personName().givenName().value());
    }
}
