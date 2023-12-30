package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Person;
import org.apache.commons.lang3.ObjectUtils;

public record PersonDto(Long id,
                        String name,
                        String gender,
                        java.time.LocalDate birthDate) {
    static public PersonDto from(Person person) {
        return new PersonDto(
                ObjectUtils.isNotEmpty(person.getId()) ?
                        person.getId().value() : 0,
                person.getPersonName().getFullName(),
                person.getGender().name(),
                person.getBirthDate().value()
        );
    }
}
