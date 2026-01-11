package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.*;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@PrimaryPort
public interface PersonService {

    PersonRepository.PersonPerson findOrCreate(Person person);

    Person getById(PersonId personId);

    Optional<Person> findById(PersonId personId);

    List<Person> findAll();

    Collection<PersonRepository.PersonPerson> findOrCreate(Collection<Person> persons);

    @NonNull
    Person updatePerson(PersonId personId, PersonName personName, BirthDate birthDate, Gender gender);

    /**
     * Unified search for persons with optional duplicate mode.
     * @param filter optional filter expression
     * @param pageable pageable and sort information
     * @param duplicates when true, only persons that have potential duplicates are returned
     */
    Page<Person> findAllOrPossibleDuplicates(@Nullable String filter, @NonNull Pageable pageable, boolean duplicates);

    /**
     * Deprecated: use {@link #findAllOrPossibleDuplicates(String, Pageable, boolean)} with duplicates=false
     */
    @Deprecated
    Page<Person> findAll(@Nullable String filter, @NonNull Pageable pageable);

    /**
     * Deprecated: use {@link #findAllOrPossibleDuplicates(String, Pageable, boolean)} with duplicates=true
     */
    @Deprecated
    Page<Person> findDuplicates(@Nullable String filter, @NonNull Pageable pageable);

    List<Person> findDoubles(PersonId personId);

    @Transactional(propagation = Propagation.REQUIRED)
    Person mergePersons(PersonId personId, PersonId mergeId);

    @Transactional
    void deletePerson(PersonId personId);

    /**
     * Determines which persons in the list should show the merge button.
     * Uses a two-tier similarity approach:
     * - Regular threshold (>750): determines if a person has duplicates (needed for merge button)
     * - Strict threshold (>1050): groups very similar persons together (only smallest ID gets button)
     * Returns a set of person IDs that should show the merge button.
     */
    java.util.Set<Long> determineGroupLeaders(List<Person> persons);
}
