package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.Gender;
import de.jobst.resulter.domain.Person;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ =@PersistenceCreator)
@Table(name = "person")
public class PersonDbo {

    @Id
    @With
    @Column("id")
    @Nullable
    private Long id;

    @Column("family_name")
    private String familyName;

    @Column("given_name")
    private String givenName;

    @Column("gender")
    private Gender gender;

    @Column("birth_date")
    @Nullable
    private LocalDate birthDate;

    public PersonDbo(String familyName, String givenName) {
        this.id = null;
        this.familyName = familyName;
        this.givenName = givenName;
    }

    public static PersonDbo from(Person person, DboResolvers dboResolvers) {
        PersonDbo personDbo;
        if (person.id().isPersistent()) {
            personDbo = Optional.ofNullable(dboResolvers.getPersonDboResolver()).orElseThrow().findDboById(person.id());
            personDbo.setFamilyName(person.personName().familyName().value());
            personDbo.setGivenName(person.personName().givenName().value());
        } else {
            personDbo =
                new PersonDbo(person.personName().familyName().value(), person.personName().givenName().value());
        }
        personDbo.setGender(person.gender());
        if (person.birthDate() != null) {
            personDbo.setBirthDate(person.birthDate().value());
        } else {
            personDbo.setBirthDate(null);
        }
        return personDbo;
    }

    public static Person asPerson(PersonDbo personDbo) {
        return personDbo.asPerson();
    }

    public Person asPerson() {
        return Person.of(id != null ? id : 0, familyName, givenName, birthDate, gender);
    }

    public static String mapOrdersDomainToDbo(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            case "personName.familyName.value" -> "familyName";
            case "personName.givenName.value" -> "givenName";
            case "gender.id" -> "gender";
            case "birthDate.value" -> "birthDate";
            default -> order.getProperty();
        };
    }

    public static String mapOrdersDboToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            case "familyName" -> "personName.familyName.value";
            case "givenName" -> "personName.givenName.value";
            case "gender" -> "gender.id";
            case "birthDate" -> "birthDate.value";
            default -> order.getProperty();
        };
    }
}
