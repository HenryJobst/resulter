package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.Gender;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.time.LocalDate;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "PERSON")
public class PersonDbo {

    @Id
    @With
    private Long id;

    private String familyName;

    private String givenName;

    private Gender gender;

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
        if (person.getId().value() != PersonId.empty().value()) {
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
}
