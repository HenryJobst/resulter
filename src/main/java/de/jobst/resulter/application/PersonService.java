package de.jobst.resulter.application;

import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Transactional
    public Person findOrCreate(Person person) {
        return personRepository.findOrCreate(person);
    }

    @Transactional
    Optional<Person> findById(PersonId personId) {
        return personRepository.findById(personId);
    }
}
