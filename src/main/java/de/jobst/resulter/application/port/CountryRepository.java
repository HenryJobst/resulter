package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;

import java.util.List;
import java.util.Optional;

public interface CountryRepository {
    Country save(Country country);

    List<Country> findAll();

    Optional<Country> findById(CountryId CountryId);

    Country findOrCreate(Country country);
}
