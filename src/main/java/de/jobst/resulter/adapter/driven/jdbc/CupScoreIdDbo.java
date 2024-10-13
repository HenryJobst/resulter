package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.CupScoreId;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class CupScoreIdDbo implements Serializable, Comparable<CupScoreIdDbo> {

    private Long cupId;
    private Long personId;
    private String classResultShortName;

    public static CupScoreIdDbo empty() {
        return new CupScoreIdDbo(0L, 0L, "");
    }

    public static CupScoreIdDbo from(CupScoreId id) {
        return new CupScoreIdDbo(id.cupId().value(), id.personId().value(), id.classResultShortName().value());
    }

    @Override
    public int compareTo(@NonNull CupScoreIdDbo o) {
        int val = classResultShortName.compareTo(o.classResultShortName);
        if (val == 0) {
            val = personId.compareTo(o.personId);
        }
        if (val == 0) {
            val = cupId.compareTo(o.cupId);
        }
        return val;
    }
}
