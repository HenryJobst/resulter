package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.analysis.PerformanceIndex;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PortRecordTest {

    // -------------------------------------------------------------------------
    // ClassGroupOption
    // -------------------------------------------------------------------------

    @Test
    void classGroupOption_accessorsReturnCorrectValues() {
        ClassGroupOption opt = new ClassGroupOption("H21", 42);
        assertThat(opt.className()).isEqualTo("H21");
        assertThat(opt.runnerCount()).isEqualTo(42);
    }

    @Test
    void classGroupOption_equalsAndHashCode() {
        ClassGroupOption a = new ClassGroupOption("D18", 10);
        ClassGroupOption b = new ClassGroupOption("D18", 10);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    // -------------------------------------------------------------------------
    // CourseGroupOption
    // -------------------------------------------------------------------------

    @Test
    void courseGroupOption_accessorsReturnCorrectValues() {
        CourseGroupOption opt = new CourseGroupOption(5L, "Kurz", List.of("H10", "D10"), 15);
        assertThat(opt.courseId()).isEqualTo(5L);
        assertThat(opt.courseName()).isEqualTo("Kurz");
        assertThat(opt.classNames()).containsExactly("H10", "D10");
        assertThat(opt.runnerCount()).isEqualTo(15);
    }

    @Test
    void courseGroupOption_compactConstructorCopiesList() {
        List<String> mutable = new java.util.ArrayList<>(List.of("H10"));
        CourseGroupOption opt = new CourseGroupOption(1L, "Lang", mutable, 5);
        mutable.add("D10");
        assertThat(opt.classNames()).containsExactly("H10");
    }

    // -------------------------------------------------------------------------
    // CupBatchResult
    // -------------------------------------------------------------------------

    @Test
    void cupBatchResult_accessorsReturnCorrectValues() {
        Cup cup = Cup.of(1L, "TestCup", CupType.KJ, Year.of(2025), List.of());
        Event event = Event.of("TestEvent");
        EventId eventId = EventId.of(99L);
        CupBatchResult result = new CupBatchResult(
                List.of(cup), 1L, Pageable.unpaged(), Map.of(eventId, event));

        assertThat(result.cups()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.resolvedPageable()).isEqualTo(Pageable.unpaged());
        assertThat(result.eventMap()).containsKey(eventId);
    }

    // -------------------------------------------------------------------------
    // SegmentPI
    // -------------------------------------------------------------------------

    @Test
    void segmentPI_accessorsReturnCorrectValues() {
        PerformanceIndex pi = new PerformanceIndex(1.15);
        SegmentPI seg = new SegmentPI(2, "K1", "K2", 120.0, 100.0, pi);

        assertThat(seg.legNumber()).isEqualTo(2);
        assertThat(seg.fromControl()).isEqualTo("K1");
        assertThat(seg.toControl()).isEqualTo("K2");
        assertThat(seg.runnerTime()).isEqualTo(120.0);
        assertThat(seg.referenceTime()).isEqualTo(100.0);
        assertThat(seg.pi()).isEqualTo(pi);
    }

    @Test
    void segmentPI_equalsAndHashCode() {
        PerformanceIndex pi = new PerformanceIndex(1.0);
        SegmentPI a = new SegmentPI(1, "S", "F", 60.0, 60.0, pi);
        SegmentPI b = new SegmentPI(1, "S", "F", 60.0, 60.0, pi);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    // -------------------------------------------------------------------------
    // EventCertificateStatBatchResult
    // -------------------------------------------------------------------------

    @Test
    void eventCertificateStatBatchResult_accessorsReturnCorrectValues() {
        EventCertificateStatBatchResult result = new EventCertificateStatBatchResult(
                List.of(), Map.of(), Map.of());

        assertThat(result.eventCertificateStats()).isEmpty();
        assertThat(result.eventMap()).isEmpty();
        assertThat(result.personMap()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // EventCertificateBatchResult
    // -------------------------------------------------------------------------

    @Test
    void eventCertificateBatchResult_accessorsReturnCorrectValues() {
        EventCertificateBatchResult result = new EventCertificateBatchResult(
                List.of(), 0L, Pageable.unpaged(), Map.of(), Map.of());

        assertThat(result.eventCertificates()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.resolvedPageable()).isEqualTo(Pageable.unpaged());
        assertThat(result.eventMap()).isEmpty();
        assertThat(result.mediaFileMap()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // CupDetailedBatchResult
    // -------------------------------------------------------------------------

    @Test
    void cupDetailedBatchResult_accessorsReturnCorrectValues() {
        CupDetailedBatchResult result = new CupDetailedBatchResult(
                null, Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());

        assertThat(result.cupDetailed()).isNull();
        assertThat(result.eventMap()).isEmpty();
        assertThat(result.hasSplitTimesMap()).isEmpty();
        assertThat(result.organisationMap()).isEmpty();
        assertThat(result.certificateMap()).isEmpty();
        assertThat(result.countryMap()).isEmpty();
        assertThat(result.childOrganisationMap()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // PersonRepository.PersonPerson
    // -------------------------------------------------------------------------

    @Test
    void personPerson_accessorsReturnCorrectValues() {
        Person source = Person.of("Müller", "Max", null, Gender.M);
        Person target = Person.of("Schmidt", "Anna", null, Gender.F);
        PersonRepository.PersonPerson pp = new PersonRepository.PersonPerson(source, target);

        assertThat(pp.source()).isEqualTo(source);
        assertThat(pp.target()).isEqualTo(target);
    }
}
