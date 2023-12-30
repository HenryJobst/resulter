package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CountryRepository;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CountryService {

    private final CountryRepository countryRepository;

    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public List<Country> findAll() {
        return countryRepository.findAll();
    }

    public Country findOrCreate(Country country) {
        return countryRepository.findOrCreate(country);
    }

    public Optional<Country> findById(CountryId countryId) {
        return countryRepository.findById(countryId);
    }
}
