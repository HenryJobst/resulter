package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.GenderDto;
import de.jobst.resulter.adapter.driver.web.dto.PersonDto;
import de.jobst.resulter.application.PersonService;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class PersonController {

    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/person")
    public ResponseEntity<List<PersonDto>> handlePersons() {
        try {
            List<Person> persons = personService.findAll();
            return ResponseEntity.ok(persons.stream().map(PersonDto::from).toList());
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/person/{id}")
    public ResponseEntity<PersonDto> getPerson(@PathVariable Long id) {
        try {
            Optional<Person> person = personService.findById(PersonId.of(id));
            return person.map(value -> ResponseEntity.ok(PersonDto.from(value)))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/person/{id}")
    public ResponseEntity<PersonDto> updatePerson(@PathVariable Long id, @RequestBody PersonDto personDto) {
        try {
            Person person = personService.updatePerson(PersonId.of(id),
                PersonName.of(personDto.familyName(), personDto.givenName()),
                ObjectUtils.isNotEmpty(personDto.birthDate()) ? BirthDate.of(personDto.birthDate()) : null,
                Gender.of(personDto.gender().id()));
            if (null != person) {
                return ResponseEntity.ok(PersonDto.from(person));
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (DataIntegrityViolationException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (IllegalArgumentException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/gender")
    public ResponseEntity<List<GenderDto>> handleGender() {
        try {
            List<GenderDto> gender = Arrays.stream(Gender.values()).map(GenderDto::from).toList();
            return ResponseEntity.ok(gender);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static void logError(Exception e) {
        log.error(e.getMessage());
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }
}
