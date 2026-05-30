package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DomainIdTest {

    // -------------------------------------------------------------------------
    // CupId
    // -------------------------------------------------------------------------

    @Test
    void cupId_isPersistent_falseForEmpty() {
        assertThat(CupId.empty().isPersistent()).isFalse();
    }

    @Test
    void cupId_isPersistent_trueForNonZero() {
        assertThat(CupId.of(1L).isPersistent()).isTrue();
    }

    @Test
    void cupId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> CupId.of(-1L));
    }

    @Test
    void cupId_compareTo_numeric() {
        assertThat(CupId.of(1L).compareTo(CupId.of(2L))).isLessThan(0);
        assertThat(CupId.of(2L).compareTo(CupId.of(1L))).isGreaterThan(0);
        assertThat(CupId.of(5L).compareTo(CupId.of(5L))).isEqualTo(0);
    }

    @Test
    void cupId_toString_containsClassNameAndValue() {
        assertThat(CupId.of(7L).toString()).isEqualTo("CupId=7");
    }

    // -------------------------------------------------------------------------
    // SplitTimeId
    // -------------------------------------------------------------------------

    @Test
    void splitTimeId_isPersistent_falseForEmpty() {
        assertThat(SplitTimeId.empty().isPersistent()).isFalse();
    }

    @Test
    void splitTimeId_isPersistent_trueForNonZero() {
        assertThat(SplitTimeId.of(42L).isPersistent()).isTrue();
    }

    @Test
    void splitTimeId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> SplitTimeId.of(-1L));
    }

    @Test
    void splitTimeId_compareTo_numeric() {
        assertThat(SplitTimeId.of(1L).compareTo(SplitTimeId.of(2L))).isLessThan(0);
        assertThat(SplitTimeId.of(3L).compareTo(SplitTimeId.of(3L))).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // PersonRaceResultId
    // -------------------------------------------------------------------------

    @Test
    void personRaceResultId_isPersistent_falseForEmpty() {
        assertThat(PersonRaceResultId.empty().isPersistent()).isFalse();
    }

    @Test
    void personRaceResultId_isPersistent_trueForNonZero() {
        assertThat(PersonRaceResultId.of(7L).isPersistent()).isTrue();
    }

    @Test
    void personRaceResultId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> PersonRaceResultId.of(-1L));
    }

    @Test
    void personRaceResultId_compareTo_numeric() {
        assertThat(PersonRaceResultId.of(1L).compareTo(PersonRaceResultId.of(2L))).isLessThan(0);
        assertThat(PersonRaceResultId.of(3L).compareTo(PersonRaceResultId.of(3L))).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // CourseId
    // -------------------------------------------------------------------------

    @Test
    void courseId_isPersistent_falseForEmpty() {
        assertThat(CourseId.empty().isPersistent()).isFalse();
    }

    @Test
    void courseId_isPersistent_trueForNonNull() {
        assertThat(CourseId.of(3L).isPersistent()).isTrue();
    }

    @Test
    void courseId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> CourseId.of(-1L));
    }

    @Test
    void courseId_compareTo_numeric() {
        assertThat(CourseId.of(1L).compareTo(CourseId.of(2L))).isLessThan(0);
        assertThat(CourseId.of(3L).compareTo(CourseId.of(3L))).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // CupScoreListId
    // -------------------------------------------------------------------------

    @Test
    void cupScoreListId_isPersistent_falseForEmpty() {
        assertThat(CupScoreListId.empty().isPersistent()).isFalse();
    }

    @Test
    void cupScoreListId_isPersistent_trueForNonNull() {
        assertThat(CupScoreListId.of(9L).isPersistent()).isTrue();
    }

    @Test
    void cupScoreListId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> CupScoreListId.of(-1L));
    }

    @Test
    void cupScoreListId_compareTo_numeric() {
        assertThat(CupScoreListId.of(1L).compareTo(CupScoreListId.of(2L))).isLessThan(0);
        assertThat(CupScoreListId.of(3L).compareTo(CupScoreListId.of(3L))).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // PersonResultId
    // -------------------------------------------------------------------------

    @Test
    void personResultId_isPersistent_falseForEmpty() {
        assertThat(PersonResultId.empty().isPersistent()).isFalse();
    }

    @Test
    void personResultId_isPersistent_trueForNonZero() {
        assertThat(PersonResultId.of(15L).isPersistent()).isTrue();
    }

    @Test
    void personResultId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> PersonResultId.of(-1L));
    }

    @Test
    void personResultId_toString_containsClassNameAndValue() {
        assertThat(PersonResultId.of(15L).toString()).isEqualTo("PersonResultId=15");
    }

    // -------------------------------------------------------------------------
    // EventCertificateId
    // -------------------------------------------------------------------------

    @Test
    void eventCertificateId_isPersistent_falseForEmpty() {
        assertThat(EventCertificateId.empty().isPersistent()).isFalse();
    }

    @Test
    void eventCertificateId_isPersistent_trueForNonNull() {
        assertThat(EventCertificateId.of(100L).isPersistent()).isTrue();
    }

    @Test
    void eventCertificateId_of_throwsForNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> EventCertificateId.of(null));
    }

    @Test
    void eventCertificateId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> EventCertificateId.of(-1L));
    }

    @Test
    void eventCertificateId_compareTo_numeric() {
        assertThat(EventCertificateId.of(1L).compareTo(EventCertificateId.of(2L))).isLessThan(0);
        assertThat(EventCertificateId.of(3L).compareTo(EventCertificateId.of(3L))).isEqualTo(0);
    }

    @Test
    void eventCertificateId_toString_containsClassNameAndValue() {
        assertThat(EventCertificateId.of(100L).toString()).isEqualTo("EventCertificateId=100");
    }

    // -------------------------------------------------------------------------
    // EventCertificateStatId
    // -------------------------------------------------------------------------

    @Test
    void eventCertificateStatId_isPersistent_falseForEmpty() {
        assertThat(EventCertificateStatId.empty().isPersistent()).isFalse();
    }

    @Test
    void eventCertificateStatId_isPersistent_trueForNonNull() {
        assertThat(EventCertificateStatId.of(8L).isPersistent()).isTrue();
    }

    @Test
    void eventCertificateStatId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> EventCertificateStatId.of(-1L));
    }

    @Test
    void eventCertificateStatId_compareTo_numeric() {
        assertThat(EventCertificateStatId.of(1L).compareTo(EventCertificateStatId.of(2L))).isLessThan(0);
        assertThat(EventCertificateStatId.of(3L).compareTo(EventCertificateStatId.of(3L))).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // CountryId
    // -------------------------------------------------------------------------

    @Test
    void countryId_isPersistent_falseForEmpty() {
        assertThat(CountryId.empty().isPersistent()).isFalse();
    }

    @Test
    void countryId_isPersistent_trueForNonNull() {
        assertThat(CountryId.of(5L).isPersistent()).isTrue();
    }

    @Test
    void countryId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> CountryId.of(-1L));
    }

    @Test
    void countryId_compareTo_numeric() {
        assertThat(CountryId.of(1L).compareTo(CountryId.of(2L))).isLessThan(0);
        assertThat(CountryId.of(2L).compareTo(CountryId.of(1L))).isGreaterThan(0);
        assertThat(CountryId.of(3L).compareTo(CountryId.of(3L))).isEqualTo(0);
    }

    @Test
    void countryId_toString_containsClassNameAndValue() {
        assertThat(CountryId.of(5L).toString()).isEqualTo("CountryId=5");
    }

    // -------------------------------------------------------------------------
    // PersonId
    // -------------------------------------------------------------------------

    @Test
    void personId_isPersistent_falseForEmpty() {
        assertThat(PersonId.empty().isPersistent()).isFalse();
    }

    @Test
    void personId_isPersistent_trueForNonNull() {
        assertThat(PersonId.of(10L).isPersistent()).isTrue();
    }

    @Test
    void personId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> PersonId.of(-1L));
    }

    @Test
    void personId_compareTo_numeric() {
        assertThat(PersonId.of(1L).compareTo(PersonId.of(2L))).isLessThan(0);
        assertThat(PersonId.of(2L).compareTo(PersonId.of(1L))).isGreaterThan(0);
        assertThat(PersonId.of(5L).compareTo(PersonId.of(5L))).isEqualTo(0);
    }

    @Test
    void personId_toString_containsClassNameAndValue() {
        assertThat(PersonId.of(10L).toString()).isEqualTo("PersonId=10");
    }

    // -------------------------------------------------------------------------
    // EventId / ResultListId / RaceId
    // -------------------------------------------------------------------------

    @Test
    void eventId_empty_returnsZeroValue() {
        assertThat(EventId.empty().value()).isEqualTo(0L);
    }

    @Test
    void eventId_isPersistent_falseForEmpty() {
        assertThat(EventId.empty().isPersistent()).isFalse();
    }

    @Test
    void eventId_isPersistent_trueForNonZero() {
        assertThat(EventId.of(1L).isPersistent()).isTrue();
    }

    @Test
    void eventId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> EventId.of(-1L));
    }

    @Test
    void eventId_compareTo_numeric() {
        assertThat(EventId.of(1L).compareTo(EventId.of(2L))).isLessThan(0);
        assertThat(EventId.of(2L).compareTo(EventId.of(1L))).isGreaterThan(0);
        assertThat(EventId.of(5L).compareTo(EventId.of(5L))).isEqualTo(0);
    }

    @Test
    void eventId_toString_containsClassNameAndValue() {
        assertThat(EventId.of(3L).toString()).isEqualTo("EventId=3");
    }

    @Test
    void resultListId_empty_returnsZeroValue() {
        assertThat(ResultListId.empty().value()).isEqualTo(0L);
    }

    @Test
    void resultListId_isPersistent_falseForEmpty() {
        assertThat(ResultListId.empty().isPersistent()).isFalse();
    }

    @Test
    void resultListId_isPersistent_trueForNonZero() {
        assertThat(ResultListId.of(1L).isPersistent()).isTrue();
    }

    @Test
    void resultListId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> ResultListId.of(-1L));
    }

    @Test
    void resultListId_compareTo_numeric() {
        assertThat(ResultListId.of(1L).compareTo(ResultListId.of(2L))).isLessThan(0);
        assertThat(ResultListId.of(3L).compareTo(ResultListId.of(3L))).isEqualTo(0);
    }

    @Test
    void resultListId_toString_containsClassNameAndValue() {
        assertThat(ResultListId.of(5L).toString()).isEqualTo("ResultListId=5");
    }

    @Test
    void raceId_empty_returnsZeroValue() {
        assertThat(RaceId.empty().value()).isEqualTo(0L);
    }

    @Test
    void mediaFileId_isPersistent_falseForEmpty() {
        assertThat(MediaFileId.empty().isPersistent()).isFalse();
    }

    @Test
    void mediaFileId_isPersistent_trueForNonNull() {
        assertThat(MediaFileId.of(20L).isPersistent()).isTrue();
    }

    @Test
    void mediaFileId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> MediaFileId.of(-1L));
    }

    @Test
    void mediaFileId_compareTo_numeric() {
        assertThat(MediaFileId.of(1L).compareTo(MediaFileId.of(2L))).isLessThan(0);
        assertThat(MediaFileId.of(3L).compareTo(MediaFileId.of(3L))).isEqualTo(0);
    }

    @Test
    void splitTimeListId_isPersistent_falseForEmpty() {
        assertThat(SplitTimeListId.empty().isPersistent()).isFalse();
    }

    @Test
    void splitTimeListId_isPersistent_trueForNonNull() {
        assertThat(SplitTimeListId.of(4L).isPersistent()).isTrue();
    }

    @Test
    void splitTimeListId_compareTo_numeric() {
        assertThat(SplitTimeListId.of(1L).compareTo(SplitTimeListId.of(2L))).isLessThan(0);
        assertThat(SplitTimeListId.of(3L).compareTo(SplitTimeListId.of(3L))).isEqualTo(0);
    }

    @Test
    void splitTimeListId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> SplitTimeListId.of(-1L));
    }

    @Test
    void splitTimeListId_toString_containsClassNameAndValue() {
        assertThat(SplitTimeListId.of(4L).toString()).isEqualTo("SplitTimeListId=4");
    }

    // -------------------------------------------------------------------------
    // RaceId
    // -------------------------------------------------------------------------

    @Test
    void raceId_isPersistent_falseForEmpty() {
        assertThat(RaceId.empty().isPersistent()).isFalse();
    }

    @Test
    void raceId_isPersistent_trueForNonNull() {
        assertThat(RaceId.of(7L).isPersistent()).isTrue();
    }

    @Test
    void raceId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> RaceId.of(-1L));
    }

    @Test
    void raceId_compareTo_numeric() {
        assertThat(RaceId.of(1L).compareTo(RaceId.of(2L))).isLessThan(0);
        assertThat(RaceId.of(5L).compareTo(RaceId.of(5L))).isEqualTo(0);
    }

    @Test
    void raceId_toString_containsClassNameAndValue() {
        assertThat(RaceId.of(7L).toString()).isEqualTo("RaceId=7");
    }

    // -------------------------------------------------------------------------
    // OrganisationId
    // -------------------------------------------------------------------------

    @Test
    void organisationId_isPersistent_falseForEmpty() {
        assertThat(OrganisationId.empty().isPersistent()).isFalse();
    }

    @Test
    void organisationId_isPersistent_trueForNonNull() {
        assertThat(OrganisationId.of(11L).isPersistent()).isTrue();
    }

    @Test
    void organisationId_of_allowsNull() {
        OrganisationId oid = OrganisationId.of(null);
        assertThat(oid.value()).isNull();
    }

    @Test
    void organisationId_of_throwsForNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> OrganisationId.of(-1L));
    }

    // -------------------------------------------------------------------------
    // toString — alle ID-Klassen mit getSimpleName()=value Format
    // -------------------------------------------------------------------------

    @Test
    void splitTimeId_toString_containsClassNameAndValue() {
        assertThat(SplitTimeId.of(42L).toString()).isEqualTo("SplitTimeId=42");
    }

    @Test
    void personRaceResultId_toString_containsClassNameAndValue() {
        assertThat(PersonRaceResultId.of(7L).toString()).isEqualTo("PersonRaceResultId=7");
    }

    @Test
    void courseId_toString_containsClassNameAndValue() {
        assertThat(CourseId.of(3L).toString()).isEqualTo("CourseId=3");
    }

    @Test
    void mediaFileId_toString_containsClassNameAndValue() {
        assertThat(MediaFileId.of(20L).toString()).isEqualTo("MediaFileId=20");
    }

    @Test
    void eventCertificateStatId_toString_containsClassNameAndValue() {
        assertThat(EventCertificateStatId.of(8L).toString()).isEqualTo("EventCertificateStatId=8");
    }

    @Test
    void cupScoreListId_toString_containsClassNameAndValue() {
        assertThat(CupScoreListId.of(9L).toString()).isEqualTo("CupScoreListId=9");
    }
}
