package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

@Getter
public class SplitTimeList {

    @NonNull
    @Setter
    private SplitTimeListId id;
    @NonNull
    private final EventId eventId;
    @NonNull
    private final ResultListId resultListId;

    public SplitTimeList(@NonNull SplitTimeListId id, @NonNull EventId eventId, @NonNull ResultListId resultListId) {
        this.id = id;
        this.eventId = eventId;
        this.resultListId = resultListId;
    }
}
