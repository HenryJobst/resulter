package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CupScoreListRepository;
import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.Gender;
import de.jobst.resulter.domain.Person;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PersonServiceTest {

    private PersonService personService;

    @BeforeEach
    void setUp() {
        PersonRepository personRepository = Mockito.mock(PersonRepository.class);
        ResultListRepository resultListRepository = Mockito.mock(ResultListRepository.class);
        SplitTimeListRepository splitTimeListRepository = Mockito.mock(SplitTimeListRepository.class);
        CupScoreListRepository cupScoreListRepository = Mockito.mock(CupScoreListRepository.class);
        personService = new PersonService(personRepository, resultListRepository, splitTimeListRepository, cupScoreListRepository);
    }

    @Test
    void findDoubles_shouldReturnListOfDoubles() {
        Person person = Person.of(
            1L,
            "John", "Doe", LocalDate.of(1990, 1, 1), Gender.M);

        List<Person> allPersons = Arrays.asList(person,
            Person.of("John", "Doe", LocalDate.of(1990, 1, 1), Gender.M),
            Person.of("Jane", "Doe", LocalDate.of(1992, 2, 2), Gender.F),
            Person.of("Max", "Mustermann", LocalDate.of(1990, 1, 1), Gender.M));

        List<Person> result = personService.findDoubles(person, allPersons);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getPersonName().familyName().value()).isEqualTo("John");
        assertThat(result.getFirst().getPersonName().givenName().value()).isEqualTo("Doe");
    }

    @Test
    void findDoubles_shouldReturnEmptyListWhenNoDoubles() {
        Person person = Person.of(1L, "John", "Doe", LocalDate.of(1990, 1, 1), Gender.M);

        List<Person> allPersons = Arrays.asList(person, Person.of("Meier", "Jim", LocalDate.of(1990, 2, 2), Gender.M));

        List<Person> result = personService.findDoubles(person, allPersons);

        assertThat(result).isEmpty();
    }

    @Test
    void testJaroWinklerDistance() {
        JaroWinklerDistance jaroWinkler = new JaroWinklerDistance();
        String str1 = "Meier";
        String str2 = "John";
        double similarity = jaroWinkler.apply(str1, str2);
        assertThat(similarity).isGreaterThan(0.9);
    }
}
