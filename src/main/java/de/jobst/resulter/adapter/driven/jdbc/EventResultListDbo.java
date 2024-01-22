package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "event_result_list")
public class EventResultListDbo {

    @Column("result_list_id")
    AggregateReference<ResultListDbo, Long> id;

    EventResultListDbo(Long id) {
        this.id = AggregateReference.to(id);
    }
}

