package de.jobst.resulter.adapter.driven.jdbc;

import lombok.Getter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "cup_event")
@Getter
public class CupEventDbo {

    @Column("cup_id")
    AggregateReference<CupDbo, Long> cupId;

    @Column("event_id")
    AggregateReference<EventDbo, Long> id;

    CupEventDbo(Long id) {
        this.id = AggregateReference.to(id);
    }
}
