package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.EventConfig;
import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.ResultStatus;
import jakarta.persistence.*;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed", "unused"})
@Entity
@Table(name = "PERSON_RACE_RESULT")
public class PersonRaceResultDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator")
    @SequenceGenerator(name = "entity_generator", sequenceName = "SEQ_PERSON_RACE_RESULT_ID", allocationSize = 10)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PERSON_RESULT_ID", nullable = false)
    private PersonResultDbo personResultDbo;

    @Column(name = "RACE_NUMBER", nullable = false)
    private Long raceNumber;
    @Column(name = "START_TIME")
    private LocalDateTime startTime;
    @Column(name = "FINISH_TIME")
    private LocalDateTime finishTime;
    @Column(name = "PUNCH_TIME")
    private Double punchTime;
    @Column(name = "POSITION")
    private Long position;
    @Column(name = "STATE")
    @Enumerated(value = EnumType.STRING)
    private ResultStatus state;

    @OneToMany(mappedBy = "personRaceResultDbo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SplitTimeDbo> spitTimes = new ArrayList<>();

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

    public PersonRaceResult asPersonRaceResult(EventConfig eventConfig) {
        return PersonRaceResult.of(raceNumber, startTime, finishTime, punchTime, position, state,
                eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.SPLIT_TIMES) ? Optional.empty() :
                        Optional.of(
                                spitTimes.stream().map(SplitTimeDbo::asSplitTime).toList()));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
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
        return spitTimes;
    }

    public void setSplitTimes(List<SplitTimeDbo> splitTimes) {
        this.spitTimes = splitTimes;
    }
}