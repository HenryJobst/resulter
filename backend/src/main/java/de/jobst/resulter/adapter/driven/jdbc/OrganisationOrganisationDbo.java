package de.jobst.resulter.adapter.driven.jdbc;

import lombok.Getter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "organisation_organisation")
@Getter
public class OrganisationOrganisationDbo {

    @Column("parent_organisation_id")
    AggregateReference<OrganisationDbo, Long> parentOrganisationId;

    @Column("organisation_id")
    AggregateReference<OrganisationDbo, Long> id;

    OrganisationOrganisationDbo(Long id) {
        this.id = AggregateReference.to(id);
    }
}
