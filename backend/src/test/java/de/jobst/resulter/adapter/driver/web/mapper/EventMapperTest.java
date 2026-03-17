package de.jobst.resulter.adapter.driver.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import de.jobst.resulter.domain.Discipline;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EventMapperTest {

    @Test
    void toDtos_shouldMapEventsUsingProvidedMaps() {
        OrganisationId orgId = OrganisationId.of(10L);
        EventCertificateId certificateId = EventCertificateId.of(99L);

        Event event = Event.of(1L, "Event A", null, null, Set.of(orgId), null, certificateId, Discipline.LONG, false);
        Organisation organisation = Organisation.of(orgId.value(), "Org", "ORG");
        EventCertificate certificate = EventCertificate.of(certificateId.value(), "Cert", event.getId(), "{}", null, true);

        List<?> dtos = EventMapper.toDtos(
                List.of(event),
                Map.of(event.getId().value(), true),
                Map.of(orgId, organisation),
                Map.of(certificateId, certificate));

        assertThat(dtos).hasSize(1);
    }

    @Test
    void toDtos_shouldReturnEmptyListForEmptyInput() {
        List<?> dtos = EventMapper.toDtos(List.of(), Map.of(), Map.of(), Map.of());

        assertThat(dtos).isEmpty();
    }
}
