package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.Gender;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;

@Repository
@RepositoryRestResource(path = "persons")
public interface PersonJdbcRepository
    extends CrudRepository<PersonDbo, Long>, PagingAndSortingRepository<PersonDbo, Long> {

    @NonNull
    Collection<PersonDbo> findAll();

    Optional<PersonDbo> findByFamilyNameAndGivenNameAndBirthDateAndGender(String familyName,
                                                                          String givenName,
                                                                          ZonedDateTime birthDate,
                                                                          Gender gender);
}
