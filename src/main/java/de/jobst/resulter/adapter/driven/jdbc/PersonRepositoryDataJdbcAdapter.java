package de.jobst.resulter.adapter.driven.jdbc;

import com.turkraft.springfilter.converter.FilterStringConverter;
import com.turkraft.springfilter.parser.node.FilterNode;
import com.turkraft.springfilter.transformer.FilterNodeTransformer;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformResult;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformer;
import de.jobst.resulter.adapter.driver.web.FilterAndSortConverter;
import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
@Slf4j
public class PersonRepositoryDataJdbcAdapter implements PersonRepository {

    private final PersonJdbcRepository personJdbcRepository;
    private final FilterStringConverter filterStringConverter;
    private final FilterNodeTransformer<MappingFilterNodeTransformResult> filterNodeTransformer;

    public PersonRepositoryDataJdbcAdapter(
            PersonJdbcRepository personJdbcRepository, FilterStringConverter filterStringConverter) {
        this.personJdbcRepository = personJdbcRepository;
        this.filterStringConverter = filterStringConverter;
        this.filterNodeTransformer = new MappingFilterNodeTransformer(new DefaultConversionService());
    }

    @Override
    public Person save(Person person) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setPersonDboResolver(
                id -> personJdbcRepository.findById(id.value()).orElseThrow());
        PersonDbo personEntity = PersonDbo.from(person, dboResolvers);
        PersonDbo savedPersonEntity = personJdbcRepository.save(personEntity);
        return savedPersonEntity.asPerson();
    }

    @Override
    public List<Person> findAll() {
        return personJdbcRepository.findAll().stream()
                .map(it -> it.asPerson())
                .sorted()
                .toList();
    }

    @Override
    public Optional<Person> findById(PersonId personId) {
        Optional<PersonDbo> personEntity = personJdbcRepository.findById(personId.value());
        return personEntity.map(it -> it.asPerson());
    }

    @Override
    public PersonPerson findOrCreate(Person person) {
        Optional<PersonDbo> personEntity =
            personJdbcRepository.findByFamilyNameAndGivenNameAndBirthDateAndGender(
                    person.getPersonName().familyName().value(),
                    person.getPersonName().givenName().value(),
                    person.getBirthDate().value(),
                    person.getGender());

        return personEntity.map(personDbo -> new PersonPerson(person, personDbo.asPerson()))
            .orElseGet(() -> new PersonPerson(person, save(person)));
    }

    @Override
    @Transactional
    public Collection<PersonPerson> findOrCreate(Collection<Person> persons) {
        return persons.stream().map(this::findOrCreate).toList();
    }

    @Override
    public Page<Person> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        Page<PersonDbo> page;
        if (filter != null) {
            PersonDbo personDbo = new PersonDbo();
            AtomicReference<ExampleMatcher> matcher = new AtomicReference<>(
                    ExampleMatcher.matching()
                    // .withIgnorePaths("")
                    );
            FilterNode filterNode = filterStringConverter.convert(filter);
            log.info("FilterNode: {}", filterNode);
            MappingFilterNodeTransformResult transformResult = filterNodeTransformer.transform(filterNode);
            transformResult.filterMap().forEach((key, value) -> {
                String unquotedValue = value.value().replace("'", "");
                switch (key) {
                    case "familyName" -> {
                        personDbo.setFamilyName(unquotedValue);
                        matcher.set(matcher.get().withMatcher("familyName", m -> m.stringMatcher(value.matcher())));
                    }
                    case "givenName" -> {
                        personDbo.setGivenName(unquotedValue);
                        matcher.set(matcher.get().withMatcher("givenName", m -> m.stringMatcher(value.matcher())));
                    }
                    case "id" -> {
                        personDbo.setId(Long.parseLong(unquotedValue));
                        matcher.set(matcher.get().withMatcher("id", ExampleMatcher.GenericPropertyMatcher::exact));
                    }
                }
            });

            page = personJdbcRepository.findAll(
                    Example.of(personDbo, matcher.get()),
                    FilterAndSortConverter.mapOrderProperties(pageable, PersonDbo::mapOrdersDomainToDbo));

        } else {
            page = personJdbcRepository.findAll(
                    FilterAndSortConverter.mapOrderProperties(pageable, PersonDbo::mapOrdersDomainToDbo));
        }
        return new PageImpl<>(
                page.stream().map(x -> PersonDbo.asPerson(x)).toList(),
                FilterAndSortConverter.mapOrderProperties(page.getPageable(), PersonDbo::mapOrdersDboToDomain),
                page.getTotalElements());
    }

    @Override
    public void delete(Person person) {
        personJdbcRepository.deleteById(person.getId().value());
    }
}
