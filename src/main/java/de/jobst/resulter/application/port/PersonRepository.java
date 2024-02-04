package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PersonRepository {

    Person save(Person event);

    List<Person> findAll();

    Optional<Person> findById(PersonId PersonId);

    Person findOrCreate(Person person);

    Collection<Person> findOrCreate(Collection<Person> persons);

}
