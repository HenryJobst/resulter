package de.jobst.resulter.adapter.driver.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.domain.Discipline;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EventMapperTest {

    private OrganisationService organisationService;
    private EventCertificateService eventCertificateService;
    private EventMapper eventMapper;

    @BeforeEach
    void setUp() {
        organisationService = Mockito.mock(OrganisationService.class);
        eventCertificateService = Mockito.mock(EventCertificateService.class);
        eventMapper = new EventMapper(organisationService, eventCertificateService);
    }

    @Test
    void toDtos_shouldBatchLoadOrganisationsAndCertificates() {
        OrganisationId orgId = OrganisationId.of(10L);
        EventCertificateId certificateId = EventCertificateId.of(99L);

        Event event = Event.of(1L, "Event A", null, null, Set.of(orgId), null, certificateId, Discipline.LONG, false);
        Organisation organisation = Organisation.of(orgId.value(), "Org", "ORG");
        EventCertificate certificate = EventCertificate.of(certificateId.value(), "Cert", event.getId(), "{}", null, true);

        when(organisationService.findAllByIdAsMap(Set.of(orgId))).thenReturn(Map.of(orgId, organisation));
        when(eventCertificateService.findAllByIdAsMap(Set.of(certificateId))).thenReturn(Map.of(certificateId, certificate));

        List<?> dtos = eventMapper.toDtos(List.of(event), Map.of(event.getId().value(), true));

        assertThat(dtos).hasSize(1);
        verify(organisationService).findAllByIdAsMap(Set.of(orgId));
        verify(eventCertificateService).findAllByIdAsMap(Set.of(certificateId));
        verify(eventCertificateService, never()).getById(certificateId);
    }

    @Test
    void toDtos_shouldHandleEmptyInputWithoutServiceCalls() {
        List<?> dtos = eventMapper.toDtos(List.of(), Map.of());

        assertThat(dtos).isEmpty();
        verify(organisationService).findAllByIdAsMap(Set.of());
        verify(eventCertificateService).findAllByIdAsMap(Set.of());
    }
}
