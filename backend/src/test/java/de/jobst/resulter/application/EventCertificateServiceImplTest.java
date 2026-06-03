package de.jobst.resulter.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.jobst.resulter.application.port.EventCertificateRepository;
import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.application.port.MediaFileRepository;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.Discipline;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventCertificateLayoutDescription;
import de.jobst.resulter.domain.EventCertificateName;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.MediaFile;
import de.jobst.resulter.domain.MediaFileId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EventCertificateServiceImplTest {

    private EventCertificateRepository eventCertificateRepository;
    private EventRepository eventRepository;
    private MediaFileRepository mediaFileRepository;
    private EventCertificateServiceImpl service;

    @BeforeEach
    void setUp() {
        eventCertificateRepository = mock(EventCertificateRepository.class);
        eventRepository = mock(EventRepository.class);
        mediaFileRepository = mock(MediaFileRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        OrganisationRepository organisationRepository = mock(OrganisationRepository.class);

        service = new EventCertificateServiceImpl(
                eventCertificateRepository,
                personRepository,
                organisationRepository,
                eventRepository,
                mediaFileRepository);
    }

    @Test
    void updateEventCertificate_whenMarkedPrimary_setsOnlyOtherCertificatesToFalse() {
        EventId eventId = EventId.of(10L);
        EventCertificateId updatedCertificateId = EventCertificateId.of(1L);
        EventCertificate updatedCertificate = EventCertificate.of(1L, "Updated", eventId, "{}", MediaFileId.of(5L), false);
        EventCertificate otherPrimary = EventCertificate.of(2L, "Other Primary", eventId, "{}", MediaFileId.of(6L), true);
        EventCertificate otherSecondary = EventCertificate.of(3L, "Other Secondary", eventId, "{}", MediaFileId.of(7L), false);
        Event event = Event.of(10L, "Event 10");
        MediaFile mediaFile = MediaFile.of(5L, "cert.pdf", "thumb-cert.pdf", "application/pdf", 1024L);

        when(eventCertificateRepository.findById(updatedCertificateId)).thenReturn(Optional.of(updatedCertificate));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(mediaFileRepository.findById(MediaFileId.of(5L))).thenReturn(Optional.of(mediaFile));
        when(eventCertificateRepository.findAllByEvent(eventId))
                .thenReturn(List.of(updatedCertificate, otherPrimary, otherSecondary));
        when(eventCertificateRepository.save(any(EventCertificate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventCertificate result = service.updateEventCertificate(
                updatedCertificateId,
                EventCertificateName.of("Updated Name"),
                eventId,
                EventCertificateLayoutDescription.of("{\"paragraphs\":[]}"),
                MediaFileId.of(5L),
                true);

        assertThat(result.getId()).isEqualTo(updatedCertificateId);
        assertThat(result.isPrimary()).isTrue();
        assertThat(otherPrimary.isPrimary()).isFalse();
        assertThat(otherSecondary.isPrimary()).isFalse();
        assertThat(event.getCertificate()).isEqualTo(updatedCertificateId);

        ArgumentCaptor<List<EventCertificate>> certificatesCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventCertificateRepository).saveAll(certificatesCaptor.capture());
        assertThat(certificatesCaptor.getValue())
                .extracting(EventCertificate::getId)
                .doesNotContain(updatedCertificateId);
        verify(eventRepository).save(event);
    }

    @Test
    void updateEventCertificate_withNullEventAndNullMediaFile_savesWithoutLookups() {
        EventCertificateId certId = EventCertificateId.of(5L);
        EventCertificate cert = EventCertificate.of(5L, "Cert", null, null, null, false);
        when(eventCertificateRepository.findById(certId)).thenReturn(Optional.of(cert));
        when(eventCertificateRepository.save(any(EventCertificate.class))).thenAnswer(inv -> inv.getArgument(0));

        EventCertificate result = service.updateEventCertificate(
                certId,
                EventCertificateName.of("New Name"),
                null,    // null event
                null,
                null,    // null mediaFile
                false);

        assertThat(result).isNotNull();
        verify(eventCertificateRepository).save(cert);
    }

    @Test
    void updateEventCertificate_whenNotPrimaryButEventHasCert_clearsCertificateFromEvent() {
        EventCertificateId certId = EventCertificateId.of(7L);
        EventId eventId = EventId.of(20L);
        // Event currently holds this certId as its certificate
        Event event = Event.of(20L, "TestEvent", null, null, List.of(), null,
                certId, Discipline.getDefault(), false);
        EventCertificate cert = EventCertificate.of(7L, "Cert", eventId, null, null, false);

        when(eventCertificateRepository.findById(certId)).thenReturn(Optional.of(cert));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventCertificateRepository.save(any(EventCertificate.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateEventCertificate(
                certId,
                EventCertificateName.of("Updated"),
                eventId,
                null,
                null,
                false);  // not primary → else-if branch: clear event's certificate

        assertThat(event.getCertificate()).isNull();
        verify(eventRepository).save(event);
    }

    @Test
    void createEventCertificate_withNullEventAndNullMediaFile_savesNewCertificate() {
        EventCertificate saved = EventCertificate.of(99L, "New Cert", null, null, null, false);
        when(eventCertificateRepository.save(any(EventCertificate.class))).thenReturn(saved);

        EventCertificate result = service.createEventCertificate(
                "New Cert",
                null,   // null event
                null,
                null,   // null mediaFile
                false);

        assertThat(result).isNotNull();
        verify(eventCertificateRepository).save(any(EventCertificate.class));
    }
}
