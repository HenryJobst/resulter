package de.jobst.resulter.domain.comparators;

import de.jobst.resulter.domain.Person;

import java.util.Comparator;

public class PersonComparator {

    public static final Comparator<Person> COMPARATOR = Comparator.comparing(Person::personName)
    .thenComparing(Person::birthDate, Comparator.nullsLast(Comparator.naturalOrder()))
    .thenComparing(Person::gender)
    .thenComparing(Person::id);
}
