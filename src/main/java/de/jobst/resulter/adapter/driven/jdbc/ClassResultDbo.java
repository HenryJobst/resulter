package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.ClassResult;
import de.jobst.resulter.domain.Gender;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "CLASS_RESULT")
public class ClassResultDbo {

    @Id
    @With
    private Long id;

    private String name;

    private String shortName;

    private Gender gender;

    public ClassResultDbo(String name) {
        this.id = null;
        this.name = name;
    }

    private Set<PersonResultDbo> personResults = new HashSet<>();

    public static ClassResultDbo from(ClassResult classResult, @NonNull DboResolvers dboResolvers) {
        ClassResultDbo classResultDbo = new ClassResultDbo(classResult.classResultName().value());
        if (StringUtils.isNotEmpty(classResult.classResultShortName().value())) {
            classResultDbo.setShortName(classResult.classResultShortName().value());
        }
        classResultDbo.setGender(classResult.gender());
        classResultDbo.setPersonResults(classResult.personResults()
            .value()
            .stream()
            .map(x -> PersonResultDbo.from(x, dboResolvers))
            .collect(Collectors.toSet()));
        return classResultDbo;
    }

    static public Collection<ClassResult> asClassResults(Collection<ClassResultDbo> classResultDbos) {
        return classResultDbos.stream()
            .map(it -> ClassResult.of(it.name,
                it.shortName,
                it.gender,
                PersonResultDbo.asPersonResults(it.getPersonResults())))
            .toList();
    }
}
