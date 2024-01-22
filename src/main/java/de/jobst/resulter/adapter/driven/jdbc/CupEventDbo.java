package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "cup_event")
public class CupEventDbo {

    @Column("event_id")
    AggregateReference<EventDbo, Long> id;

    CupEventDbo(Long id) {
        this.id = AggregateReference.to(id);
    }
}
