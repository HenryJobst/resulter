package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.Gender;
import de.jobst.resulter.domain.Person;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "PERSON")
@Getter
@Setter
@NoArgsConstructor
public class PersonDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator")
    @SequenceGenerator(name = "entity_generator", sequenceName = "SEQ_PERSON_ID", allocationSize = 10)
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Column(name = "FAMILY_NAME", nullable = false)
    private String familyName;

    @Column(name = "GIVEN_NAME", nullable = false)
    private String givenName;

    @Column(name = "GENDER", nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "BIRTH_DATE", nullable = false)
    private LocalDate birthDate;

    public static PersonDbo from(Person person) {
        PersonDbo personDbo = new PersonDbo();
        if (person.getId() != null) {
            personDbo.setId(person.getId().value());
        }
        personDbo.setFamilyName(person.getPersonName().familyName().value());
        personDbo.setGivenName(person.getPersonName().givenName().value());
        personDbo.setGender(person.getGender());
        personDbo.setBirthDate(person.getBirthDate().value());
        return personDbo;
    }

    public Person asPerson() {
        return Person.of(id, familyName, givenName, birthDate, gender);
    }
}