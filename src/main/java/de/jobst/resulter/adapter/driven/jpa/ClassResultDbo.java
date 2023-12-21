package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.*;
import jakarta.persistence.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
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
        if (classResult.getId().value() != ClassResultId.empty().value()) {
            classResultDbo.setId(classResult.getId().value());
        }
        classResultDbo.setEventDbo(eventDbo);
        classResultDbo.setName(classResult.getClassResultName().value());
        if (StringUtils.isNotEmpty(classResult.getClassResultShortName().value())) {
            classResultDbo.setShortName(classResult.getClassResultShortName().value());
        }
        classResultDbo.setGender(classResult.getGender());
        classResultDbo.setPersonResults(
                Objects.requireNonNull(classResult.getPersonResults())
                        .get().value()
                        .stream()
                        .map(it -> PersonResultDbo.from(it, classResultDbo))
                        .collect(Collectors.toSet()));
        return classResultDbo;
    }

    static public Collection<ClassResult> asClassResults(EventConfig eventConfig,
                                                         Collection<ClassResultDbo> classResultDbos) {

        Map<ClassResultId, List<PersonResult>> personResultsByClassResultId;
        if (!eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.PERSON_RESULTS)) {
            personResultsByClassResultId =
                    PersonResultDbo.asPersonResults(eventConfig,
                                    classResultDbos.stream().flatMap(x -> x.personResults.stream()).toList())
                            .stream()
                            .collect(Collectors.groupingBy(PersonResult::getClassResultId));
        } else {
            personResultsByClassResultId = null;
        }
        return classResultDbos.stream()
                .map(
                        it -> ClassResult.of(it.id,
                                it.eventDbo != null ? it.eventDbo.getId() : EventId.empty().value(),
                                it.name, it.shortName, it.gender,
                                personResultsByClassResultId == null ? null :
                                        personResultsByClassResultId.getOrDefault(ClassResultId.of(it.id),
                                                new ArrayList<>())))
                .toList();
    }

    public long getId() {
        return id != null ? id : ClassResultId.empty().value();
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

    @SuppressWarnings("unused")
    public EventDbo getEventDbo() {
        return eventDbo;
    }

    public void setEventDbo(EventDbo eventDbo) {
        this.eventDbo = eventDbo;
    }
}