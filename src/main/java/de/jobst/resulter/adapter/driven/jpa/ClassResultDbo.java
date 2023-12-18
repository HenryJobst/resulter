package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.ClassResult;
import de.jobst.resulter.domain.EventConfig;
import de.jobst.resulter.domain.Gender;
import jakarta.persistence.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed"})
@Entity
@Table(name = "CLASS_RESULT")
public class ClassResultDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "class_result_generator")
    @SequenceGenerator(name = "class_result_generator", sequenceName = "SEQ_CLASS_RESULT_ID")
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "SHORT_NAME")
    private String shortName;

    @Column(name = "GENDER", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_ID")
    private EventDbo eventDbo;

    @OneToMany(mappedBy = "classResultDbo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PersonResultDbo> personResults = new HashSet<>();

    public static ClassResultDbo from(ClassResult classResult, EventDbo eventDbo) {
        ClassResultDbo classResultDbo = new ClassResultDbo();
        if (classResult.id() != null) {
            classResultDbo.setId(classResult.id().value());
        }
        classResultDbo.setEventDbo(eventDbo);
        classResultDbo.setName(classResult.classResultName().value());
        if (StringUtils.isNotEmpty(classResult.classResultShortName().value())) {
            classResultDbo.setShortName(classResult.classResultShortName().value());
        }
        classResultDbo.setGender(classResult.gender());
        classResultDbo.setPersonResults(
                Objects.requireNonNull(classResult.personResults())
                        .value()
                        .stream()
                        .map(it -> PersonResultDbo.from(it, classResultDbo))
                        .collect(Collectors.toSet()));
        return classResultDbo;
    }

    public ClassResult asClassResult(EventConfig eventConfig) {
        return ClassResult.of(getName(),
                getShortName(),
                getGender(),
                getPersonResults().stream().map(it -> it.asPersonResult(eventConfig)).toList()
        );
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Collection<PersonResultDbo> getPersonResults() {
        return personResults;
    }

    private void setPersonResults(Set<PersonResultDbo> personResultDbos) {
        this.personResults = personResultDbos;
    }

    public EventDbo getEventDbo() {
        return eventDbo;
    }

    public void setEventDbo(EventDbo eventDbo) {
        this.eventDbo = eventDbo;
    }
}