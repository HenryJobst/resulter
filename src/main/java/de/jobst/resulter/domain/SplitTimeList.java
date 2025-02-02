package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.util.List;

@Getter
public class SplitTimeList {

    @NonNull
    @Setter
    private SplitTimeListId id;
    @NonNull
    private final EventId eventId;
    @NonNull
    private final ResultListId resultListId;
    @NonNull
    private final ClassResultShortName classResultShortName;
    @NonNull
    private final PersonId personId;
    @NonNull
    private final RaceNumber raceNumber;
    @NonNull
    private final List<SplitTime> splitTimes;

    public SplitTimeList(@NonNull SplitTimeListId id,
                         @NonNull EventId eventId,
                         @NonNull ResultListId resultListId,
                         @NonNull ClassResultShortName classResultShortName,
                         @NonNull PersonId personId,
                         @NonNull RaceNumber raceNumber,
                         @NonNull List<SplitTime> splitTimes) {
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

    public record DomainKey(EventId eventId, ResultListId resultListId, ClassResultShortName classResultShortName,
                            PersonId personId, RaceNumber raceNumber) implements Comparable<DomainKey> {

        @Override
        public int compareTo(@NonNull DomainKey o) {
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
