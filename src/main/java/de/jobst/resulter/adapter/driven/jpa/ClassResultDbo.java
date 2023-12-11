package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.ClassResult;
import de.jobst.resulter.domain.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Entity
@Table(name = "CLASS_RESULT")
public class ClassResultDbo {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "class_result_generator")
    @SequenceGenerator(name = "class_result_generator", sequenceName = "SEQ_CLASS_RESULT_ID", allocationSize = 50)
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

    public static ClassResultDbo from(ClassResult classResult, EventDbo eventDbo) {
        ClassResultDbo classResultDbo = new ClassResultDbo();
        classResultDbo.setEventDbo(eventDbo);
        classResultDbo.setName(classResult.classResultName().value());
        classResultDbo.setShortName(classResult.classResultShortName().value());
        classResultDbo.setGender(classResult.gender());
        return classResultDbo;
    }

    public ClassResult asClassResult() {
        return ClassResult.of(getName(), getShortName(), getGender(), new ArrayList<>());
    }
}