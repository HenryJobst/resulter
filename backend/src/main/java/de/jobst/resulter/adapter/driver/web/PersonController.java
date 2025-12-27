package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.GenderDto;
import de.jobst.resulter.adapter.driver.web.dto.PersonDto;
import de.jobst.resulter.application.port.PersonService;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/person/all")
    public ResponseEntity<List<PersonDto>> getAllPersons() {
        List<Person> persons = personService.findAll();
        return ResponseEntity.ok(persons.stream().map(PersonDto::from).toList());
    }

    @GetMapping("/person")
    public ResponseEntity<Page<PersonDto>> searchPersons(@RequestParam Optional<String> filter,
                                                         @RequestParam Optional<Boolean> duplicates,
                                                         @Nullable Pageable pageable) {
        boolean dup = duplicates.orElse(false);
        Pageable mapped = pageable != null
                ? FilterAndSortConverter.mapOrderProperties(pageable, PersonDto::mapOrdersDtoToDomain)
                : Pageable.unpaged();
        Page<Person> persons = personService.findAllOrPossibleDuplicates(
                filter.orElse(null),
                mapped,
                dup);
        return ResponseEntity.ok(new PageImpl<>(
                persons.getContent().stream().map(PersonDto::from).toList(),
                FilterAndSortConverter.mapOrderProperties(persons.getPageable(), PersonDto::mapOrdersDomainToDto),
                persons.getTotalElements()));
    }

    @GetMapping("/person/duplicates")
    public ResponseEntity<Page<PersonDto>> searchDuplicatePersons(@RequestParam Optional<String> filter,
                                                                  @Nullable Pageable pageable) {
        Pageable mapped = pageable != null
                ? FilterAndSortConverter.mapOrderProperties(pageable, PersonDto::mapOrdersDtoToDomain)
                : Pageable.unpaged();
        Page<Person> persons = personService.findAllOrPossibleDuplicates(
                filter.orElse(null),
                mapped,
                true);
        return ResponseEntity.ok(new PageImpl<>(
                persons.getContent().stream().map(PersonDto::from).toList(),
                FilterAndSortConverter.mapOrderProperties(persons.getPageable(), PersonDto::mapOrdersDomainToDto),
                persons.getTotalElements()));
    }

    @GetMapping("/person/{id}")
    public ResponseEntity<PersonDto> getPerson(@PathVariable Long id) {
        Optional<Person> person = personService.findById(PersonId.of(id));
        return person.map(value -> ResponseEntity.ok(PersonDto.from(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/person/{id}")
    public ResponseEntity<PersonDto> updatePerson(@PathVariable Long id, @RequestBody PersonDto personDto) {
        Person person = personService.updatePerson(
                PersonId.of(id),
                PersonName.of(personDto.familyName(), personDto.givenName()),
                ObjectUtils.isNotEmpty(personDto.birthDate()) ? BirthDate.of(personDto.birthDate()) : null,
                Gender.of(personDto.gender().id()));
        return ResponseEntity.ok(PersonDto.from(person));
    }

    @DeleteMapping("/person/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        personService.deletePerson(PersonId.of(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/gender")
    public ResponseEntity<List<GenderDto>> handleGender() {
        List<GenderDto> gender =
                Arrays.stream(Gender.values()).map(GenderDto::from).toList();
        return ResponseEntity.ok(gender);
    }

    @GetMapping("/person/{id}/doubles")
    public ResponseEntity<List<PersonDto>> getDoubles(@PathVariable Long id) {
        List<Person> doubles = personService.findDoubles(PersonId.of(id));
        return ResponseEntity.ok(doubles.stream().map(PersonDto::from).toList());
    }

    @PostMapping("/person/{id}/merge")
    public ResponseEntity<PersonDto> mergePersons(@PathVariable Long id, @RequestBody Long removeId) {
        Person person = personService.mergePersons(PersonId.of(id), PersonId.of(removeId));
        return ResponseEntity.ok(PersonDto.from(person));
    }
}
