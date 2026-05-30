package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

    // -------------------------------------------------------------------------
    // compareTo — Sortierung: startTime → name
    // -------------------------------------------------------------------------

    @Test
    void compareTo_bothHaveNullStartTimeValue_ordersByName() {
        // Event.of("name") setzt startTime = DateTime.of(null) — beide identisch → nach Name sortieren
        Event alpha = Event.of("Alpha Event");
        Event beta = Event.of("Beta Event");

        assertThat(alpha.compareTo(beta)).isLessThan(0);
        assertThat(beta.compareTo(alpha)).isGreaterThan(0);
        assertThat(alpha.compareTo(alpha)).isEqualTo(0);
    }

    @Test
    void compareTo_bothHaveActualStartTime_earlierEventComesFirst() {
        ZonedDateTime earlier = ZonedDateTime.now().minusDays(1);
        ZonedDateTime later = ZonedDateTime.now();

        Event earlyEvent = Event.of(1L, "Event", earlier, null, List.of(), null, Discipline.getDefault(), false);
        Event lateEvent = Event.of(2L, "Event", later, null, List.of(), null, Discipline.getDefault(), false);

        assertThat(earlyEvent.compareTo(lateEvent)).isLessThan(0);
        assertThat(lateEvent.compareTo(earlyEvent)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameActualStartTime_ordersByName() {
        ZonedDateTime now = ZonedDateTime.now();

        Event alpha = Event.of(1L, "Alpha", now, null, List.of(), null, Discipline.getDefault(), false);
        Event beta = Event.of(2L, "Beta", now, null, List.of(), null, Discipline.getDefault(), false);

        assertThat(alpha.compareTo(beta)).isLessThan(0);
        assertThat(beta.compareTo(alpha)).isGreaterThan(0);
    }

    // -------------------------------------------------------------------------
    // withCertificate — Resolver wird aufgerufen, Ergebnis wird gesetzt
    // -------------------------------------------------------------------------

    @Test
    void withCertificate_resolverReturnsCertificate_setsCertificateId() {
        Event event = Event.of(1L, "Test Event");
        EventCertificate cert = EventCertificate.of(5L, "Urkunde", null, null, null, true);

        event.withCertificate(eventId -> cert);

        assertThat(event.getCertificate()).isEqualTo(EventCertificateId.of(5L));
    }

    @Test
    void withCertificate_resolverReturnsNull_certificateRemainsNull() {
        Event event = Event.of(1L, "Test Event");

        event.withCertificate(eventId -> null);

        assertThat(event.getCertificate()).isNull();
    }

    // -------------------------------------------------------------------------
    // update — Felder werden aktualisiert
    // -------------------------------------------------------------------------

    @Test
    void update_replacesAllMutableFields() {
        Event event = Event.of("Original Name");
        ZonedDateTime newStartTime = ZonedDateTime.now();

        event.update(
            EventName.of("Updated Name"),
            DateTime.of(newStartTime),
            EventStatus.PLANNED,
            List.of(OrganisationId.of(1L)),
            null,
            Discipline.SPRINT,
            true);

        assertThat(event.getName().value()).isEqualTo("Updated Name");
        assertThat(event.getStartTime().value()).isEqualTo(newStartTime);
        assertThat(event.getEventState()).isEqualTo(EventStatus.PLANNED);
        assertThat(event.getOrganisationIds()).containsExactly(OrganisationId.of(1L));
        assertThat(event.getDiscipline()).isEqualTo(Discipline.SPRINT);
        assertThat(event.isAggregatedScore()).isTrue();
    }

    // -------------------------------------------------------------------------
    // of() — Fabrikmethoden
    // -------------------------------------------------------------------------

    @Test
    void of_stringOnly_setsDefaultsAndName() {
        Event event = Event.of("Mein Event");

        assertThat(event.getName().value()).isEqualTo("Mein Event");
        assertThat(event.getId()).isEqualTo(EventId.empty());
        assertThat(event.isAggregatedScore()).isFalse();
        assertThat(event.getEventState()).isEqualTo(EventStatus.getDefault());
    }

    @Test
    void of_stringAndOrganisations_setsOrganisations() {
        List<OrganisationId> orgs = List.of(OrganisationId.of(1L), OrganisationId.of(2L));
        Event event = Event.of("Teamlauf", orgs);

        assertThat(event.getName().value()).isEqualTo("Teamlauf");
        assertThat(event.getOrganisationIds()).containsExactlyInAnyOrderElementsOf(orgs);
    }

    @Test
    void of_stringStartTimeOrganisations_setsFields() {
        ZonedDateTime start = ZonedDateTime.now();
        Event event = Event.of("Staffellauf", start, List.of());

        assertThat(event.getName().value()).isEqualTo("Staffellauf");
        assertThat(event.getStartTime().value()).isEqualTo(start);
    }

    // -------------------------------------------------------------------------
    // compareTo — Mischfälle: einer hat startTime, anderer nicht
    // -------------------------------------------------------------------------

    @Test
    void compareTo_nullStartTimeIsLessThanActualStartTime() {
        ZonedDateTime now = ZonedDateTime.now();
        // Event.of("Event") hat DateTime(null) als startTime
        Event withoutTime = Event.of("Event");
        Event withTime = Event.of(1L, "Event", now, null, List.of(), null, Discipline.getDefault(), false);

        // DateTime(null).compareTo(DateTime(now)) → -1 (null sorts before actual time)
        assertThat(withoutTime.compareTo(withTime)).isLessThan(0);
        assertThat(withTime.compareTo(withoutTime)).isGreaterThan(0);
    }

    // -------------------------------------------------------------------------
    // of() — Null-ID-Branch
    // -------------------------------------------------------------------------

    @Test
    void of_withNullId_usesEmptyEventId() {
        Event event = Event.of(null, "Test Event");

        assertThat(event.getId()).isEqualTo(EventId.empty());
        assertThat(event.getName().value()).isEqualTo("Test Event");
    }

    @Test
    void of_nineParam_withNullId_usesEmptyEventId() {
        Event event = Event.of(
                null, "Test", null, null, List.of(), null,
                EventCertificateId.of(1L), Discipline.getDefault(), false);

        assertThat(event.getId()).isEqualTo(EventId.empty());
        assertThat(event.getCertificate()).isEqualTo(EventCertificateId.of(1L));
    }
}
