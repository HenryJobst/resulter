package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CountryRepository;
import de.jobst.resulter.application.port.CountryService;
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
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;

    public CountryServiceImpl(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Override
    public List<Country> findAll() {
        return countryRepository.findAll();
    }

    @Override
    public Country findOrCreate(Country country) {
        return countryRepository.findOrCreate(country);
    }

    @Override
    public Collection<Country> findOrCreate(Collection<Country> countries) {
        return countryRepository.findOrCreate(countries);
    }

    @Override
    public Country getById(CountryId countryId) {
        return countryRepository.findById(countryId).orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Optional<Country> findById(CountryId id) {
        return countryRepository.findById(id);
    }

    @NonNull
    @Override
    public Country updateCountry(CountryId id, CountryCode code, CountryName name) {
        return countryRepository.save(new Country(
                findById(id).orElseThrow(ResourceNotFoundException::new).getId(), code, name));
    }

    @Override
    public Country createCountry(CountryCode code, CountryName name) {
        Country country = Country.of(code, name);
        return countryRepository.save(country);
    }
}
