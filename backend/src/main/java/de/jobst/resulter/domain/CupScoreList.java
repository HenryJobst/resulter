package de.jobst.resulter.domain;

import java.time.ZonedDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Association;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.Nullable;

@AggregateRoot
@Getter
public class CupScoreList {

    @Identity
    @Setter
    private CupScoreListId id;

    @Association
    private final CupId cupId;

    @Association
    private final ResultListId resultListId;

    private final List<CupScore> cupScores;

    @Nullable
    private final String creator;

    @Nullable
    private final ZonedDateTime createTime;

    private final String status = "COMPLETE";

    public CupScoreList(
            CupScoreListId id,
            CupId cupId,
            ResultListId resultListId,
            List<CupScore> cupScores,
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
