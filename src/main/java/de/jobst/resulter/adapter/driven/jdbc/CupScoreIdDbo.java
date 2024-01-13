package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.CupScoreId;
import de.jobst.resulter.domain.CupType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class CupScoreIdDbo implements Serializable, Comparable<CupScoreIdDbo> {

    private CupType type;
    private Long personRaceResultDbo;

    public static CupScoreIdDbo empty() {
        return new CupScoreIdDbo(null, 0L);
    }

    public static CupScoreIdDbo from(CupScoreId id) {
        return new CupScoreIdDbo(id.type(), id.value().value());
    }

    @Override
    public int compareTo(@NonNull CupScoreIdDbo o) {
        int val = type.compareTo(o.type);
        if (val == 0) {
            val = Long.compare(personRaceResultDbo, o.personRaceResultDbo);
        }
        return val;
    }
}
