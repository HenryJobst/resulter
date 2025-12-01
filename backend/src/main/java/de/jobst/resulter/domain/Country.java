package de.jobst.resulter.domain;

import java.util.Objects;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("ClassCanBeRecord")
@AggregateRoot
@Getter
public final class Country {

    @Identity
    private final CountryId id;

    private final CountryCode code;
    private final @Nullable CountryName name;

    public Country(CountryId id, CountryCode code, @Nullable CountryName name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public static Country of(String code, @Nullable String name) {
        return Country.of(CountryId.empty().value(), code, name);
    }

    public static Country of(long id, String code, @Nullable String name) {
        return new Country(CountryId.of(id), CountryCode.of(code), CountryName.of(name));
    }

    public static Country of(CountryCode code, @Nullable CountryName name) {
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
        return Objects.equals(id, country.id)
                && Objects.equals(code, country.code)
                && Objects.equals(name, country.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name);
    }
}
