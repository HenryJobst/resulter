package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.aggregations.CupDetailed;
import java.util.Map;

public record CupDetailedBatchResult(
        CupDetailed cupDetailed,
        Map<EventId, Event> eventMap,
        Map<EventId, Boolean> hasSplitTimesMap,
        Map<OrganisationId, Organisation> organisationMap,
        Map<EventCertificateId, EventCertificate> certificateMap,
        Map<CountryId, Country> countryMap,
        Map<OrganisationId, Organisation> childOrganisationMap) {}
