package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.CupId;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.PersonId;

public record DboResolvers(
        DboResolver<CupId, CupDbo> cupDboDboResolver,
        DboResolver<EventId, EventDbo> eventDboResolver,
        DboResolver<PersonId, PersonDbo> personDboResolver,
        DboResolver<OrganisationId, OrganisationDbo> organisationDboResolver
) {
}
