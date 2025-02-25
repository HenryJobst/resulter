package de.jobst.resulter.domain;

import java.time.ZonedDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@AggregateRoot
@Getter
public class CupScoreList {

    @Identity
    @NonNull
    @Setter
    private CupScoreListId id;

    @NonNull
    private final CupId cupId;

    @NonNull
    private final ResultListId resultListId;

    @NonNull
    private final List<CupScore> cupScores;

    @Nullable
    private final String creator;

    @Nullable
    private final ZonedDateTime createTime;

    @NonNull
    private final String status = "COMPLETE";

    public CupScoreList(
            @NonNull CupScoreListId id,
            @NonNull CupId cupId,
            @NonNull ResultListId resultListId,
            @NonNull List<CupScore> cupScores,
            @Nullable String creator,
            @Nullable ZonedDateTime createTime) {
        this.id = id;
        this.cupId = cupId;
        this.resultListId = resultListId;
        this.cupScores = cupScores;
        this.creator = creator;
        this.createTime = createTime;
    }

    public DomainKey getDomainKey() {
        return new DomainKey(cupId, resultListId, status);
    }

    public record DomainKey(CupId cupId, ResultListId resultListId, String status) {}
}
