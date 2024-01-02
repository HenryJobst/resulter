package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.application.port.CountryRepository;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class CountryRepositoryDataJpaAdapter implements CountryRepository {

    private final CountryJpaRepository countryJpaRepository;

    public CountryRepositoryDataJpaAdapter(CountryJpaRepository countryJpaRepository) {
        this.countryJpaRepository = countryJpaRepository;
    }

    @Override
    @Transactional
    public Country save(Country country) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setCountryDboResolver(
                (id) -> countryJpaRepository.findById(id.value()).orElseThrow()
        );
        CountryDbo countryEntity = CountryDbo.from(country, null, dboResolvers);
        CountryDbo savedCountryEntity = countryJpaRepository.save(countryEntity);
        return savedCountryEntity.asCountry();
    }

    @Override
    @Transactional
    public List<Country> findAll() {
        return countryJpaRepository.findAll().stream()
                .map(CountryDbo::asCountry)
                .toList();
    }

    @Override
    @Transactional
    public Optional<Country> findById(CountryId countryId) {
        Optional<CountryDbo> countryEntity =
                countryJpaRepository.findById(countryId.value());
        return countryEntity.map(CountryDbo::asCountry);
    }

    @Override
    @Transactional
    public Country findOrCreate(Country country) {
        Optional<CountryDbo> countryEntity =
                countryJpaRepository.findByName(country.getName().value());
        if (countryEntity.isEmpty()) {
            return save(country);
        }
        CountryDbo entity = countryEntity.get();
        return entity.asCountry();
    }
}
