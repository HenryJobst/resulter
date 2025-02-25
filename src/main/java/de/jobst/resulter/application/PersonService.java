package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CupScoreListRepository;
import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final ResultListRepository resultListRepository;
    private final SplitTimeListRepository splitTimeListRepository;
    private final CupScoreListRepository cupScoreListRepository;

    public PersonService(
            PersonRepository personRepository,
            ResultListRepository resultListRepository,
            SplitTimeListRepository splitTimeListRepository,
            CupScoreListRepository cupScoreListRepository) {
        this.personRepository = personRepository;
        this.resultListRepository = resultListRepository;
        this.splitTimeListRepository = splitTimeListRepository;
        this.cupScoreListRepository = cupScoreListRepository;
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

    private static long getDaysBetween(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            return 100_000; // a large number
        }
        return ChronoUnit.DAYS.between(date1, date2);
    }

    public Person findOrCreate(Person person) {
        return personRepository.findOrCreate(person);
    }

    public Person getById(PersonId personId) {
        return personRepository.findById(personId).orElseThrow(ResourceNotFoundException::new);
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

    public @NonNull Person updatePerson(PersonId personId, PersonName personName, BirthDate birthDate, Gender gender) {
        return findById(personId)
                .map(person -> personRepository.save(new Person(person.getId(), personName, birthDate, gender)))
                .orElseThrow(ResourceNotFoundException::new);
    }

    public Page<Person> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        return personRepository.findAll(filter, pageable);
    }

    public List<Person> findDoubles(PersonId personId) {
        return findDoubles(personRepository.findById(personId).orElseThrow());
    }

    @NonNull
    private List<Person> findDoubles(Person person) {
        List<Person> all = personRepository.findAll();
        return findDoubles(person, all);
    }

    @Transactional(readOnly = true)
    @NonNull
    List<Person> findDoubles(Person person, List<Person> all) {
        return all.stream()
                .filter(p -> !p.getId().equals(person.getId()))
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
                person1.getPersonName().familyName().value(),
                person2.getPersonName().familyName().value());
        if (isJaroWinklerSimilar(familyNameSimilarity)) {
            score += 1000.0 - (1000.0 * familyNameSimilarity);
        }
        double givenNameSimilarity = getSimilarity(
                person1.getPersonName().givenName().value(),
                person2.getPersonName().givenName().value());
        if (isJaroWinklerSimilar(givenNameSimilarity)) {
            score += 100.0 - (100.0 * givenNameSimilarity);
        }
        long daysBetween = getDaysBetween(
                person1.getBirthDate().value(), person2.getBirthDate().value());
        if (isSimilarDate(daysBetween)) {
            score += 10.0 - (10.0 * Math.abs(daysBetween) / 30.0);
        }
        if (person1.getGender().equals(person2.getGender())) {
            score += 1.0;
        }
        return score;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Person mergePersons(PersonId personId, PersonId mergeId) {
        Person person = personRepository.findById(personId).orElseThrow();
        Person merge = personRepository.findById(mergeId).orElseThrow();
        replacePerson(merge, person);
        personRepository.delete(merge);
        return person;
    }

    private void replacePerson(Person merge, Person person) {
        resultListRepository.replacePersonId(merge.getId(), person.getId());
        splitTimeListRepository.replacePersonId(merge.getId(), person.getId());
        cupScoreListRepository.replacePersonId(merge.getId(), person.getId());
    }

    record PersonSimilarity(Person person, double similarity) {}
}
