package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.Gender;
import de.jobst.resulter.domain.Person;
import jakarta.persistence.*;

import java.time.LocalDate;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed", "unused"})
@Entity
@Table(name = "PERSON")
public class PersonDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator_person")
    @SequenceGenerator(name = "entity_generator_person", sequenceName = "SEQ_PERSON_ID", allocationSize = 10)
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Column(name = "FAMILY_NAME", nullable = false)
    private String familyName;

    @Column(name = "GIVEN_NAME", nullable = false)
    private String givenName;

    @Column(name = "GENDER", nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    public static PersonDbo from(Person person) {
        PersonDbo personDbo = new PersonDbo();
        if (person.getId() != null) {
            personDbo.setId(person.getId().value());
        }
        personDbo.setFamilyName(person.getPersonName().familyName().value());
        personDbo.setGivenName(person.getPersonName().givenName().value());
        personDbo.setGender(person.getGender());
        if (person.getBirthDate() != null) {
            personDbo.setBirthDate(person.getBirthDate().value());
        }
        return personDbo;
    }

    public Person asPerson() {
        return Person.of(id, familyName, givenName, birthDate, gender);
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Gender getGender() {
        return gender;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }
}