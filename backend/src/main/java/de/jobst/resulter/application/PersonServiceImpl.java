package de.jobst.resulter.application;

import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
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

    private static boolean isSimilar(double score) {
        return score > 750; // Score threshold for duplicates
    }

    private static boolean isStrictDuplicate(double score) {
        return score > 1050; // Stricter threshold - requires both family AND given name to be similar
    }

    private static double getSimilarity(String str1, String str2) {
        JaroWinklerDistance jaroWinkler = new JaroWinklerDistance();
        return jaroWinkler.apply(str1, str2);
    }

    private static long getDaysBetween(@Nullable BirthDate date1,
                                       @Nullable BirthDate date2) {
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
    public Page<Person> findAllOrPossibleDuplicates(@Nullable String filter, Pageable pageable,
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

    // Find strict duplicates for grouping (higher threshold)
    List<Person> findStrictDuplicates(Person person, List<Person> all) {
        return all.stream()
                .filter(p -> !p.id().equals(person.id()))
                .map(p -> {
                    double similarity = calculateSimilarity(p, person);
                    // Debug logging
                    log.debug("Strict duplicate check: {} {} (ID {}) vs {} {} (ID {}): similarity = {}",
                            person.personName().familyName().value(),
                            person.personName().givenName().value(),
                            person.id().value(),
                            p.personName().familyName().value(),
                            p.personName().givenName().value(),
                            p.id().value(),
                            similarity);
                    return new PersonSimilarity(p, similarity);
                })
                .filter(ps -> {
                    boolean isStrict = isStrictDuplicate(ps.similarity);
                    if (isStrict) {
                        log.info("STRICT DUPLICATE FOUND: {} (ID {}) and {} (ID {}) with similarity {}",
                                person.personName().familyName().value() + " " + person.personName().givenName().value(),
                                person.id().value(),
                                ps.person.personName().familyName().value() + " " + ps.person.personName().givenName().value(),
                                ps.person.id().value(),
                                ps.similarity);
                    }
                    return isStrict;
                })
                .sorted((p1, p2) -> Double.compare(p2.similarity, p1.similarity))
                .map(PersonSimilarity::person)
                .toList();
    }

    private boolean isSimilarDate(long daysBetween) {
        return Math.abs(daysBetween) <= 30; // Adjust the range as needed
    }

    private double calculateSimilarity(Person person1, Person person2) {
        // Calculate a similarity score for sorting (higher score = more similar)
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

    @Override
    public java.util.Set<Long> determineGroupLeaders(List<Person> persons) {
        // Two-tier approach:
        // 1. Use strict similarity (>900) to group persons - only these share a merge button
        // 2. Use regular similarity (>750) to find duplicates - these are suggested for merging

        // Use Union-Find to group persons with strict similarity
        java.util.Map<Long, Long> parent = new java.util.HashMap<>();

        // Initialize: each person is its own parent
        for (Person person : persons) {
            parent.put(person.id().value(), person.id().value());
        }

        // Find operation with path compression
        java.util.function.Function<Long, Long> find = new java.util.function.Function<>() {
            public Long apply(Long id) {
                if (!parent.get(id).equals(id)) {
                    parent.put(id, apply(parent.get(id))); // Path compression
                }
                return parent.get(id);
            }
        };

        // Union operation: connect only STRICT duplicates
        for (Person person : persons) {
            Long personId = person.id().value();
            List<Person> strictDuplicates = findStrictDuplicates(person, persons);

            for (Person duplicate : strictDuplicates) {
                Long duplicateId = duplicate.id().value();
                // Union: connect the two trees
                Long root1 = find.apply(personId);
                Long root2 = find.apply(duplicateId);

                if (!root1.equals(root2)) {
                    // Always attach the larger root to the smaller one
                    // This ensures the smallest ID becomes the root
                    if (root1 < root2) {
                        parent.put(root2, root1);
                    } else {
                        parent.put(root1, root2);
                    }
                }
            }
        }

        // Collect group leaders: persons that have duplicates (regular threshold) and are the root of their strict group
        java.util.Set<Long> groupLeaders = new java.util.HashSet<>();

        for (Person person : persons) {
            Long personId = person.id().value();
            List<Person> duplicates = findDoubles(person, persons);

            // Only show merge button if this person has at least one duplicate (regular threshold)
            if (!duplicates.isEmpty()) {
                // But group by strict threshold - only the root of each strict group gets the button
                Long root = find.apply(personId);
                if (root.equals(personId)) {
                    groupLeaders.add(personId);
                }
            }
        }

        return groupLeaders;
    }

    private void replacePerson(Person merge, Person person) {
        resultListRepository.replacePersonId(merge.id(), person.id());
        splitTimeListRepository.replacePersonId(merge.id(), person.id());
        cupScoreListRepository.replacePersonId(merge.id(), person.id());
        eventCertificateStatRepository.replacePersonId(merge.id(), person.id());
    }

    record PersonSimilarity(Person person, double similarity) {}
}
