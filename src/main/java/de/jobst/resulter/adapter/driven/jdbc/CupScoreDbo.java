package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.CupScore;
import de.jobst.resulter.domain.CupScoreId;
import de.jobst.resulter.domain.CupType;
import jakarta.persistence.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed", "unused"})
@Entity
@Table(name = "CUP_SCORE")
@IdClass(CupScoreIdDbo.class)
public class CupScoreDbo implements Comparable<CupScoreDbo> {

    @Id
    @Column(name = "CUP_TYPE")
    @Enumerated(value = EnumType.STRING)
    private CupType type;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PERSON_RACE_RESULT_ID", nullable = false)
    private PersonRaceResultDbo personRaceResultDbo;

    @Column(name = "SCORE")
    private Double score;

    @Override
    public int compareTo(@NonNull CupScoreDbo o) {
        return 0;
    }

    public static CupScoreDbo from(CupScore cupScore,
                                   PersonRaceResultDbo personRaceResultDbo,
                                   @Nullable DboResolver<CupScoreIdDbo, CupScoreDbo> dboResolver,
                                   @NonNull DboResolvers dboResolvers) {
        CupScoreDbo cupScoreDbo = null;
        if (cupScore.id().isPersistent()) {
            if (dboResolver != null) {
                cupScoreDbo = dboResolver.findDboById(CupScoreIdDbo.from(cupScore.id()));
            }
            if (cupScoreDbo == null) {
                cupScoreDbo = dboResolvers.getCupScoreDboResolver().findDboById(CupScoreIdDbo.from(cupScore.id()));
            }
        } else {
            cupScoreDbo = new CupScoreDbo();
        }
        cupScoreDbo.setPersonRaceResultDbo(personRaceResultDbo);
        cupScoreDbo.setScore(cupScore.value());
        return cupScoreDbo;
    }

    static public List<CupScore> asCupScores(List<CupScoreDbo> cupScoreDbos) {
        return cupScoreDbos.stream()
            .map(it -> CupScore.of(CupScoreId.of(it.type, it.personRaceResultDbo.getId()), it.score))
            .toList();
    }

    public CupScore asCupScore() {
        return CupScore.of(CupScoreId.of(type, personRaceResultDbo.getId()), score);
    }

    public CupType getType() {
        return type;
    }

    public void setType(CupType type) {
        this.type = type;
    }

    public PersonRaceResultDbo getPersonRaceResultDbo() {
        return personRaceResultDbo;
    }

    public void setPersonRaceResultDbo(PersonRaceResultDbo personRaceResultDbo) {
        this.personRaceResultDbo = personRaceResultDbo;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
