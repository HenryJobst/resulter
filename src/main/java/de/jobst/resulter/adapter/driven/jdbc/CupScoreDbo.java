package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.CupScore;
import de.jobst.resulter.domain.CupType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Data
@Table(name = "CUP_SCORE")
public class CupScoreDbo implements Comparable<CupScoreDbo> {

    @Id
    private CupType type;

    private PersonRaceResultDbo personRaceResultDbo;

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
