package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "event_organisation")
public class EventOrganisationDbo {

    @Column("organisation_id")
    AggregateReference<OrganisationDbo, Long> id;

    EventOrganisationDbo(Long id) {
        this.id = AggregateReference.to(id);
    }
}

