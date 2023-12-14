package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.ClassResult;
import de.jobst.resulter.domain.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "CLASS_RESULT")
@NoArgsConstructor
public class ClassResultDbo {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "class_result_generator")
    @SequenceGenerator(name = "class_result_generator", sequenceName = "SEQ_CLASS_RESULT_ID")
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "SHORT_NAME", nullable = false)
    private String shortName;

    @Column(name = "GENDER", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_ID")
    private EventDbo eventDbo;

    @OneToMany(mappedBy = "classResultDbo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PersonResultDbEntity> personResults = new HashSet<>();

    public static ClassResultDbo from(ClassResult classResult, EventDbo eventDbo) {
        ClassResultDbo classResultDbo = new ClassResultDbo();
        if (classResult.id() != null) {
            classResultDbo.setId(classResult.id().value());
        }
        classResultDbo.setEventDbo(eventDbo);
        classResultDbo.setName(classResult.classResultName().value());
        classResultDbo.setShortName(classResult.classResultShortName().value());
        classResultDbo.setGender(classResult.gender());
        classResultDbo.setPersonResults(
                Objects.requireNonNull(classResult.personResults())
                        .value()
                        .stream()
                        .map(it -> PersonResultDbEntity.from(it, classResultDbo))
                        .collect(Collectors.toSet()));
        return classResultDbo;
    }

    public ClassResult asClassResult() {
        return ClassResult.of(getName(),
                getShortName(),
                getGender(),
                getPersonResults().stream().map(PersonResultDbEntity::asPersonResult).toList()
        );
    }
}