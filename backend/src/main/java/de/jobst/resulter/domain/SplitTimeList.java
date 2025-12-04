package de.jobst.resulter.domain;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Association;
import org.jmolecules.ddd.annotation.Identity;

@AggregateRoot
@Getter
public class SplitTimeList {

    @Identity
    @Setter
    private SplitTimeListId id;

    @Association
    private final EventId eventId;

    @Association
    private final ResultListId resultListId;

    private final ClassResultShortName classResultShortName;

    @Association
    private final PersonId personId;

    private final RaceNumber raceNumber;

    private final List<SplitTime> splitTimes;

    public SplitTimeList(
            SplitTimeListId id,
            EventId eventId,
            ResultListId resultListId,
            ClassResultShortName classResultShortName,
            PersonId personId,
            RaceNumber raceNumber,
            List<SplitTime> splitTimes) {
        this.id = id;
        this.eventId = eventId;
        this.resultListId = resultListId;
        this.classResultShortName = classResultShortName;
        this.personId = personId;
        this.raceNumber = raceNumber;
        this.splitTimes = splitTimes;
    }

    public DomainKey getDomainKey() {
        return new DomainKey(eventId, resultListId, classResultShortName, personId, raceNumber);
    }

    public record DomainKey(
            EventId eventId,
            ResultListId resultListId,
            ClassResultShortName classResultShortName,
            PersonId personId,
            RaceNumber raceNumber)
            implements Comparable<DomainKey> {

        @Override
        public int compareTo(DomainKey o) {
            int val = personId.compareTo(o.personId);
            if (val == 0) {
                val = classResultShortName.compareTo(o.classResultShortName);
                if (val == 0) {
                    val = raceNumber.compareTo(o.raceNumber);
                    if (val == 0) {
                        val = resultListId.compareTo(o.resultListId);
                        if (val == 0) {
                            val = eventId.compareTo(o.eventId);
                        }
                    }
                }
            }
            return val;
        }
    }
}
