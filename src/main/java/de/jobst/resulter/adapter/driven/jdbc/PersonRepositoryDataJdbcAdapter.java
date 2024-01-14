package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class PersonRepositoryDataJdbcAdapter implements PersonRepository {

    private final PersonJdbcRepository personJdbcRepository;

    public PersonRepositoryDataJdbcAdapter(PersonJdbcRepository personJdbcRepository) {
        this.personJdbcRepository = personJdbcRepository;
    }

    @Override
    public Person save(Person person) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setPersonDboResolver(id -> personJdbcRepository.findById(id.value()).orElseThrow());
        PersonDbo personEntity = PersonDbo.from(person, dboResolvers);
        PersonDbo savedPersonEntity = personJdbcRepository.save(personEntity);
        return savedPersonEntity.asPerson();
    }

    @Override
    public List<Person> findAll() {
        return personJdbcRepository.findAll().stream().map(PersonDbo::asPerson).sorted().toList();
    }

    @Override
    public Optional<Person> findById(PersonId personId) {
        Optional<PersonDbo> personEntity = personJdbcRepository.findById(personId.value());
        return personEntity.map(PersonDbo::asPerson);
    }

    @Override
    public Person findOrCreate(Person person) {
        Optional<PersonDbo> personEntity =
            personJdbcRepository.findByFamilyNameAndGivenNameAndBirthDateAndGender(person.getPersonName()
                    .familyName()
                    .value(),
                person.getPersonName().givenName().value(),
                person.getBirthDate().value(),
                person.getGender());
        if (personEntity.isEmpty()) {
            return save(person);
        }
        PersonDbo entity = personEntity.get();
        return entity.asPerson();
    }

    @Override
    @Transactional
    public Collection<Person> findOrCreate(Collection<Person> persons) {
        return persons.stream().map(this::findOrCreate).toList();
    }
}
