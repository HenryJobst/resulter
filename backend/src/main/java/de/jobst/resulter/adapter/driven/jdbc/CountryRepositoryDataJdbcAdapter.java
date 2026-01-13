package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.CountryRepository;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class CountryRepositoryDataJdbcAdapter implements CountryRepository {

    private final CountryJdbcRepository countryJdbcRepository;

    public CountryRepositoryDataJdbcAdapter(CountryJdbcRepository countryJdbcRepository) {
        this.countryJdbcRepository = countryJdbcRepository;
    }

    @Override
    @Transactional
    public Country save(Country country) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setCountryDboResolver((id) -> countryJdbcRepository.findById(id.value()).orElseThrow());
        CountryDbo countryEntity = CountryDbo.from(country, dboResolvers);
        assert countryEntity != null;
        CountryDbo savedCountryEntity = countryJdbcRepository.save(countryEntity);
        return savedCountryEntity.asCountry();
    }

    @Override
    @Transactional
    public List<Country> findAll() {
        return countryJdbcRepository.findAll().stream().map(CountryDbo::asCountry).toList();
    }

    @Override
    @Transactional
    public Optional<Country> findById(CountryId countryId) {
        Optional<CountryDbo> countryEntity = countryJdbcRepository.findById(countryId.value());
        return countryEntity.map(CountryDbo::asCountry);
    }

    @Override
    @Transactional
    public Map<CountryId, Country> findAllById(Set<CountryId> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        Set<Long> rawIds = ids.stream().map(CountryId::value).collect(Collectors.toSet());
        Collection<CountryDbo> countryDbos = countryJdbcRepository.findAllByIdIn(rawIds);
        return countryDbos.stream()
            .map(CountryDbo::asCountry)
            .collect(Collectors.toMap(Country::getId, country -> country));
    }

    @Override
    @Transactional
    public Country findOrCreate(Country country) {
        Optional<CountryDbo> countryEntity = countryJdbcRepository.findByCode(country.getCode().value());
        if (countryEntity.isEmpty()) {
            return save(country);
        }
        CountryDbo entity = countryEntity.get();
        return entity.asCountry();
    }

    @Override
    @Transactional
    public Collection<Country> findOrCreate(Collection<Country> countries) {
        return countries.stream().map(this::findOrCreate).toList();
    }
}
