package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.Gender;
import de.jobst.resulter.domain.Person;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.time.LocalDate;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "person")
public class PersonDbo {

    @Id
    @With
    @Column("id")
    private Long id;

    @Column("family_name")
    private String familyName;

    @Column("given_name")
    private String givenName;

    @Column("gender")
    private Gender gender;

    @Column("birth_date")
    private LocalDate birthDate;

    public PersonDbo(String familyName, String givenName) {
        this.id = null;
        this.familyName = familyName;
        this.givenName = givenName;
    }

    public static PersonDbo from(Person person, @NonNull DboResolvers dboResolvers) {
        if (null == person) {
            return null;
        }
        PersonDbo personDbo;
        if (person.getId().isPersistent()) {
            personDbo = dboResolvers.getPersonDboResolver().findDboById(person.getId());
            personDbo.setFamilyName(person.getPersonName().familyName().value());
            personDbo.setGivenName(person.getPersonName().givenName().value());
        } else {
            personDbo =
                new PersonDbo(person.getPersonName().familyName().value(), person.getPersonName().givenName().value());
        }
        personDbo.setGender(person.getGender());
        if (person.getBirthDate() != null) {
            personDbo.setBirthDate(person.getBirthDate().value());
        } else {
            personDbo.setBirthDate(null);
        }
        return personDbo;
    }

    public Person asPerson() {
        return Person.of(id, familyName, givenName, birthDate, gender);
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
