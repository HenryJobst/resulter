package de.jobst.resulter.application;

import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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

    public Optional<Person> findById(PersonId personId) {
        return personRepository.findById(personId);
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Collection<Person> findOrCreate(Collection<Person> persons) {
        return personRepository.findOrCreate(persons);
    }

    public Person updatePerson(PersonId personId, PersonName personName, BirthDate birthDate, Gender gender) {
        Optional<Person> optionalPerson = findById(personId);
        if (optionalPerson.isEmpty()) {
            return null;
        }
        Person person = optionalPerson.get();
        person.update(personName, birthDate, gender);
        return personRepository.save(person);
    }

    public Page<Person> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        return personRepository.findAll(filter, pageable);
    }
}
