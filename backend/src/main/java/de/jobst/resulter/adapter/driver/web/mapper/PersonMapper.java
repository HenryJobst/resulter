package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.GenderDto;
import de.jobst.resulter.adapter.driver.web.dto.PersonDto;
import de.jobst.resulter.domain.Person;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.ObjectUtils;

public class PersonMapper {

    private PersonMapper() {
        // Utility class
    }

    public static PersonDto toDto(Person person) {
        return toDto(person, false);
    }

    public static PersonDto toDto(Person person, boolean showMergeButton) {
        return new PersonDto(
                ObjectUtils.isNotEmpty(person.id()) ? person.id().value() : 0,
                person.personName().familyName().value(),
                person.personName().givenName().value(),
                GenderDto.from(person.gender()),
                person.birthDate() != null ? person.birthDate().value() : null,
                showMergeButton);
    }

    public static List<PersonDto> toDtos(List<Person> persons) {
        return persons.stream().map(PersonMapper::toDto).toList();
    }

    public static List<PersonDto> toDtos(List<Person> persons, boolean showMergeButton) {
        return persons.stream().map(p -> toDto(p, showMergeButton)).toList();
    }

    public static List<PersonDto> toDtos(List<Person> persons, Set<Long> groupLeaderIds) {
        return persons.stream()
                .map(p -> toDto(p, groupLeaderIds.contains(p.id().value())))
                .toList();
    }
}
