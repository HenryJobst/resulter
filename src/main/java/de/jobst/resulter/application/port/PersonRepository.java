package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Repository
public interface PersonRepository {

    Person save(Person event);

    List<Person> findAll();

    Optional<Person> findById(PersonId PersonId);

    Person findOrCreate(Person person);

    Collection<Person> findOrCreate(Collection<Person> persons);

    Page<Person> findAll(@Nullable String filter, @NonNull Pageable pageable);

    void delete(Person merge);
}
