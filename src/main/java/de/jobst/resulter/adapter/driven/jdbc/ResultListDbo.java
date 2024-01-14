package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "RESULT_LIST")
public class ResultListDbo {

    @Id
    private Long id;

    private AggregateReference<EventDbo, Long> event;

    private String creator;

    private ZonedDateTime createTime;

    private String status = "Complete";

    private Set<ClassResultDbo> classResults = new HashSet<>();

    public ResultListDbo(AggregateReference<EventDbo, Long> event, String creator, ZonedDateTime createTime) {
        this.id = null;
        this.event = event;
        this.creator = creator;
        this.createTime = createTime;
    }

    public static ResultListDbo from(ResultList resultList, @NonNull DboResolvers dboResolvers) {
        ResultListDbo resultListDbo;
        if (resultList.getId().isPersistent()) {
            resultListDbo = dboResolvers.getResultListDboResolver().findDboById(resultList.getId());
            resultListDbo.setEvent(AggregateReference.to(resultList.getEventId().value()));
            resultListDbo.setCreator(resultList.getCreator());
            resultListDbo.setCreateTime(resultList.getCreateTime());
        } else {
            resultListDbo = new ResultListDbo(AggregateReference.to(resultList.getEventId().value()),
                resultList.getCreator(),
                resultList.getCreateTime());
        }
        resultListDbo.setClassResults(resultList.getClassResults()
            .stream()
            .map(x -> ClassResultDbo.from(x, dboResolvers))
            .collect(Collectors.toSet()));
        return resultListDbo;
    }

    static public Collection<ResultList> asResultLists(Collection<ResultListDbo> resultListDbos) {
        return resultListDbos.stream()
            .map(it -> new ResultList(ResultListId.of(it.id),
                EventId.of(it.event.getId()),
                it.creator,
                it.createTime,
                it.status,
                ClassResultDbo.asClassResults(it.getClassResults())))
            .toList();
    }
}
