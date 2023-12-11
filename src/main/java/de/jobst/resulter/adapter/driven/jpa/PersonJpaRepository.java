package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PersonJpaRepository extends JpaRepository<PersonDbo, Long> {
    Optional<PersonDbo> findByFamilyNameAndGivenNameAndBirthDateAndGender(String familyName,
                                                                          String givenName,
                                                                          LocalDate birthDate,
                                                                          Gender gender);
}
