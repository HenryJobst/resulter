package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
        if (ObjectUtils.isEmpty(person.getId()) || person.getId().value() == 0) {
            person.setId(PersonId.of(sequence.incrementAndGet()));
        }
        persons.put(person.getId(), person);
        savedPersons.add(person);
        return person;
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
    public Person findOrCreate(Person person) {
        return persons.values()
                .stream()
                .filter(it -> Objects.equals(it.getPersonName(), person.getPersonName()) &&
                        Objects.equals(it.getBirthDate(), person.getBirthDate()) &&
                        Objects.equals(it.getGender(), person.getGender()))
                .findAny()
                .orElseGet(() -> save(person));
    }

    @SuppressWarnings("unused") public List<Person> savedPersons() {
        return savedPersons;
    }

    @SuppressWarnings("unused") public int saveCount() {
        return savedPersons.size();
    }

    @SuppressWarnings("unused") public void resetSaveCount() {
        savedPersons.clear();
    }

}
