package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.Gender;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
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

    @Query("""
    SELECT * FROM person
    WHERE family_name = :familyName
      AND given_name = :givenName
      AND birth_date = COALESCE(:birthDate, birth_date)
      AND (:gender = 'U' OR gender = COALESCE(:gender, gender))
      ORDER BY family_name, given_name, birth_date ASC nulls last, gender ASC nulls last
      LIMIT 1
    """)
    Optional<PersonDbo> findByFamilyNameAndGivenNameAndBirthDateAndGender(
                                          @Param("familyName") String familyName,
                                          @Param("givenName") String givenName,
                                          @Param("birthDate") @Nullable LocalDate birthDate,
                                          @Param("gender") Gender gender);
}
