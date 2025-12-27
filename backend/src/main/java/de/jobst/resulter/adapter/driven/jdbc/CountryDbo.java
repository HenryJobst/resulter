package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.Country;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ =@PersistenceCreator)
@Table(name = "country")
public class CountryDbo {

    @Id
    @With
    @Column("id")
    @Nullable
    private Long id;

    @Column("code")
    private String code;

    @Column("name")
    private String name;

    public CountryDbo(String code) {
        this.id = null;
        this.code = code;
        this.name = code;
    }

    public static @Nullable CountryDbo from(@Nullable Country country, DboResolvers dboResolvers) {
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
        assert Objects.nonNull(id);
        return Country.of(id, code, name);
    }
}
