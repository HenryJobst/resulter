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
@Table(name = "SPLIT_TIME_LIST")
public class SplitTimeListDbo {

    @Id
    @With
    private Long id;

    @Column("EVENT_ID")
    private AggregateReference<EventDbo, Long> event;

    @Column("RESULT_LIST_ID")
    private AggregateReference<ResultListDbo, Long> resultList;

    @Column("PERSON_ID")
    private AggregateReference<PersonDbo, Long> person;

    private String classResultShortName;
    private Long raceNumber;

    @MappedCollection(idColumn = "SPLIT_TIME_LIST_ID")
    private Set<SplitTimeDbo> splitTimes;

    public SplitTimeListDbo(AggregateReference<EventDbo, Long> event,
                            AggregateReference<ResultListDbo, Long> resultList,
                            String classResultShortName,
                            AggregateReference<PersonDbo, Long> person,
                            Long raceNumber,
                            Set<SplitTimeDbo> splitTimes) {
        this.id = null;
        this.event = event;
        this.resultList = resultList;
        this.person = person;
        this.classResultShortName = classResultShortName;
        this.raceNumber = raceNumber;
        this.splitTimes = splitTimes;
    }

    public static SplitTimeListDbo from(SplitTimeList splitTimeList, @NonNull DboResolvers dboResolvers) {
        SplitTimeListDbo splitTimeListDbo;
        if (splitTimeList.getId().isPersistent()) {
            splitTimeListDbo = dboResolvers.getSplitTimeListDboResolver().findDboById(splitTimeList.getId());
            splitTimeListDbo.setEvent(AggregateReference.to(splitTimeList.getEventId().value()));
            splitTimeListDbo.setResultList(AggregateReference.to(splitTimeList.getResultListId().value()));
            splitTimeListDbo.setClassResultShortName(splitTimeList.getClassResultShortName().value());
            splitTimeListDbo.setPerson(AggregateReference.to(splitTimeList.getPersonId().value()));
            splitTimeListDbo.setRaceNumber(splitTimeList.getRaceNumber().value());
            splitTimeListDbo.setSplitTimes(splitTimeList.getSplitTimes()
                .stream()
                .map(SplitTimeDbo::from)
                .collect(Collectors.toSet()));
        } else {
            splitTimeListDbo = new SplitTimeListDbo(AggregateReference.to(splitTimeList.getEventId().value()),
                AggregateReference.to(splitTimeList.getResultListId().value()),
                splitTimeList.getClassResultShortName().value(),
                AggregateReference.to(splitTimeList.getPersonId().value()),
                splitTimeList.getRaceNumber().value(),
                splitTimeList.getSplitTimes().stream().map(SplitTimeDbo::from).collect(Collectors.toSet()));
        }
        return splitTimeListDbo;
    }

    static public Collection<SplitTimeList> asSplitTimeLists(Collection<SplitTimeListDbo> splitTimeListDbos) {
        return splitTimeListDbos.stream()
            .map(it -> new SplitTimeList(SplitTimeListId.of(it.id),
                EventId.of(it.event.getId()),
                ResultListId.of(it.resultList.getId()),
                ClassResultShortName.of(it.classResultShortName),
                PersonId.of(it.person.getId()),
                RaceNumber.of(it.raceNumber),
                it.getSplitTimes().stream().map(x -> SplitTime.of(x.getControlCode(), x.getPunchTime())).toList()))
            .toList();
    }
}
