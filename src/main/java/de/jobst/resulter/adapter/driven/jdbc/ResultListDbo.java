package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.ResultList;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "RESULT_LIST")
public class ResultListDbo {

    @Id
    @With
    private Long id;

    @Column("EVENT_ID")
    private AggregateReference<EventDbo, Long> event;

    private String creator;

    private OffsetDateTime createTime;
    private String createTimeZone;

    private String status = "COMPLETE";

    @MappedCollection(idColumn = "RESULT_LIST_ID")
    private Set<ClassResultDbo> classResults = new HashSet<>();

    public ResultListDbo(AggregateReference<EventDbo, Long> event,
                         String creator,
                         OffsetDateTime createTime,
                         String createTimeZone) {
        this.id = null;
        this.event = event;
        this.creator = creator;
        this.createTime = createTime;
        this.createTimeZone = createTimeZone;
    }

    public static ResultListDbo from(ResultList resultList, @NonNull DboResolvers dboResolvers) {
        ResultListDbo resultListDbo;
        if (resultList.getId().isPersistent()) {
            resultListDbo = dboResolvers.getResultListDboResolver().findDboById(resultList.getId());
            resultListDbo.setEvent(AggregateReference.to(resultList.getEventId().value()));
            resultListDbo.setCreator(resultList.getCreator());
            resultListDbo.setCreateTime(
                resultList.getCreateTime() != null ? resultList.getCreateTime().toOffsetDateTime() : null);
            resultListDbo.setCreateTimeZone(
                resultList.getCreateTime() != null ? resultList.getCreateTime().getZone().getId() : null);
        } else {
            resultListDbo = new ResultListDbo(AggregateReference.to(resultList.getEventId().value()),
                resultList.getCreator(),
                resultList.getCreateTime() != null ? resultList.getCreateTime().toOffsetDateTime() : null,
                resultList.getCreateTime() != null ? resultList.getCreateTime().getZone().getId() : null);

        }
        if (resultList.getClassResults() != null) {
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
                EventId.of(it.event.getId()),
                it.creator,
                it.createTime != null ? it.createTime.atZoneSameInstant(ZoneId.of(it.createTimeZone)) : null,
                it.status,
                ClassResultDbo.asClassResults(it.getClassResults())))
            .toList();
    }
}
