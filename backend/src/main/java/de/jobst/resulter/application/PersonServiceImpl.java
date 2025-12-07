package de.jobst.resulter.application;

import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final ResultListRepository resultListRepository;
    private final SplitTimeListRepository splitTimeListRepository;
    private final CupScoreListRepository cupScoreListRepository;
    private final EventCertificateStatRepository eventCertificateStatRepository;

    public PersonServiceImpl(
        PersonRepository personRepository,
        ResultListRepository resultListRepository,
        SplitTimeListRepository splitTimeListRepository,
        CupScoreListRepository cupScoreListRepository, EventCertificateStatRepository eventCertificateStatRepository) {
        this.personRepository = personRepository;
        this.resultListRepository = resultListRepository;
        this.splitTimeListRepository = splitTimeListRepository;
        this.cupScoreListRepository = cupScoreListRepository;
        this.eventCertificateStatRepository = eventCertificateStatRepository;
    }

    private static boolean isJaroWinklerSimilar(double similarity) {
        return similarity < 0.5; // Adjust the threshold as needed
    }

    private static boolean isSimilar(double similarity) {
        return similarity > 750; // Adjust the threshold as needed
    }

    private static double getSimilarity(String str1, String str2) {
        JaroWinklerDistance jaroWinkler = new JaroWinklerDistance();
        return jaroWinkler.apply(str1, str2);
    }

    private static long getDaysBetween(@org.jspecify.annotations.Nullable BirthDate date1,
                                       @org.jspecify.annotations.Nullable BirthDate date2) {
        if (date1 == null || date2 == null || date1.value() == null || date2.value() == null) {
            return 100_000; // a large number
        }
        return ChronoUnit.DAYS.between(date1.value(), date2.value());
    }

    @Override
    public PersonRepository.PersonPerson findOrCreate(Person person) {
        return personRepository.findOrCreate(person);
    }

    @Override
    public Person getById(PersonId personId) {
        return personRepository.findById(personId).orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Optional<Person> findById(PersonId personId) {
        return personRepository.findById(personId);
    }

    @Override
    public List<Person> findAll() {
        return personRepository.findAll();
    }

    @Override
    public Collection<PersonRepository.PersonPerson> findOrCreate(Collection<Person> persons) {
        return personRepository.findOrCreate(persons);
    }

    @Override
    public Person updatePerson(PersonId personId, PersonName personName, BirthDate birthDate, Gender gender) {
        return findById(personId)
                .map(person -> personRepository.save(new Person(person.id(), personName, birthDate, gender)))
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Page<Person> findAllOrPossibleDuplicates(@org.jspecify.annotations.Nullable String filter, Pageable pageable,
                                                    boolean duplicates) {
        return duplicates ? personRepository.findDuplicates(filter, pageable) : personRepository.findAll(filter, pageable);
    }

    @Override
    @Deprecated
    public Page<Person> findAll(@Nullable String filter, Pageable pageable) {
        return findAllOrPossibleDuplicates(filter, pageable, false);
    }

    @Override
    @Deprecated
    public Page<Person> findDuplicates(@Nullable String filter, Pageable pageable) {
        return findAllOrPossibleDuplicates(filter, pageable, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Person> findDoubles(PersonId personId) {
        return findDoubles(personRepository.findById(personId).orElseThrow());
    }

    private List<Person> findDoubles(Person person) {
        List<Person> all = personRepository.findAll();
        return findDoubles(person, all);
    }

    List<Person> findDoubles(Person person, List<Person> all) {
        return all.stream()
                .filter(p -> !p.id().equals(person.id()))
                .map(p -> new PersonSimilarity(p, calculateSimilarity(p, person)))
                .filter(ps -> isSimilar(ps.similarity))
                .sorted((p1, p2) -> Double.compare(p2.similarity, p1.similarity))
                .map(PersonSimilarity::person)
                .toList();
    }

    private boolean isSimilarDate(long daysBetween) {
        return Math.abs(daysBetween) <= 30; // Adjust the range as needed
    }

    private double calculateSimilarity(Person person1, Person person2) {
        // calculate a similarity score for sorting
        double score = 0.0;
        double familyNameSimilarity = getSimilarity(
                person1.personName().familyName().value(),
                person2.personName().familyName().value());
        if (isJaroWinklerSimilar(familyNameSimilarity)) {
            score += 1000.0 - (1000.0 * familyNameSimilarity);
        }
        double givenNameSimilarity = getSimilarity(
                person1.personName().givenName().value(),
                person2.personName().givenName().value());
        if (isJaroWinklerSimilar(givenNameSimilarity)) {
            score += 100.0 - (100.0 * givenNameSimilarity);
        }
        long daysBetween = getDaysBetween(
                person1.birthDate(), person2.birthDate());
        if (isSimilarDate(daysBetween)) {
            score += 10.0 - (10.0 * Math.abs(daysBetween) / 30.0);
        }
        if (person1.gender().equals(person2.gender())) {
            score += 1.0;
        }
        return score;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Person mergePersons(PersonId personId, PersonId mergeId) {
        Person person = personRepository.findById(personId).orElseThrow();
        Person merge = personRepository.findById(mergeId).orElseThrow();
        replacePerson(merge, person);
        personRepository.delete(merge);
        return person;
    }

    @Override
    public void deletePerson(PersonId personId) {
        Person person = findById(personId).orElseThrow(ResourceNotFoundException::new);
        personRepository.delete(person);
    }

    private void replacePerson(Person merge, Person person) {
        resultListRepository.replacePersonId(merge.id(), person.id());
        splitTimeListRepository.replacePersonId(merge.id(), person.id());
        cupScoreListRepository.replacePersonId(merge.id(), person.id());
        eventCertificateStatRepository.replacePersonId(merge.id(), person.id());
    }

    record PersonSimilarity(Person person, double similarity) {}
}
