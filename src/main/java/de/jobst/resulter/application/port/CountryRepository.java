package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@SecondaryPort
public interface CountryRepository {

    Country save(Country country);

    List<Country> findAll();

    Optional<Country> findById(CountryId CountryId);

    Country findOrCreate(Country country);

    Collection<Country> findOrCreate(Collection<Country> countries);
}
