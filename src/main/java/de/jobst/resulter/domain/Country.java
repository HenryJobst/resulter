package de.jobst.resulter.domain;

import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.Objects;

@Getter
public final class Country {

    @NonNull
    private final CountryId id;

    private final CountryCode code;
    private final CountryName name;

    public Country(@NonNull CountryId id, @NonNull CountryCode code, @NonNull CountryName name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public static Country of(String code, String name) {
        return Country.of(CountryId.empty().value(), code, name);
    }

    public static Country of(long id, String code, String name) {
        return new Country(CountryId.of(id), CountryCode.of(code), CountryName.of(name));
    }

    public static Country of(CountryCode code, CountryName name) {
        return new Country(CountryId.empty(), code, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Country country = (Country) o;
        return Objects.equals(id, country.id) && Objects.equals(code, country.code) && Objects.equals(name, country.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name);
    }
}
