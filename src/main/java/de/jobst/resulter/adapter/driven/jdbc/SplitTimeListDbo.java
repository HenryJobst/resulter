package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.SplitTimeList;
import de.jobst.resulter.domain.SplitTimeListId;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "SPLIT_TIME_LIST")
public class SplitTimeListDbo {

    @Id
    @With
    private Long id;

    @Column("EVENT_ID")
    private AggregateReference<EventDbo, Long> event;

    @Column("RESULT_LIST_ID")
    private AggregateReference<ResultListDbo, Long> resultList;

    @MappedCollection(idColumn = "SPLIT_TIME_LIST_ID")
    private Set<ClassResultDbo> classResults = new HashSet<>();

    public SplitTimeListDbo(AggregateReference<EventDbo, Long> event,
                            AggregateReference<ResultListDbo, Long> resultList) {
        this.id = null;
        this.event = event;
        this.resultList = resultList;
    }

    public static SplitTimeListDbo from(SplitTimeList splitTimeList, @NonNull DboResolvers dboResolvers) {
        SplitTimeListDbo splitTimeListDbo;
        if (splitTimeList.getId().isPersistent()) {
            splitTimeListDbo = dboResolvers.getSplitTimeListDboResolver().findDboById(splitTimeList.getId());
            splitTimeListDbo.setEvent(AggregateReference.to(splitTimeList.getEventId().value()));
            splitTimeListDbo.setResultList(AggregateReference.to(splitTimeList.getResultListId().value()));
        } else {
            splitTimeListDbo = new SplitTimeListDbo(AggregateReference.to(splitTimeList.getEventId().value()),
                AggregateReference.to(splitTimeList.getResultListId().value()));

        }
        return splitTimeListDbo;
    }

    static public Collection<SplitTimeList> asSplitTimeLists(Collection<SplitTimeListDbo> splitTimeListDbos) {
        return splitTimeListDbos.stream()
            .map(it -> new SplitTimeList(SplitTimeListId.of(it.id),
                EventId.of(it.event.getId()),
                ResultListId.of(it.resultList.getId())))
            .toList();
    }
}
