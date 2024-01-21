package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.Collection;

@Getter
public class ResultList {

    @NonNull
    @Setter
    private ResultListId id;
    @NonNull
    private final EventId eventId;

    @Nullable
    private final String creator;

    @Nullable
    private final ZonedDateTime createTime;

    @Nullable
    private final String status;

    @Nullable
    @Setter
    private Collection<ClassResult> classResults;

    public ResultList(@NonNull ResultListId id,
                      @NonNull EventId eventId,
                      @Nullable String creator,
                      @Nullable ZonedDateTime createTime,
                      @Nullable String status,
                      @Nullable Collection<ClassResult> classResults) {
        this.id = id;
        this.eventId = eventId;
        this.creator = creator;
        this.createTime = createTime;
        this.status = status;
        this.classResults = classResults;
    }
}
