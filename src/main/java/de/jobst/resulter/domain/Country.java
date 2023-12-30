package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

@SuppressWarnings("FieldMayBeFinal")
@Getter
public class Country {

    @NonNull
    @Setter
    private CountryId id;

    private CountryCode code;
    private CountryName name;

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
}
