package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.SplitTime;
import de.jobst.resulter.domain.SplitTimeId;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

import static de.jobst.resulter.domain.util.CompareUtils.compareNullable;

@Data
@Table(name = "SPLIT_TIME")
public class SplitTimeDbo implements Comparable<SplitTimeDbo> {

    private String controlCode;
    private Double punchTime;

    public static SplitTimeDbo from(SplitTime splitTime,
                                    PersonRaceResultDbo personRaceResultDbo,
                                    @Nullable DboResolver<SplitTimeId, SplitTimeDbo> dboResolver,
                                    @NonNull DboResolvers dboResolvers) {
        SplitTimeDbo splitTimeDbo = null;
        if (splitTime.getId().value() != SplitTimeId.empty().value()) {
            if (dboResolver != null) {
                splitTimeDbo = dboResolver.findDboById(splitTime.getId());
            }
            if (splitTimeDbo == null) {
                splitTimeDbo = dboResolvers.getSplitTimeDboResolver().findDboById(splitTime.getId());
            }
        } else {
            splitTimeDbo = new SplitTimeDbo();
        }
        splitTimeDbo.setControlCode(splitTime.getControlCode().value());
        splitTimeDbo.setPunchTime(splitTime.getPunchTime().value());
        return splitTimeDbo;
    }

    static public List<SplitTime> asSplitTimes(List<SplitTimeDbo> splitTimeDbos) {
        return splitTimeDbos.stream().map(it -> SplitTime.of(it.controlCode, it.punchTime)).toList();
    }

    @Override
    public int compareTo(@NonNull SplitTimeDbo o) {
        // Vergleich von punchTime unter Berücksichtigung von Null-Werten
        int value = compareNullable(punchTime, o.punchTime);

        // Wenn punchTime gleich ist, weiter mit controlCode vergleichen
        if (value == 0) {
            value = compareNullable(controlCode, o.controlCode);
        }
        return value;
    }
}
