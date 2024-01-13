package de.jobst.resulter.application;

import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Person findOrCreate(Person person) {
        return personRepository.findOrCreate(person);
    }

    Optional<Person> findById(PersonId personId) {
        return personRepository.findById(personId);
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Collection<Person> findOrCreate(Collection<Person> persons) {
        return personRepository.findOrCreate(persons);
    }
}
