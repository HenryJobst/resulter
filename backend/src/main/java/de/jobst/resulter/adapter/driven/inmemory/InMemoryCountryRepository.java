package de.jobst.resulter.adapter.driven.inmemory;

import de.jobst.resulter.application.port.CountryRepository;
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
        Country savedCountry;
        if (ObjectUtils.isEmpty(country.getId()) || country.getId().value() == 0) {
            savedCountry = new Country(
                CountryId.of(sequence.incrementAndGet()),
                country.getCode(),
                country.getName());
        } else {
            savedCountry = country;
        }
        countries.put(country.getId(), savedCountry);
        savedCountrys.add(savedCountry);
        return savedCountry;
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
    public Map<CountryId, Country> findAllById(Set<CountryId> ids) {
        Map<CountryId, Country> result = new HashMap<>();
        for (CountryId id : ids) {
            Country country = countries.get(id);
            if (country != null) {
                result.put(id, country);
            }
        }
        return result;
    }

    @Override
    public Country findOrCreate(Country country) {
        return countries.values()
            .stream()
            .filter(it -> Objects.equals(it.getName(), country.getName()))
            .findAny()
            .orElseGet(() -> save(country));
    }

    @Override
    public Collection<Country> findOrCreate(Collection<Country> countries) {
        return countries.stream().map(this::findOrCreate).toList();
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
