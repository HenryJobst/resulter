package de.jobst.resulter.adapter.driven.jpa;

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
public class PersonRepositoryDataJpaAdapter implements PersonRepository {

    private final PersonJpaRepository personJpaRepository;

    public PersonRepositoryDataJpaAdapter(PersonJpaRepository personJpaRepository) {
        this.personJpaRepository = personJpaRepository;
    }

    @Override
    public Person save(Person person) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setPersonDboResolver(id -> personJpaRepository.findById(id.value()).orElseThrow());
        PersonDbo personEntity = PersonDbo.from(person, null, dboResolvers);
        PersonDbo savedPersonEntity = personJpaRepository.save(personEntity);
        return savedPersonEntity.asPerson();
    }

    @Override
    public List<Person> findAll() {
        return personJpaRepository.findAll().stream().map(PersonDbo::asPerson).sorted().toList();
    }

    @Override
    public Optional<Person> findById(PersonId personId) {
        Optional<PersonDbo> personEntity = personJpaRepository.findById(personId.value());
        return personEntity.map(PersonDbo::asPerson);
    }

    @Override
    public Person findOrCreate(Person person) {
        Optional<PersonDbo> personEntity =
            personJpaRepository.findByFamilyNameAndGivenNameAndBirthDateAndGender(person.getPersonName()
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
