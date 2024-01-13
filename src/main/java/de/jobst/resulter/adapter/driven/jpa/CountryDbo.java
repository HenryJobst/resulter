package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.Country;
import jakarta.persistence.*;
import org.springframework.lang.NonNull;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed", "unused"})
@Entity
@Table(name = "COUNTRY")
public class CountryDbo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator_country")
    @SequenceGenerator(name = "entity_generator_country", sequenceName = "SEQ_COUNTRY_ID", allocationSize = 1)
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "NAME", nullable = false)
    private String name;

    public static CountryDbo from(Country country, @NonNull DboResolvers dboResolvers) {
        if (null == country) {
            return null;
        }
        CountryDbo countryDbo;
        if (country.getId().isPersistent()) {
            countryDbo = dboResolvers.getCountryDboResolver().findDboById(country.getId());
        } else {
            countryDbo = new CountryDbo();
        }
        countryDbo.setCode(country.getCode().value());
        countryDbo.setName(country.getName().value());
        return countryDbo;
    }

    public Country asCountry() {
        return Country.of(id, code, name);
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
