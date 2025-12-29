package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.RaceId;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ =@PersistenceCreator)
@Table(name = "result_list")
public class ResultListDbo {

    @Nullable
    @Id
    @With
    @Column("id")
    private Long id;

    @Column("event_id")
    private AggregateReference<EventDbo, Long> eventId;

    @Column("race_id")
    private AggregateReference<RaceDbo, Long> raceId;

    @Nullable
    @Column("creator")
    private String creator;

    @Nullable
    @Column("create_time")
    private Timestamp createTime;

    @Nullable
    @Column("create_time_zone")
    private String createTimeZone;

    @Column("status")
    private String status = "COMPLETE";

    @MappedCollection(idColumn = "result_list_id")
    private Set<ClassResultDbo> classResults = new HashSet<>();

    public ResultListDbo(AggregateReference<EventDbo, Long> eventId,
                         AggregateReference<RaceDbo, Long> raceId,
                         @Nullable String creator,
                         @Nullable Timestamp createTime,
                         @Nullable String createTimeZone) {
        this.id = null;
        this.eventId = eventId;
        this.raceId = raceId;
        this.creator = creator;
        this.createTime = createTime;
        this.createTimeZone = createTimeZone;
    }

    public static ResultListDbo from(ResultList resultList, DboResolvers dboResolvers) {
        ResultListDbo resultListDbo;
        if (resultList.getId().isPersistent()) {
            resultListDbo = dboResolvers.getResultListDboResolver().findDboById(resultList.getId());
            resultListDbo.setEventId(AggregateReference.to(resultList.getEventId().value()));
            resultListDbo.setRaceId(AggregateReference.to(resultList.getRaceId().value()));
            resultListDbo.setCreator(resultList.getCreator());
            resultListDbo.setCreateTime(null != resultList.getCreateTime() ?
                                        Timestamp.from(resultList.getCreateTime().toOffsetDateTime().toInstant()) :
                                        null);
            resultListDbo.setCreateTimeZone(
                null != resultList.getCreateTime() ? resultList.getCreateTime().getZone().getId() : null);
        } else {
            resultListDbo = new ResultListDbo(AggregateReference.to(resultList.getEventId().value()),
                AggregateReference.to(resultList.getRaceId().value()),
                resultList.getCreator(),
                null != resultList.getCreateTime() ?
                Timestamp.from(resultList.getCreateTime().toOffsetDateTime().toInstant()) :
                null,
                null != resultList.getCreateTime() ? resultList.getCreateTime().getZone().getId() : null);

        }
        if (resultList.getClassResults() != null && !resultList.getClassResults().isEmpty()) {
            resultListDbo.setClassResults(resultList.getClassResults()
                .stream()
                .map(ClassResultDbo::from)
                .collect(Collectors.toSet()));
        }
        return resultListDbo;
    }

    static public Collection<ResultList> asResultLists(Collection<ResultListDbo> resultListDbos) {
        return resultListDbos.stream()
            .map(it -> new ResultList(ResultListId.of(it.id),
                EventId.of(it.eventId.getId()),
                RaceId.of(it.raceId.getId()),
                it.creator,
                it.createTime != null ? it.createTime.toInstant().atZone(ZoneId.of(it.createTimeZone)) : null,
                it.status,
                ClassResultDbo.asClassResults(it.getClassResults())))
            .toList();
    }
}
