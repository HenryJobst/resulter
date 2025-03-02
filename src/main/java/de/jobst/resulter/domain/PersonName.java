package de.jobst.resulter.domain;

import java.text.MessageFormat;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public record PersonName(FamilyName familyName, GivenName givenName) implements Comparable<PersonName> {

    public String getFullName() {
        return MessageFormat.format("{1} {0}", familyName.value(), givenName.value());
    }

    public static PersonName of(FamilyName familyName, GivenName givenName) {
        return new PersonName(familyName, givenName);
    }

    public static PersonName of(String familyName, String givenName) {
        return new PersonName(FamilyName.of(familyName), GivenName.of(givenName));
    }

    @Override
    public int compareTo(@NonNull PersonName o) {
        int val = familyName.compareTo(o.familyName);
        if (val == 0) {
            val = givenName.compareTo(o.givenName);
        }
        return val;
    }
}
