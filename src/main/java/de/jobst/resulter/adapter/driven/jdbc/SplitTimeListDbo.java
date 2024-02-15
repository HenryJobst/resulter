package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.*;
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
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "split_time_list")
public class SplitTimeListDbo {

    @Id
    @With
    @Column("id")
    private Long id;

    @Column("event_id")
    private AggregateReference<EventDbo, Long> eventId;

    @Column("result_list_id")
    private AggregateReference<ResultListDbo, Long> resultListId;

    @Column("person_id")
    private AggregateReference<PersonDbo, Long> personId;

    @Column("class_result_short_name")
    private String classResultShortName;

    @MappedCollection(idColumn = "split_time_list_id")
    private Set<SplitTimeDbo> splitTimes;

    public SplitTimeListDbo(AggregateReference<EventDbo, Long> eventId,
                            AggregateReference<ResultListDbo, Long> resultListId,
                            String classResultShortName,
                            AggregateReference<PersonDbo, Long> personId,
                            Set<SplitTimeDbo> splitTimes) {
        this.id = null;
        this.eventId = eventId;
        this.resultListId = resultListId;
        this.personId = personId;
        this.classResultShortName = classResultShortName;
        this.splitTimes = splitTimes;
    }

    public static SplitTimeListDbo from(SplitTimeList splitTimeList, @NonNull DboResolvers dboResolvers) {
        SplitTimeListDbo splitTimeListDbo;
        if (splitTimeList.getId().isPersistent()) {
            splitTimeListDbo = dboResolvers.getSplitTimeListDboResolver().findDboById(splitTimeList.getId());
            splitTimeListDbo.setEventId(AggregateReference.to(splitTimeList.getEventId().value()));
            splitTimeListDbo.setResultListId(AggregateReference.to(splitTimeList.getResultListId().value()));
            splitTimeListDbo.setClassResultShortName(splitTimeList.getClassResultShortName().value());
            splitTimeListDbo.setPersonId(AggregateReference.to(splitTimeList.getPersonId().value()));
            splitTimeListDbo.setSplitTimes(splitTimeList.getSplitTimes()
                .stream()
                .map(SplitTimeDbo::from)
                .collect(Collectors.toSet()));
        } else {
            splitTimeListDbo = new SplitTimeListDbo(AggregateReference.to(splitTimeList.getEventId().value()),
                AggregateReference.to(splitTimeList.getResultListId().value()),
                splitTimeList.getClassResultShortName().value(),
                AggregateReference.to(splitTimeList.getPersonId().value()),
                splitTimeList.getSplitTimes().stream().map(SplitTimeDbo::from).collect(Collectors.toSet()));
        }
        return splitTimeListDbo;
    }

    static public Collection<SplitTimeList> asSplitTimeLists(Collection<SplitTimeListDbo> splitTimeListDbos) {
        return splitTimeListDbos.stream()
            .map(it -> new SplitTimeList(SplitTimeListId.of(it.id),
                EventId.of(it.eventId.getId()),
                ResultListId.of(it.resultListId.getId()),
                ClassResultShortName.of(it.classResultShortName),
                PersonId.of(it.personId.getId()),
                it.getSplitTimes()
                    .stream()
                    .map(x -> SplitTime.of(x.getControlCode(), x.getPunchTime(), SplitTimeListId.of(it.getId())))
                    .toList()))
            .toList();
    }
}
