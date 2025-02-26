package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryCode;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.CountryName;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@PrimaryPort
public interface CountryService {
    List<Country> findAll();

    Country findOrCreate(Country country);

    Collection<Country> findOrCreate(Collection<Country> countries);

    Country getById(CountryId countryId);

    Optional<Country> findById(CountryId id);

    @NonNull
    Country updateCountry(CountryId id, CountryCode code, CountryName name);

    Country createCountry(CountryCode code, CountryName name);
}
