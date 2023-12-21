package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.PersonRaceResultId;
import de.jobst.resulter.domain.SplitTime;
import de.jobst.resulter.domain.SplitTimeId;
import jakarta.persistence.*;

import java.util.List;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed", "unused"})
@Entity
@Table(name = "SPLIT_TIME")
public class SplitTimeDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator_split_time")
    @SequenceGenerator(name = "entity_generator_split_time", sequenceName = "SEQ_SPLIT_TIME_ID", allocationSize = 10)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PERSON_RACE_RESULT_ID", nullable = false)
    private PersonRaceResultDbo personRaceResultDbo;
    @Column(name = "CONTROL_CODE", nullable = false)
    private String controlCode;
    @Column(name = "PUNCH_TIME")
    private Double punchTime;

    public static SplitTimeDbo from(SplitTime splitTime, PersonRaceResultDbo personRaceResultDbo) {
        SplitTimeDbo splitTimeDbo = new SplitTimeDbo();
        if (splitTime.getId().value() != SplitTimeId.empty().value()) {
            splitTimeDbo.setId(splitTime.getId().value());
        }
        splitTimeDbo.setPersonResultDbo(personRaceResultDbo);
        splitTimeDbo.setControlCode(splitTime.getControlCode().value());
        splitTimeDbo.setPunchTime(splitTime.getPunchTime().value());
        return splitTimeDbo;
    }

    static public List<SplitTime> asSplitTimes(List<SplitTimeDbo> splitTimeDbos) {
        return splitTimeDbos.stream().map(it ->
                SplitTime.of(it.id,
                        it.personRaceResultDbo != null ? it.personRaceResultDbo.getId() : PersonRaceResultId.empty()
                                .value(),
                        it.controlCode, it.punchTime)).toList();
    }

    public String getControlCode() {
        return controlCode;
    }

    public void setControlCode(String controlCode) {
        this.controlCode = controlCode;
    }

    public SplitTime asSplitTime() {
        return SplitTime.of(id,
                personRaceResultDbo != null ? personRaceResultDbo.getId() : PersonRaceResultId.empty().value(),
                controlCode,
                punchTime);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Double getPunchTime() {
        return punchTime;
    }

    public void setPunchTime(Double punchTime) {
        this.punchTime = punchTime;
    }

    public PersonRaceResultDbo getPersonResultDbo() {
        return personRaceResultDbo;
    }

    public void setPersonResultDbo(PersonRaceResultDbo personRaceResultDbo) {
        this.personRaceResultDbo = personRaceResultDbo;
    }
}