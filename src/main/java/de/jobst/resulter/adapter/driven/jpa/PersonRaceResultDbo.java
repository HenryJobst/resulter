package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.*;
import jakarta.persistence.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed", "unused"})
@Entity
@Table(name = "PERSON_RACE_RESULT")
public class PersonRaceResultDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator_person_race_result")
    @SequenceGenerator(name = "entity_generator_person_race_result",
            sequenceName = "SEQ_PERSON_RACE_RESULT_ID",
            allocationSize = 10)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PERSON_RESULT_ID", nullable = false)
    private PersonResultDbo personResultDbo;

    @Column(name = "RACE_NUMBER", nullable = false)
    private Long raceNumber;
    @Column(name = "START_TIME")
    private ZonedDateTime startTime;
    @Column(name = "FINISH_TIME")
    private ZonedDateTime finishTime;
    @Column(name = "PUNCH_TIME")
    private Double punchTime;
    @Column(name = "POSITION")
    private Long position;
    @Column(name = "STATE")
    @Enumerated(value = EnumType.STRING)
    private ResultStatus state;

    @OneToMany(mappedBy = "personRaceResultDbo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SplitTimeDbo> splitTimes = new HashSet<>();

    public static PersonRaceResultDbo from(@NonNull PersonRaceResult personRaceResult,
                                           @NonNull PersonResultDbo personResultDbo,
                                           @Nullable DboResolver<PersonRaceResultId, PersonRaceResultDbo> dboResolver,
                                           @NonNull DboResolvers dboResolvers) {
        PersonRaceResultDbo personRaceResultDbo = null;
        PersonRaceResultDbo persistedPersonRaceResultDbo;
        if (personRaceResult.getId().value() != PersonRaceResultId.empty().value()) {
            if (dboResolver != null) {
                personRaceResultDbo = dboResolver.findDboById(personRaceResult.getId());
            }
            if (personRaceResultDbo == null) {
                personRaceResultDbo =
                        dboResolvers.getPersonRaceResultDboResolver().findDboById(personRaceResult.getId());
            }
            persistedPersonRaceResultDbo = personRaceResultDbo;
        } else {
            personRaceResultDbo = new PersonRaceResultDbo();
            persistedPersonRaceResultDbo = null;
        }

        personRaceResultDbo.setPersonResultDbo(personResultDbo);
        if (ObjectUtils.isNotEmpty(personRaceResult.getStartTime())) {
            personRaceResultDbo.setStartTime(personRaceResult.getStartTime().value());
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.getFinishTime())) {
            personRaceResultDbo.setFinishTime(personRaceResult.getFinishTime().value());
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.getRuntime())) {
            personRaceResultDbo.setPunchTime(personRaceResult.getRuntime().value());
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.getRaceNumber())) {
            personRaceResultDbo.setRaceNumber(personRaceResult.getRaceNumber().value());
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.getPositon())) {
            personRaceResultDbo.setPosition(personRaceResult.getPositon().value());
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.getState())) {
            personRaceResultDbo.setState(personRaceResult.getState());
        }
        if (personRaceResult.getSplitTimes().isLoaded()) {
            PersonRaceResultDbo finalPersonRaceResultDbo = personRaceResultDbo;
            personRaceResultDbo.setSplitTimes(personRaceResult.getSplitTimes().get()
                    .value()
                    .stream()
                    .map(it -> {
                        SplitTimeDbo persistedSplitTimeDbo =
                                persistedPersonRaceResultDbo != null ?
                                        (persistedPersonRaceResultDbo.getSplitTimes()
                                                .stream()
                                                .filter(x -> x.getId() == it.getId().value())
                                                .findFirst()
                                                .orElse(null))
                                        : null;
                        return SplitTimeDbo.from(it,
                                finalPersonRaceResultDbo,
                                (id) -> persistedSplitTimeDbo,
                                dboResolvers);
                    })
                    .collect(Collectors.toSet()));
        } else if (persistedPersonRaceResultDbo != null) {
            personRaceResultDbo.setSplitTimes(persistedPersonRaceResultDbo.getSplitTimes());

        } else if (personRaceResult.getId().isPersistent()) {
            throw new IllegalArgumentException();
        }

        return personRaceResultDbo;
    }

    public static Collection<PersonRaceResult> asPersonRaceResults(@NonNull EventConfig eventConfig,
                                                                   @NonNull
                                                                   List<PersonRaceResultDbo> personRaceResultDbos) {
        Map<PersonRaceResultId, List<SplitTime>> splitTimesByPersonRaceResultId;
        if (!eventConfig.shallowLoads().contains(EventConfig.ShallowEventLoads.SPLIT_TIMES)) {
            splitTimesByPersonRaceResultId =
                    SplitTimeDbo.asSplitTimes(personRaceResultDbos.stream()
                                    .flatMap(x -> x.splitTimes.stream())
                                    .sorted()
                                    .toList())
                            .stream()
                            .collect(Collectors.groupingBy(SplitTime::getPersonRaceResultId));
        } else {
            splitTimesByPersonRaceResultId = null;
        }
        return personRaceResultDbos.stream()
                .map(
                        it -> PersonRaceResult.of(
                                it.id,
                                it.getPersonResultDbo() != null ?
                                        it.getPersonResultDbo().getId() :
                                        PersonResultId.empty().value(),
                                it.raceNumber,
                                it.getStartTime(),
                                it.getFinishTime(),
                                it.getPunchTime(),
                                it.getPosition(),
                                it.getState(),
                                splitTimesByPersonRaceResultId == null ? null :
                                        splitTimesByPersonRaceResultId.getOrDefault(
                                                PersonRaceResultId.of(it.id), new ArrayList<>())))
                .toList();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(ZonedDateTime finishTime) {
        this.finishTime = finishTime;
    }

    public Double getPunchTime() {
        return punchTime;
    }

    public void setPunchTime(Double punchTime) {
        this.punchTime = punchTime;
    }

    public Long getRaceNumber() {
        return raceNumber;
    }

    public void setRaceNumber(Long raceNumber) {
        this.raceNumber = raceNumber;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public ResultStatus getState() {
        return state;
    }

    public void setState(ResultStatus state) {
        this.state = state;
    }

    public PersonResultDbo getPersonResultDbo() {
        return personResultDbo;
    }

    public void setPersonResultDbo(PersonResultDbo personResultDbo) {
        this.personResultDbo = personResultDbo;
    }

    public Set<SplitTimeDbo> getSplitTimes() {
        return splitTimes;
    }

    public void setSplitTimes(Set<SplitTimeDbo> splitTimes) {
        this.splitTimes = splitTimes;
    }
}