package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.PersonRaceResultId;
import de.jobst.resulter.domain.SplitTime;
import jakarta.persistence.*;

import java.util.List;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed", "unused"})
@Entity
@Table(name = "SPLIT_TIME")
public class SplitTimeDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator")
    @SequenceGenerator(name = "entity_generator", sequenceName = "SEQ_SPLIT_TIME_ID", allocationSize = 10)
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
        splitTimeDbo.setPersonResultDbo(personRaceResultDbo);
        if (splitTime.id() != null) {
            splitTimeDbo.setId(splitTime.id().value());
        }
        splitTimeDbo.setControlCode(splitTime.controlCode().value());
        splitTimeDbo.setPunchTime(splitTime.punchTime().value());
        return splitTimeDbo;
    }

    static public List<SplitTime> asSplitTimes(List<SplitTimeDbo> splitTimeDbos) {
        return splitTimeDbos.parallelStream().map(it ->
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