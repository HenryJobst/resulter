package de.jobst.resulter.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class EqualsVerifierTestCase {

    @Test
    public void equalsContractForPerson() {
        EqualsVerifier.forClass(Person.class).verify();
    }

    @Test
    public void equalsContractForPersonName() {
        EqualsVerifier.forClass(PersonName.class).verify();
    }

    @Test
    public void equalsContractForFamilyName() {
        EqualsVerifier.forClass(FamilyName.class).verify();
    }

    @Test
    public void equalsContractForGivenName() {
        EqualsVerifier.forClass(GivenName.class).verify();
    }

    @Test
    public void equalsContractForBirthDate() {
        EqualsVerifier.forClass(BirthDate.class).verify();
    }

    @Test
    public void equalsContractForGender() {
        EqualsVerifier.forClass(Gender.class).verify();
    }

    @Test
    public void equalsContractForPersonId() {
        EqualsVerifier.forClass(PersonId.class).verify();
    }

    @Test
    public void equalsContractForOrganisation() {
        EqualsVerifier.forClass(Organisation.class)
            .withPrefabValues(Organisation.class, Organisation.of("1", "1"), Organisation.of("2", "2"))
            .withIgnoredFields("type", "childOrganisations")
            .verify();
    }

    @Test
    public void equalsContractForCountry() {
        EqualsVerifier.forClass(Country.class).verify();
    }

    @Test
    public void equalsContractForCourse() {
        EqualsVerifier.forClass(Course.class)
            .withIgnoredFields("courseLength", "courseClimb", "numberOfControls")
            .verify();
    }
}
