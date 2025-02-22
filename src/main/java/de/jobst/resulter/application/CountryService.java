package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CountryRepository;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryCode;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.CountryName;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
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

    public Collection<Country> findOrCreate(Collection<Country> countries) {
        return countryRepository.findOrCreate(countries);
    }

    public Optional<Country> findById(CountryId id) {
        return countryRepository.findById(id);
    }

    public @NonNull Country updateCountry(CountryId id, CountryCode code, CountryName name) {
        return countryRepository.save(new Country(findById(id).orElseThrow(ResourceNotFoundException::new).getId(), code, name));
    }

    public Country createCountry(CountryCode code, CountryName name) {
        Country country = Country.of(code, name);
        return countryRepository.save(country);
    }
}
