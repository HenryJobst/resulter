package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.ClassResult;
import de.jobst.resulter.domain.CourseId;
import de.jobst.resulter.domain.Gender;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "class_result")
public class ClassResultDbo {

    @Column("name")
    private String name;

    @NonNull @Column("short_name")
    private String shortName;

    @Column("gender")
    private Gender gender;

    @Column("course_id")
    private AggregateReference<CourseDbo, Long> course;

    @MappedCollection(idColumn = "result_list_id")
    private Set<PersonResultDbo> personResults = new HashSet<>();

    public ClassResultDbo(String name) {
        this.name = name;
    }

    public static ClassResultDbo from(ClassResult classResult) {
        ClassResultDbo classResultDbo =
                new ClassResultDbo(classResult.classResultName().value());
        classResultDbo.setShortName(Objects.requireNonNull(
                Objects.requireNonNull(classResult.classResultShortName()).value()));
        classResultDbo.setGender(classResult.gender());
        classResultDbo.setPersonResults(classResult.personResults().value().stream()
                .map(PersonResultDbo::from)
                .collect(Collectors.toSet()));
        classResultDbo.setCourse(
                classResult.courseId() == null
                        ? null
                        : AggregateReference.to(classResult.courseId().value()));
        return classResultDbo;
    }

    public static Collection<ClassResult> asClassResults(Collection<ClassResultDbo> classResultDbos) {
        return classResultDbos.stream()
                .map(it -> ClassResult.of(
                        it.name,
                        it.shortName,
                        it.gender,
                        PersonResultDbo.asPersonResults(it.getPersonResults()),
                        it.course == null ? null : CourseId.of(it.course.getId())))
                .toList();
    }
}
