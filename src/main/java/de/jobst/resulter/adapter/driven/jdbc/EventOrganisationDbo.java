package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "EVENT_ORGANISATION")
public class EventOrganisationDbo {

    @Column("ORGANISATION_ID")
    AggregateReference<OrganisationDbo, Long> id;

    EventOrganisationDbo(Long id) {
        this.id = AggregateReference.to(id);
    }
}

