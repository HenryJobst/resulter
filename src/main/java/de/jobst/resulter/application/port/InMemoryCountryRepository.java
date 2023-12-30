package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "true")
public class InMemoryCountryRepository implements CountryRepository {
    private final Map<CountryId, Country> countries = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<Country> savedCountrys = new ArrayList<>();

    @Override
    public Country save(Country country) {
        if (ObjectUtils.isEmpty(country.getId()) || country.getId().value() == 0) {
            country.setId(CountryId.of(sequence.incrementAndGet()));
        }
        countries.put(country.getId(), country);
        savedCountrys.add(country);
        return country;
    }

    @Override
    public List<Country> findAll() {
        return List.copyOf(countries.values());
    }

    @Override
    public Optional<Country> findById(CountryId CountryId) {
        return Optional.ofNullable(countries.get(CountryId));
    }

    @Override
    public Country findOrCreate(Country country) {
        return countries.values()
                .stream()
                .filter(it -> Objects.equals(it.getName(), country.getName()))
                .findAny()
                .orElseGet(() -> save(country));
    }

    @SuppressWarnings("unused")
    public List<Country> savedCountrys() {
        return savedCountrys;
    }

    @SuppressWarnings("unused")
    public int saveCount() {
        return savedCountrys.size();
    }

    @SuppressWarnings("unused")
    public void resetSaveCount() {
        savedCountrys.clear();
    }

}
