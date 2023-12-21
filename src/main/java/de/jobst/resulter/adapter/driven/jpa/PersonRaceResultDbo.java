package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.*;
import jakarta.persistence.*;
import org.apache.commons.lang3.ObjectUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
        if (personRaceResult.getId().value() != PersonRaceResultId.empty().value()) {
            personRaceResultDbo.setId(personRaceResult.getId().value());
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
        if (personRaceResult.getSplitTimes().isLoaded() &&
                ObjectUtils.isNotEmpty(personRaceResult.getSplitTimes().get())) {
            personRaceResultDbo.setSplitTimes(personRaceResult.getSplitTimes().get()
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
        if (!eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.SPLIT_TIMES)) {
            splitTimesByPersonRaceResultId =
                    SplitTimeDbo.asSplitTimes(personRaceResultDbos.stream()
                                    .flatMap(x -> x.splitTimes.stream())
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

    public List<SplitTimeDbo> getSplitTimes() {
        return splitTimes;
    }

    public void setSplitTimes(List<SplitTimeDbo> splitTimes) {
        this.splitTimes = splitTimes;
    }
}