package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.*;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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

    Page<Person> findAll(@Nullable String filter, @NonNull Pageable pageable);

    List<Person> findDoubles(PersonId personId);

    @Transactional(propagation = Propagation.REQUIRED)
    Person mergePersons(PersonId personId, PersonId mergeId);
}
