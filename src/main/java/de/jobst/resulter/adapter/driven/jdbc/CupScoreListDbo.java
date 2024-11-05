package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.CupScoreList;
import de.jobst.resulter.domain.CupScoreListId;
import de.jobst.resulter.domain.ResultListId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "cup_score_list")
public class CupScoreListDbo {

    @Id
    @With
    @Column("id")
    private Long id;

    @Column("cup_id")
    private AggregateReference<CupDbo, Long> cupId;

    @Column("result_list_id")
    private AggregateReference<ResultListDbo, Long> resultListId;

    @Column("creator")
    private String creator;

    @Column("create_time")
    private OffsetDateTime createTime;

    @Column("create_time_zone")
    private String createTimeZone;

    @Column("status")
    private String status = "COMPLETE";

    @MappedCollection(idColumn = "cup_score_list_id")
    private Set<CupScoreDbo> cupScores = new HashSet<>();

    public CupScoreListDbo(AggregateReference<CupDbo, Long> cupId,
                           AggregateReference<ResultListDbo, Long> resultListId,
                           String creator,
                           OffsetDateTime createTime,
                           String createTimeZone) {
        this.id = null;
        this.cupId = cupId;
        this.resultListId = resultListId;
        this.creator = creator;
        this.createTime = createTime;
        this.createTimeZone = createTimeZone;
    }

    public static List<CupScoreListDbo> from(List<CupScoreList> cupScoreLists, @NonNull DboResolvers dboResolvers) {
        return cupScoreLists.stream().map(x -> CupScoreListDbo.from(x, dboResolvers)).toList();
    }

    public static CupScoreListDbo from(CupScoreList cupScoreList, @NonNull DboResolvers dboResolvers) {
        CupScoreListDbo cupScoreListDbo;
        if (cupScoreList.getId().isPersistent()) {
            cupScoreListDbo = dboResolvers.getCupScoreListDboResolver().findDboById(cupScoreList.getId());
            cupScoreListDbo.setCupId(AggregateReference.to(cupScoreList.getCupId().value()));
            cupScoreListDbo.setResultListId(AggregateReference.to(cupScoreList.getResultListId().value()));
            cupScoreListDbo.setCreator(cupScoreList.getCreator());
            cupScoreListDbo.setCreateTime(
                null != cupScoreList.getCreateTime() ? cupScoreList.getCreateTime().toOffsetDateTime() : null);
            cupScoreListDbo.setCreateTimeZone(
                null != cupScoreList.getCreateTime() ? cupScoreList.getCreateTime().getZone().getId() : null);
        } else {
            cupScoreListDbo = new CupScoreListDbo(AggregateReference.to(cupScoreList.getCupId().value()),
                AggregateReference.to(cupScoreList.getResultListId().value()),
                cupScoreList.getCreator(),
                null != cupScoreList.getCreateTime() ? cupScoreList.getCreateTime().toOffsetDateTime() : null,
                null != cupScoreList.getCreateTime() ? cupScoreList.getCreateTime().getZone().getId() : null);
        }
        if (!cupScoreList.getCupScores().isEmpty()) {
            cupScoreListDbo.setCupScores(cupScoreList.getCupScores()
                .stream()
                .map(CupScoreDbo::from)
                .collect(Collectors.toSet()));
        }
        return cupScoreListDbo;
    }

    public static List<CupScoreList> asCupScoreLists(Iterable<CupScoreListDbo> savedCupScoreListDbos) {
        return StreamSupport.stream(savedCupScoreListDbos.spliterator(), true)
            .map(it -> new CupScoreList(CupScoreListId.of(it.getId()),
                CupId.of(it.getCupId().getId()),
                ResultListId.of(it.getResultListId().getId()),
                CupScoreDbo.asCupScores(it.getCupScores()),
                it.getCreator(),
                it.getCreateTime() != null ?
                it.getCreateTime().atZoneSameInstant(ZoneId.of(it.getCreateTimeZone())) :
                null))
            .toList();
    }
}
