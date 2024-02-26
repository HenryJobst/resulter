package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Person;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

public record PersonDto(Long id, String familyName, String givenName, GenderDto gender, java.time.LocalDate birthDate) {

    static public PersonDto from(Person person) {
        return new PersonDto(ObjectUtils.isNotEmpty(person.getId()) ? person.getId().value() : 0,
            person.getPersonName().familyName().value(),
            person.getPersonName().givenName().value(),
            GenderDto.from(person.getGender()),
            person.getBirthDate().value());
    }

    @NonNull
    public static String mapOrdersDtoToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            case "familyName" -> "personName.familyName.value";
            case "givenName" -> "personName.givenName.value";
            case "gender" -> "gender.id";
            case "birthDate" -> "birthDate.value";
            default -> order.getProperty();
        };
    }

    @NonNull
    public static String mapOrdersDomainToDto(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            case "personName.familyName.value" -> "familyName";
            case "personName.givenName.value" -> "givenName";
            case "gender.id" -> "gender";
            case "birthDate.value" -> "birthDate";
            default -> order.getProperty();
        };
    }
}
