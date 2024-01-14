package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "CUP_EVENT")
public class CupEventDbo {

    @Column("EVENT_ID")
    AggregateReference<EventDbo, Long> id;

    CupEventDbo(Long id) {
        this.id = AggregateReference.to(id);
    }
}
