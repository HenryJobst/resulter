package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.ResultStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Getter
@Setter
@Entity
@Table(name = "PERSON_RACE_RESULT")
@NoArgsConstructor
public class PersonRaceResultDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator")
    @SequenceGenerator(name = "entity_generator", sequenceName = "SEQ_PERSON_RACE_RESULT_ID", allocationSize = 10)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PERSON_RESULT_ID", nullable = false)
    private PersonResultDbEntity personResultDbo;

    @Column(name = "RACE_NUMBER", nullable = false)
    private Long raceNumber;
    @Column(name = "START_TIME", nullable = false)
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


    public static PersonRaceResultDbo from(PersonRaceResult personRaceResult, PersonResultDbEntity personResultDbo) {
        PersonRaceResultDbo personRaceResultDbo = new PersonRaceResultDbo();
        personRaceResultDbo.setPersonResultDbo(personResultDbo);
        if (personRaceResult.id() != null) {
            personRaceResultDbo.setId(personRaceResult.id().value());
        }
        personRaceResultDbo.setStartTime(personRaceResult.startTime().value());
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

        return personRaceResultDbo;
    }

    public PersonRaceResult asPersonRaceResult() {
        return PersonRaceResult.of(raceNumber, startTime, finishTime, punchTime, position, state, new ArrayList<>());
    }
}