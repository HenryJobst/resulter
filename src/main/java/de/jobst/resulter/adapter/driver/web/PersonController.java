package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.GenderDto;
import de.jobst.resulter.adapter.driver.web.dto.PersonDto;
import de.jobst.resulter.application.PersonService;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class PersonController {

    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    private static void logError(Exception e) {
        log.error(e.getMessage());
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }

    @GetMapping("/person/all")
    public ResponseEntity<List<PersonDto>> getAllPersons() {
        try {
            List<Person> persons = personService.findAll();
            return ResponseEntity.ok(persons.stream().map(PersonDto::from).toList());
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/person")
    public ResponseEntity<Page<PersonDto>> searchPersons(@RequestParam Optional<String> filter,
                                                         @PageableDefault(page = 0, size = 5000) Pageable pageable) {
        try {
            Page<Person> persons = personService.findAll(filter.orElse(null),
                pageable != null ?
                FilterAndSortConverter.mapOrderProperties(pageable, PersonDto::mapOrdersDtoToDomain) :
                Pageable.unpaged());
            return ResponseEntity.ok(new PageImpl<>(persons.getContent().stream().map(PersonDto::from).toList(),
                FilterAndSortConverter.mapOrderProperties(persons.getPageable(), PersonDto::mapOrdersDomainToDto),
                persons.getTotalElements()));
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/person/{id}")
    public ResponseEntity<PersonDto> getPerson(@PathVariable Long id) {
        try {
            Optional<Person> person = personService.findById(PersonId.of(id));
            return person.map(value -> ResponseEntity.ok(PersonDto.from(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/person/{id}")
    public ResponseEntity<PersonDto> updatePerson(@PathVariable Long id, @RequestBody PersonDto personDto) {
            Person person = personService.updatePerson(PersonId.of(id),
                PersonName.of(personDto.familyName(), personDto.givenName()),
                ObjectUtils.isNotEmpty(personDto.birthDate()) ? BirthDate.of(personDto.birthDate()) : null,
                Gender.of(personDto.gender().id()));
                return ResponseEntity.ok(PersonDto.from(person));
    }

    @GetMapping("/gender")
    public ResponseEntity<List<GenderDto>> handleGender() {
        try {
            List<GenderDto> gender = Arrays.stream(Gender.values()).map(GenderDto::from).toList();
            return ResponseEntity.ok(gender);
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/person/{id}/doubles")
    public ResponseEntity<List<PersonDto>> getDoubles(@PathVariable Long id) {
        try {
            List<Person> doubles = personService.findDoubles(PersonId.of(id));
            return ResponseEntity.ok(doubles.stream().map(PersonDto::from).toList());
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/person/{id}/merge")
    public ResponseEntity<PersonDto> mergePersons(@PathVariable Long id, @RequestBody Long removeId) {
        try {
            Person person = personService.mergePersons(PersonId.of(id), PersonId.of(removeId));
            return ResponseEntity.ok(PersonDto.from(person));
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
