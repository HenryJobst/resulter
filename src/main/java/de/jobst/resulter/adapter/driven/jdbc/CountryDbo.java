package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.Country;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "COUNTRY")
public class CountryDbo {

    @Id
    @With
    private Long id;

    private String code;

    private String name;

    public CountryDbo(String code) {
        this.id = null;
        this.code = code;
        this.name = code;
    }

    public static CountryDbo from(Country country, @NonNull DboResolvers dboResolvers) {
        if (null == country) {
            return null;
        }
        CountryDbo countryDbo;
        if (country.getId().isPersistent()) {
            countryDbo = dboResolvers.getCountryDboResolver().findDboById(country.getId());
            countryDbo.setCode(country.getCode().value());
        } else {
            countryDbo = new CountryDbo(country.getCode().value());
        }
        countryDbo.setName(country.getName().value());
        return countryDbo;
    }

    public Country asCountry() {
        return Country.of(id, code, name);
    }
}
