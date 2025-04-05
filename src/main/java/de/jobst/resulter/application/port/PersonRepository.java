package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@SecondaryPort
public interface PersonRepository {

    Person save(Person event);

    List<Person> findAll();

    Optional<Person> findById(PersonId PersonId);

    record PersonPerson(Person source, Person target) {}

    PersonPerson findOrCreate(Person person);

    Collection<PersonPerson> findOrCreate(Collection<Person> persons);

    Page<Person> findAll(@Nullable String filter, @NonNull Pageable pageable);

    void delete(Person merge);
}
