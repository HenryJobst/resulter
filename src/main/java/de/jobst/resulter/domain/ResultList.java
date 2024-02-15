package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Objects;

@Getter
public class ResultList implements Comparable<ResultList> {

    @NonNull
    @Setter
    private ResultListId id;

    @NonNull
    private final EventId eventId;

    @NonNull
    private final RaceId raceId;

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
                      @NonNull RaceId raceId,
                      @Nullable String creator,
                      @Nullable ZonedDateTime createTime,
                      @Nullable String status,
                      @Nullable Collection<ClassResult> classResults) {
        this.id = id;
        this.eventId = eventId;
        this.raceId = raceId;
        this.creator = creator;
        this.createTime = createTime;
        this.status = status;
        this.classResults = classResults;
    }

    @Override
    public int compareTo(@NonNull ResultList o) {
        int var = Objects.compare(this.createTime, o.createTime, ZonedDateTime::compareTo);
        if (var == 0) {
            var = this.raceId.compareTo(o.raceId);
        }
        return var;
    }
}
