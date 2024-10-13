package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.CupScore;
import de.jobst.resulter.domain.CupType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

@Data
@Table(name = "cup_score")
public class CupScoreDbo implements Comparable<CupScoreDbo> {

    @Id
    @Column("type")
    private CupType type;

    private PersonRaceResultDbo personRaceResultDbo;

    @Setter
    @Getter
    @Column("score")
    private Double score;

    @Override
    public int compareTo(@NonNull CupScoreDbo o) {
        return 0;
    }

    public static CupScoreDbo from(CupScore cupScore, @NonNull DboResolvers dboResolvers) {
        CupScoreDbo cupScoreDbo = new CupScoreDbo();
        cupScoreDbo.setScore(cupScore.value());
        return cupScoreDbo;
    }

}
