package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.*;
import jakarta.persistence.*;
import org.apache.commons.lang3.ObjectUtils;

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
    private List<SplitTimeDbo> splitTimes = new ArrayList<>();

    public static PersonRaceResultDbo from(PersonRaceResult personRaceResult, PersonResultDbo personResultDbo) {
        PersonRaceResultDbo personRaceResultDbo = new PersonRaceResultDbo();
        personRaceResultDbo.setPersonResultDbo(personResultDbo);
        if (personRaceResult.id() != null) {
            personRaceResultDbo.setId(personRaceResult.id().value());
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.startTime())) {
            personRaceResultDbo.setStartTime(personRaceResult.startTime().value());
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.finishTime())) {
            personRaceResultDbo.setFinishTime(personRaceResult.finishTime().value());
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.runtime())) {
            personRaceResultDbo.setPunchTime(personRaceResult.runtime().value());
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.raceNumber())) {
            personRaceResultDbo.setRaceNumber(personRaceResult.raceNumber().value());
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.positon())) {
            personRaceResultDbo.setPosition(personRaceResult.positon().value());
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.state())) {
            personRaceResultDbo.setState(personRaceResult.state());
        }
        if (personRaceResult.splitTimes().isPresent() && ObjectUtils.isNotEmpty(personRaceResult.splitTimes().get())) {
            personRaceResultDbo.setSplitTimes(personRaceResult.splitTimes().get()
                    .value()
                    .stream()
                    .map(it -> SplitTimeDbo.from(it, personRaceResultDbo))
                    .toList());
        }

        return personRaceResultDbo;
    }

    public static Collection<PersonRaceResult> asPersonRaceResults(EventConfig eventConfig,
                                                                   List<PersonRaceResultDbo> personRaceResultDbos) {
        Map<PersonRaceResultId, List<SplitTime>> splitTimesByPersonRaceResultId;
        if (eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.SPLIT_TIMES)) {
            splitTimesByPersonRaceResultId = new HashMap<>();
        } else {
            splitTimesByPersonRaceResultId =
                    SplitTimeDbo.asSplitTimes(personRaceResultDbos.stream()
                                    .flatMap(x -> x.splitTimes.stream())
                                    .toList())
                            .stream()
                            .collect(Collectors.groupingBy(SplitTime::personRaceResultId));
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
                                Optional.ofNullable(splitTimesByPersonRaceResultId.getOrDefault(
                                        PersonRaceResultId.of(it.id),
                                        new ArrayList<>()))))
                .toList();
    }

    public PersonRaceResult asPersonRaceResult(EventConfig eventConfig) {
        return PersonRaceResult.of(raceNumber, startTime, finishTime, punchTime, position, state,
                eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.SPLIT_TIMES) ? Optional.empty() :
                        Optional.of(
                                splitTimes.stream().map(SplitTimeDbo::asSplitTime).toList()));
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

    public List<SplitTimeDbo> getSplitTimes() {
        return splitTimes;
    }

    public void setSplitTimes(List<SplitTimeDbo> splitTimes) {
        this.splitTimes = splitTimes;
    }
}