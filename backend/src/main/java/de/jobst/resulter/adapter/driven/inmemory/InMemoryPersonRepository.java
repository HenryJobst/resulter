package de.jobst.resulter.adapter.driven.inmemory;

import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "true")
public class InMemoryPersonRepository implements PersonRepository {

    private final Map<PersonId, Person> persons = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<Person> savedPersons = new ArrayList<>();

    @Override
    public Person save(Person person) {
        Person savedPerson;
        if (ObjectUtils.isEmpty(person.id()) || person.id().value() == 0) {
            savedPerson = new Person(
                    PersonId.of(sequence.incrementAndGet()),
                    person.personName(),
                    person.birthDate(),
                    person.gender());
        } else {
            savedPerson = person;
        }
        persons.put(person.id(), savedPerson);
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
        return new PersonPerson(
                person,
                persons.values().stream()
                        .filter(it -> Objects.equals(it.personName(), person.personName())
                                && Objects.equals(it.birthDate(), person.birthDate())
                                && Objects.equals(it.gender(), person.gender()))
                        .findAny()
                        .orElseGet(() -> save(person)));
    }

    @Override
    public Collection<PersonPerson> findOrCreate(Collection<Person> persons) {
        return persons.stream().map(this::findOrCreate).toList();
    }

    @Override
    public Page<@NonNull Person> findAll(@Nullable String filter, @org.jspecify.annotations.NonNull Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(persons.values()), pageable, persons.size());
    }

    @Override
    public Page<@NonNull Person> findDuplicates(String filter, @NonNull Pageable pageable) {
        // simple in-memory implementation for tests/dev
        List<Person> all = new ArrayList<>(persons.values());
        // optional basic filter on familyName/givenName/id substrings when filter string is simple like
        // "familyName=='X'"
        // For simplicity, ignore complex filters here.
        Map<String, Long> counts = new HashMap<>();
        for (Person p : all) {
            String key = p.personName().familyName().value() + "\u0000"
                    + p.personName().givenName().value();
            counts.put(key, counts.getOrDefault(key, 0L) + 1);
        }
        List<Person> duplicates = all.stream()
                .filter(p -> counts.getOrDefault(
                                p.personName().familyName().value() + "\u0000"
                                        + p.personName().givenName().value(),
                                0L)
                        > 1)
                .sorted()
                .toList();
        int pageSize = pageable.isPaged() ? pageable.getPageSize() : duplicates.size();
        int pageNumber = pageable.isPaged() ? pageable.getPageNumber() : 0;
        int fromIndex = Math.min(pageNumber * pageSize, duplicates.size());
        int toIndex = Math.min(fromIndex + pageSize, duplicates.size());
        List<Person> content = duplicates.subList(fromIndex, toIndex);
        return new PageImpl<>(content, pageable, duplicates.size());
    }

    @Override
    public void delete(Person person) {
        persons.remove(person.id());
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

    @Override
    public Map<PersonId, Person> findAllById(Set<PersonId> idSet) {
        return persons.values().stream()
                .filter(person -> idSet.contains(person.id()))
                .collect(Collectors.toMap(Person::id, person -> person));
    }
}
