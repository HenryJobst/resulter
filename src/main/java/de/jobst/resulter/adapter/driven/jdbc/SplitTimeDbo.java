package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.SplitTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.util.List;

import static de.jobst.resulter.domain.util.CompareUtils.compareNullable;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "split_time")
public class SplitTimeDbo implements Comparable<SplitTimeDbo> {

    @Column("control_code")
    private String controlCode;

    @Column("punch_time")
    private Double punchTime;

    public static SplitTimeDbo from(SplitTime splitTime) {
        return new SplitTimeDbo(splitTime.getControlCode().value(), splitTime.getPunchTime().value());
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
