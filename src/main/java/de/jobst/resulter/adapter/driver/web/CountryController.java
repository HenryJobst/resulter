package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.CountryDto;
import de.jobst.resulter.application.CountryService;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryCode;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.CountryName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class CountryController {

    private final CountryService countryService;

    @Autowired
    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping("/country")
    public ResponseEntity<List<CountryDto>> handleCountries() {
        try {
            List<Country> countries = countryService.findAll();
            return ResponseEntity.ok(countries.stream().map(CountryDto::from).toList());
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/country/{id}")
    public ResponseEntity<CountryDto> getCountry(@PathVariable Long id) {
        try {
            Optional<Country> country = countryService.findById(CountryId.of(id));
            return country.map(value -> ResponseEntity.ok(CountryDto.from(value)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/country/{id}")
    public ResponseEntity<CountryDto> updateCountry(@PathVariable Long id, @RequestBody CountryDto countryDto) {
        Country country = countryService.updateCountry(
                CountryId.of(id), CountryCode.of(countryDto.code()), CountryName.of(countryDto.name()));
        return ResponseEntity.ok(CountryDto.from(country));
    }

    @PostMapping("/country")
    public ResponseEntity<CountryDto> createCountry(@RequestBody CountryDto countryDto) {
        try {
            Country country =
                    countryService.createCountry(CountryCode.of(countryDto.code()), CountryName.of(countryDto.name()));
            if (null != country) {
                return ResponseEntity.ok(CountryDto.from(country));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (DataIntegrityViolationException e) {
            logError(e);
            return ResponseEntity.status(HttpStatus.CONFLICT.value()).build();
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private static void logError(Exception e) {
        log.error(e.getMessage());
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }
}
