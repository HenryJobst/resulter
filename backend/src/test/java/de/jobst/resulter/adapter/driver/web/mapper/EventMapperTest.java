package de.jobst.resulter.adapter.driver.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import de.jobst.resulter.adapter.driver.web.dto.EventDto;
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

        List<EventDto> dtos = EventMapper.toDtos(
                List.of(event),
                Map.of(event.getId().value(), true),
                Map.of(orgId, organisation),
                Map.of(certificateId, certificate));

        assertThat(dtos).hasSize(1);
        EventDto dto = dtos.getFirst();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Event A");
        assertThat(dto.hasSplitTimes()).isTrue();
        assertThat(dto.organisations()).hasSize(1);
        assertThat(dto.organisations().getFirst().id()).isEqualTo(orgId.value());
        assertThat(dto.organisations().getFirst().name()).isEqualTo("Org");
        assertThat(dto.certificate()).isNotNull();
        assertThat(dto.certificate().id()).isEqualTo(certificateId.value());
        assertThat(dto.aggregateScore()).isFalse();
    }

    @Test
    void toDtos_shouldReturnEmptyListForEmptyInput() {
        List<EventDto> dtos = EventMapper.toDtos(List.of(), Map.of(), Map.of(), Map.of());

        assertThat(dtos).isEmpty();
    }

    @Test
    void toDtos_shouldDefaultHasSplitTimesToFalseWhenNotInMap() {
        Event event = Event.of(42L, "No SplitTimes", null, null, Set.of(), null, Discipline.getDefault(), false);

        List<EventDto> dtos = EventMapper.toDtos(List.of(event), Map.of(), Map.of(), Map.of());

        assertThat(dtos).hasSize(1);
        assertThat(dtos.getFirst().hasSplitTimes()).isFalse();
    }

    @Test
    void toDtos_shouldHandleMissingOrganisationInMapGracefully() {
        OrganisationId orgId = OrganisationId.of(5L);
        Event event = Event.of(2L, "Event B", null, null, Set.of(orgId), null, Discipline.getDefault(), false);

        List<EventDto> dtos = EventMapper.toDtos(
                List.of(event), Map.of(event.getId().value(), false), Map.of(), Map.of());

        assertThat(dtos).hasSize(1);
        assertThat(dtos.getFirst().organisations()).isEmpty();
    }
}
