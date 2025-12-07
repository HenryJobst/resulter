package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Person;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.function.UnaryOperator;

public record PersonDto(Long id, String familyName, String givenName, GenderDto gender, LocalDate birthDate) {

    static public PersonDto from(Person person) {
        return new PersonDto(ObjectUtils.isNotEmpty(person.id()) ? person.id().value() : 0,
            person.personName().familyName().value(),
            person.personName().givenName().value(),
            GenderDto.from(person.gender()),
            person.birthDate() != null ? person.birthDate().value() : null);
    }

    static UnaryOperator<String> mapOperator = (String s) -> switch (s) {
        case "id" -> "id.value";
        case "familyName" -> "personName.familyName.value";
        case "givenName" -> "personName.givenName.value";
        case "gender" -> "gender.id";
        case "birthDate" -> "birthDate.value";
        default -> s;
    };

    @NonNull
    public static String mapOrdersDtoToDomain(Sort.Order order) {
        return mapOperator.apply(order.getProperty());
    }

    @NonNull
    public static String mapOrdersDomainToDto(Sort.Order order) {
        return mapOperator.apply(order.getProperty());
    }
}
