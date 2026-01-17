package de.jobst.resulter.adapter.driver.web.dto;

import java.time.LocalDate;
import java.util.function.UnaryOperator;
import org.springframework.data.domain.Sort;

public record PersonDto(
        Long id, String familyName, String givenName, GenderDto gender, LocalDate birthDate, Boolean showMergeButton) {

    static UnaryOperator<String> mapOperator = (String s) -> switch (s) {
        case "id" -> "id.value";
        case "familyName" -> "personName.familyName.value";
        case "givenName" -> "personName.givenName.value";
        case "gender" -> "gender.id";
        case "birthDate" -> "birthDate.value";
        default -> s;
    };

    public static String mapOrdersDtoToDomain(Sort.Order order) {
        return mapOperator.apply(order.getProperty());
    }

    public static String mapOrdersDomainToDto(Sort.Order order) {
        return mapOperator.apply(order.getProperty());
    }
}
