package de.jobst.resulter.adapter.driven.jdbc;

import lombok.Data;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Table(value = "organisation_organisation")
public class OrganisationOrganisationDbo {

    @Column("organisation_id")
    AggregateReference<OrganisationDbo, Long> id;

    @Column("parent_organisation_id")
    AggregateReference<OrganisationDbo, Long> parentId;

    OrganisationOrganisationDbo(Long id, Long parentId) {
        this.id = AggregateReference.to(id);
        this.parentId = AggregateReference.to(parentId);
    }
}
