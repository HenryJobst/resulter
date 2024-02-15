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
    private final List<SplitTime> splitTimes;

    public SplitTimeList(@NonNull SplitTimeListId id,
                         @NonNull EventId eventId,
                         @NonNull ResultListId resultListId,
                         @NonNull ClassResultShortName classResultShortName,
                         @NonNull PersonId personId,
                         @NonNull List<SplitTime> splitTimes) {
        this.id = id;
        this.eventId = eventId;
        this.resultListId = resultListId;
        this.classResultShortName = classResultShortName;
        this.personId = personId;
        this.splitTimes = splitTimes;
    }

    public DomainKey getDomainKey() {
        return new DomainKey(eventId, resultListId, classResultShortName, personId);
    }

    public record DomainKey(EventId eventId, ResultListId resultListId, ClassResultShortName classResultShortName,
                            PersonId personId) {}
}
