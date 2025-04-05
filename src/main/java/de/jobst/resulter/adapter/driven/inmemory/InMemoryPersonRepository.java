package de.jobst.resulter.adapter.driven.inmemory;

import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "true")
public class InMemoryPersonRepository implements PersonRepository {

    private final Map<PersonId, Person> persons = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<Person> savedPersons = new ArrayList<>();

    @Override
    public Person save(Person person) {
        Person savedPerson;
        if (ObjectUtils.isEmpty(person.getId()) || person.getId().value() == 0) {
            savedPerson = new Person(
                PersonId.of(sequence.incrementAndGet()),
                person.getPersonName(),
                person.getBirthDate(),
                person.getGender());
        } else {
            savedPerson = person;
        }
        persons.put(person.getId(), savedPerson);
        savedPersons.add(savedPerson);
        return savedPerson;
    }

    @Override
    public List<Person> findAll() {
        return List.copyOf(persons.values());
    }

    @Override
    public Optional<Person> findById(PersonId PersonId) {
        return Optional.ofNullable(persons.get(PersonId));
    }

    @Override
    public PersonPerson findOrCreate(Person person) {
        return new PersonPerson(person, persons.values()
            .stream()
            .filter(it -> Objects.equals(it.getPersonName(), person.getPersonName()) &&
                          Objects.equals(it.getBirthDate(), person.getBirthDate()) &&
                          Objects.equals(it.getGender(), person.getGender()))
            .findAny()
            .orElseGet(() -> save(person)));
    }

    @Override
    public Collection<PersonPerson> findOrCreate(Collection<Person> persons) {
        return persons.stream().map(this::findOrCreate).toList();
    }

    @Override
    public Page<Person> findAll(String filter, @NonNull Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(persons.values()), pageable, persons.size());
    }

    @Override
    public void delete(Person person) {
        persons.remove(person.getId());
    }

    @SuppressWarnings("unused")
    public List<Person> savedPersons() {
        return savedPersons;
    }

    @SuppressWarnings("unused")
    public int saveCount() {
        return savedPersons.size();
    }

    @SuppressWarnings("unused")
    public void resetSaveCount() {
        savedPersons.clear();
    }

}
