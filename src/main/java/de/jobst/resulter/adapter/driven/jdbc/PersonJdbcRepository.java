package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.Gender;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface PersonJdbcRepository
    extends CrudRepository<PersonDbo, Long>, PagingAndSortingRepository<PersonDbo, Long>,
            QueryByExampleExecutor<PersonDbo> {

    @NonNull
    Collection<PersonDbo> findAll();

    Optional<PersonDbo> findByFamilyNameAndGivenNameAndBirthDateAndGender(String familyName,
                                                                          String givenName,
                                                                          LocalDate birthDate,
                                                                          Gender gender);
}
