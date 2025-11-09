package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.CountryDto;
import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryCode;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.CountryName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping("/country/all")
    public ResponseEntity<List<CountryDto>> getAllCountries() {
        List<Country> countries = countryService.findAll();
        return ResponseEntity.ok(countries.stream().map(CountryDto::from).toList());
    }

    @GetMapping("/country")
    public ResponseEntity<List<CountryDto>> handleCountries() {
        List<Country> countries = countryService.findAll();
        return ResponseEntity.ok(countries.stream().map(CountryDto::from).toList());
    }

    @GetMapping("/country/{id}")
    public ResponseEntity<CountryDto> getCountry(@PathVariable Long id) {
        Optional<Country> country = countryService.findById(CountryId.of(id));
        return country.map(value -> ResponseEntity.ok(CountryDto.from(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/country/{id}")
    public ResponseEntity<CountryDto> updateCountry(@PathVariable Long id, @RequestBody CountryDto countryDto) {
        Country country = countryService.updateCountry(
                CountryId.of(id), CountryCode.of(countryDto.code()), CountryName.of(countryDto.name()));
        return ResponseEntity.ok(CountryDto.from(country));
    }

    @PostMapping("/country")
    public ResponseEntity<CountryDto> createCountry(@RequestBody CountryDto countryDto) {
        Country country =
                countryService.createCountry(CountryCode.of(countryDto.code()), CountryName.of(countryDto.name()));
        if (null != country) {
            return ResponseEntity.ok(CountryDto.from(country));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
