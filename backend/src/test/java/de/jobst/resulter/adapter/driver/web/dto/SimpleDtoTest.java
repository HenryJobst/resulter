package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.port.ClassGroupOption;
import de.jobst.resulter.application.port.CourseGroupOption;
import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleDtoTest {

    // -------------------------------------------------------------------------
    // RaceDto
    // -------------------------------------------------------------------------

    @Test
    void raceDto_accessorsReturnCorrectValues() {
        RaceDto dto = new RaceDto(1L, "Lauf 1", (byte) 1);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Lauf 1");
        assertThat(dto.number()).isEqualTo((byte) 1);
    }

    // -------------------------------------------------------------------------
    // PersonWithScoreDto
    // -------------------------------------------------------------------------

    @Test
    void personWithScoreDto_accessorsReturnCorrectValues() {
        PersonWithScoreDto dto = new PersonWithScoreDto(42L, 7.5, "H21");
        assertThat(dto.personId()).isEqualTo(42L);
        assertThat(dto.score()).isEqualTo(7.5);
        assertThat(dto.classShortName()).isEqualTo("H21");
    }

    // -------------------------------------------------------------------------
    // GenderDto
    // -------------------------------------------------------------------------

    @Test
    void genderDto_from_mapsGender() {
        GenderDto dto = GenderDto.from(Gender.M);
        assertThat(dto.id()).isEqualTo("M");
    }

    @Test
    void genderDto_from_mapsGenderF() {
        GenderDto dto = GenderDto.from(Gender.F);
        assertThat(dto.id()).isEqualTo("F");
    }

    // -------------------------------------------------------------------------
    // CupTypeDto
    // -------------------------------------------------------------------------

    @Test
    void cupTypeDto_from_mapsCupType() {
        CupTypeDto dto = CupTypeDto.from(CupType.KJ);
        assertThat(dto.id()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // AggregatedPersonScoresDto
    // -------------------------------------------------------------------------

    @Test
    void aggregatedPersonScoresDto_accessorsReturnCorrectValues() {
        PersonWithScoreDto person = new PersonWithScoreDto(1L, 5.0, "D18");
        AggregatedPersonScoresDto dto = new AggregatedPersonScoresDto("D18", List.of(person));
        assertThat(dto.classResultShortName()).isEqualTo("D18");
        assertThat(dto.personWithScoreList()).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // ClassResultScoreDto
    // -------------------------------------------------------------------------

    @Test
    void classResultScoreDto_accessorsReturnCorrectValues() {
        ClassResultScoreDto dto = new ClassResultScoreDto("H14", List.of());
        assertThat(dto.classResultShortName()).isEqualTo("H14");
        assertThat(dto.personWithScores()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // OrganisationScoreBaseDto
    // -------------------------------------------------------------------------

    @Test
    void organisationScoreBaseDto_accessorsReturnCorrectValues() {
        OrganisationScoreBaseDto dto = new OrganisationScoreBaseDto(null, 42.0);
        assertThat(dto.organisationDto()).isNull();
        assertThat(dto.score()).isEqualTo(42.0);
    }

    // -------------------------------------------------------------------------
    // RaceClassResultGroupedCupScoreDto
    // -------------------------------------------------------------------------

    @Test
    void raceClassResultGroupedCupScoreDto_accessorsReturnCorrectValues() {
        RaceDto race = new RaceDto(1L, "Sprint", (byte) 1);
        RaceClassResultGroupedCupScoreDto dto = new RaceClassResultGroupedCupScoreDto(race, List.of());
        assertThat(dto.race()).isEqualTo(race);
        assertThat(dto.classResultScores()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // RaceOrganisationGroupedCupScoreDto
    // -------------------------------------------------------------------------

    @Test
    void raceOrganisationGroupedCupScoreDto_accessorsReturnCorrectValues() {
        RaceDto race = new RaceDto(2L, "Mittel", (byte) 2);
        RaceOrganisationGroupedCupScoreDto dto = new RaceOrganisationGroupedCupScoreDto(race, List.of());
        assertThat(dto.race()).isEqualTo(race);
        assertThat(dto.organisationScores()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // SplitTimeTableOptionsDto
    // -------------------------------------------------------------------------

    @Test
    void splitTimeTableOptionsDto_accessorsReturnCorrectValues() {
        SplitTimeTableOptionsDto dto = new SplitTimeTableOptionsDto(List.of(), List.of());
        assertThat(dto.classes()).isEmpty();
        assertThat(dto.courses()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // EventResultsDto
    // -------------------------------------------------------------------------

    @Test
    void eventResultsDto_accessorsReturnCorrectValues() {
        EventResultsDto dto = new EventResultsDto(List.of());
        assertThat(dto.resultLists()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // EventCertificateStatsDto
    // -------------------------------------------------------------------------

    @Test
    void eventCertificateStatsDto_accessorsReturnCorrectValues() {
        EventCertificateStatsDto dto = new EventCertificateStatsDto(List.of());
        assertThat(dto.stats()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // CupStatisticsDto
    // -------------------------------------------------------------------------

    @Test
    void cupStatisticsDto_accessorsReturnCorrectValues() {
        CupStatisticsDto dto = new CupStatisticsDto(null, List.of());
        assertThat(dto.overallStatistics()).isNull();
        assertThat(dto.organisationStatistics()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // MediaFileKeyDto
    // -------------------------------------------------------------------------

    @Test
    void mediaFileKeyDto_accessorsReturnCorrectValues() {
        MediaFileKeyDto dto = new MediaFileKeyDto(5L, "photo.jpg", "thumb-base64");
        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.fileName()).isEqualTo("photo.jpg");
        assertThat(dto.thumbnailContent()).isEqualTo("thumb-base64");
    }

    // -------------------------------------------------------------------------
    // EventRacesCupScoreDto
    // -------------------------------------------------------------------------

    @Test
    void eventRacesCupScoreDto_accessorsReturnCorrectValues() {
        EventRacesCupScoreDto dto = new EventRacesCupScoreDto(null, List.of(), List.of());
        assertThat(dto.event()).isNull();
        assertThat(dto.raceOrganisationGroupedCupScores()).isEmpty();
        assertThat(dto.raceClassResultGroupedCupScores()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // UserPermissionsDto
    // -------------------------------------------------------------------------

    @Test
    void userPermissionsDto_from_withAdminRole_allTrue() {
        UserPermissionsDto dto = UserPermissionsDto.from(List.of("ADMIN"), List.of());
        assertThat(dto.canManageEvents()).isTrue();
        assertThat(dto.canUploadResults()).isTrue();
        assertThat(dto.canManageCups()).isTrue();
        assertThat(dto.canViewReports()).isTrue();
        assertThat(dto.canManageUsers()).isTrue();
        assertThat(dto.canAccessAdmin()).isTrue();
    }

    @Test
    void userPermissionsDto_from_withAdminGroup_allTrue() {
        UserPermissionsDto dto = UserPermissionsDto.from(List.of(), List.of("ADMIN"));
        assertThat(dto.canManageEvents()).isTrue();
    }

    @Test
    void userPermissionsDto_from_withEndpointAdminRole_onlyAccessAdmin() {
        UserPermissionsDto dto = UserPermissionsDto.from(List.of("ENDPOINT_ADMIN"), List.of());
        assertThat(dto.canManageEvents()).isFalse();
        assertThat(dto.canAccessAdmin()).isTrue();
    }

    @Test
    void userPermissionsDto_from_withEndpointAdminGroup_onlyAccessAdmin() {
        UserPermissionsDto dto = UserPermissionsDto.from(List.of(), List.of("ENDPOINT_ADMIN"));
        assertThat(dto.canManageEvents()).isFalse();
        assertThat(dto.canAccessAdmin()).isTrue();
    }

    @Test
    void userPermissionsDto_from_withNoRoles_allFalseExceptViewReports() {
        UserPermissionsDto dto = UserPermissionsDto.from(List.of(), List.of());
        assertThat(dto.canManageEvents()).isFalse();
        assertThat(dto.canViewReports()).isTrue();
        assertThat(dto.canAccessAdmin()).isFalse();
    }

    // -------------------------------------------------------------------------
    // EventKeyDto — compareTo-Branches
    // -------------------------------------------------------------------------

    @Test
    void eventKeyDto_compareTo_bothStartTimesNull_comparesByName() {
        EventKeyDto a = new EventKeyDto(1L, "Alpha", null);
        EventKeyDto b = new EventKeyDto(2L, "Beta", null);
        assertThat(a.compareTo(b)).isNegative();
    }

    @Test
    void eventKeyDto_compareTo_firstStartTimeNull_secondNotNull_returnsPositive() {
        EventKeyDto a = new EventKeyDto(1L, "Alpha", null);
        EventKeyDto b = new EventKeyDto(2L, "Beta", java.time.ZonedDateTime.now());
        assertThat(a.compareTo(b)).isPositive();
    }

    @Test
    void eventKeyDto_compareTo_firstNotNull_secondNull_returnsNegative() {
        EventKeyDto a = new EventKeyDto(1L, "Alpha", java.time.ZonedDateTime.now());
        EventKeyDto b = new EventKeyDto(2L, "Beta", null);
        assertThat(a.compareTo(b)).isNegative();
    }

    @Test
    void eventKeyDto_compareTo_sameStartTime_comparesByName() {
        java.time.ZonedDateTime t = java.time.ZonedDateTime.now();
        EventKeyDto a = new EventKeyDto(1L, "Alpha", t);
        EventKeyDto b = new EventKeyDto(2L, "Beta", t);
        assertThat(a.compareTo(b)).isNegative();
    }

    @Test
    void eventKeyDto_compareTo_sameStartTimeAndName_comparesById() {
        java.time.ZonedDateTime t = java.time.ZonedDateTime.now();
        EventKeyDto a = new EventKeyDto(1L, "Same", t);
        EventKeyDto b = new EventKeyDto(2L, "Same", t);
        assertThat(a.compareTo(b)).isNegative();
    }

    // -------------------------------------------------------------------------
    // OrganisationKeyDto
    // -------------------------------------------------------------------------

    @Test
    void organisationKeyDto_accessorsReturnCorrectValues() {
        OrganisationKeyDto dto = new OrganisationKeyDto(3L, "TSB OJ");
        assertThat(dto.id()).isEqualTo(3L);
        assertThat(dto.name()).isEqualTo("TSB OJ");
    }

    // -------------------------------------------------------------------------
    // CourseDto.from — with non-empty id (true-branch)
    // -------------------------------------------------------------------------

    @Test
    void courseDto_from_withNonEmptyId_usesId() {
        Course course = new Course(
                CourseId.of(5L),
                EventId.of(1L),
                CourseName.of("Lang"),
                CourseLength.of(8.0),
                CourseClimb.of(300.0),
                NumberOfControls.of(25));
        CourseDto dto = CourseDto.from(course);
        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.name()).isEqualTo("Lang");
    }

    // -------------------------------------------------------------------------
    // EventCertificateKeyDto.from — with non-null id (true-branch)
    // -------------------------------------------------------------------------

    @Test
    void eventCertificateKeyDto_from_withNonNullId_usesId() {
        EventCertificate cert = EventCertificate.of(9L, "Urkunde", null, null, null, false);
        EventCertificateKeyDto dto = EventCertificateKeyDto.from(cert);
        assertThat(dto.id()).isEqualTo(9L);
        assertThat(dto.name()).isEqualTo("Urkunde");
    }

    // -------------------------------------------------------------------------
    // ClassGroupOptionDto
    // -------------------------------------------------------------------------

    @Test
    void classGroupOptionDto_from_mapsCorrectly() {
        ClassGroupOption option = new ClassGroupOption("H21", 30);
        ClassGroupOptionDto dto = ClassGroupOptionDto.from(option);
        assertThat(dto.className()).isEqualTo("H21");
        assertThat(dto.runnerCount()).isEqualTo(30);
    }

    // -------------------------------------------------------------------------
    // CourseGroupOptionDto
    // -------------------------------------------------------------------------

    @Test
    void courseGroupOptionDto_from_mapsCorrectly() {
        CourseGroupOption option = new CourseGroupOption(3L, "Lang", List.of("H21", "D21"), 50);
        CourseGroupOptionDto dto = CourseGroupOptionDto.from(option);
        assertThat(dto.courseId()).isEqualTo(3L);
        assertThat(dto.courseName()).isEqualTo("Lang");
        assertThat(dto.classNames()).containsExactly("H21", "D21");
        assertThat(dto.runnerCount()).isEqualTo(50);
    }
}
