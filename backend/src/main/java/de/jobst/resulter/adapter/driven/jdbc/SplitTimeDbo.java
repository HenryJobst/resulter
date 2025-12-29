package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.SplitTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import static de.jobst.resulter.domain.util.CompareUtils.compareNullable;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ =@PersistenceCreator)
@Table(name = "split_time")
public class SplitTimeDbo implements Comparable<SplitTimeDbo> {

    @Column("split_time_list_id")
    private Long splitTimeListId;

    @Nullable
    @Column("control_code")
    private String controlCode;

    @Nullable
    @Column("punch_time")
    private Double punchTime;

    public static SplitTimeDbo from(SplitTime splitTime) {
        return new SplitTimeDbo(
            splitTime.splitTimeListId().value(),
            splitTime.controlCode().value(),
            splitTime.punchTime().value());
    }

    @Override
    public int compareTo(SplitTimeDbo o) {
        // Vergleich von punchTime unter Berücksichtigung von Null-Werten
        int value = compareNullable(punchTime, o.punchTime);

        // Wenn punchTime gleich ist, weiter mit controlCode vergleichen
        if (value == 0) {
            value = compareNullable(controlCode, o.controlCode);
        }
        return value;
    }
}
