package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "EVENT_RESULT_LIST")
public class EventResultListDbo {

    @Column("RESULT_LIST_ID")
    AggregateReference<ResultListDbo, Long> id;

    EventResultListDbo(Long id) {
        this.id = AggregateReference.to(id);
    }
}

