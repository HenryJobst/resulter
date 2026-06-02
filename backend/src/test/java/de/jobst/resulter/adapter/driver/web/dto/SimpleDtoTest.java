package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.port.ClassGroupOption;
import de.jobst.resulter.application.port.CourseGroupOption;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.analysis.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.time.Duration;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    // ClassResultDto — Accessors + compareTo
    // -------------------------------------------------------------------------

    @Test
    void classResultDto_accessorsReturnCorrectValues() {
        ClassResultDto dto = new ClassResultDto("H21", "Herren 21", 5L, List.of());
        assertThat(dto.shortName()).isEqualTo("H21");
        assertThat(dto.name()).isEqualTo("Herren 21");
        assertThat(dto.courseId()).isEqualTo(5L);
        assertThat(dto.personResults()).isEmpty();
    }

    @Test
    void classResultDto_compareTo_comparesByName() {
        ClassResultDto a = new ClassResultDto("H21", "Alpha", null, List.of());
        ClassResultDto b = new ClassResultDto("D21", "Beta", null, List.of());
        assertThat(a.compareTo(b)).isNegative();
        assertThat(b.compareTo(a)).isPositive();
    }

    // -------------------------------------------------------------------------
    // CountryDto — from() mit und ohne Id
    // -------------------------------------------------------------------------

    @Test
    void countryDto_from_withId_usesId() {
        Country country = Country.of(3L, "DE", "Deutschland");
        CountryDto dto = CountryDto.from(country);
        assertThat(dto.id()).isEqualTo(3L);
        assertThat(dto.name()).isEqualTo("Deutschland");
        assertThat(dto.code()).isEqualTo("DE");
    }

    @Test
    void countryDto_from_withoutId_usesZero() {
        Country country = Country.of(null, "AT", "Österreich");
        CountryDto dto = CountryDto.from(country);
        assertThat(dto.id()).isEqualTo(0L);
    }

    // -------------------------------------------------------------------------
    // CupKeyDto — from() mit und ohne Id
    // -------------------------------------------------------------------------

    @Test
    void cupKeyDto_from_withId_usesId() {
        Cup cup = Cup.of(7L, "NOR Cup", CupType.NOR, Year.of(2025), List.of());
        CupKeyDto dto = CupKeyDto.from(cup);
        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.name()).isEqualTo("NOR Cup");
        assertThat(dto.cupType()).isEqualTo(CupType.NOR);
    }

    @Test
    void cupKeyDto_from_withoutId_usesZero() {
        Cup cup = Cup.of(null, "KJ Cup", CupType.KJ, Year.of(2025), List.of());
        CupKeyDto dto = CupKeyDto.from(cup);
        assertThat(dto.id()).isEqualTo(0L);
    }

    // -------------------------------------------------------------------------
    // OrganisationDto — mapOrdersDtoToDomain / mapOrdersDomainToDto
    // -------------------------------------------------------------------------

    @Test
    void organisationDto_mapOrdersDtoToDomain_knownProperties() {
        assertThat(OrganisationDto.mapOrdersDtoToDomain(Sort.Order.asc("id"))).isEqualTo("id.value");
        assertThat(OrganisationDto.mapOrdersDtoToDomain(Sort.Order.asc("name"))).isEqualTo("name.value");
        assertThat(OrganisationDto.mapOrdersDtoToDomain(Sort.Order.asc("shortName"))).isEqualTo("shortName.value");
        assertThat(OrganisationDto.mapOrdersDtoToDomain(Sort.Order.asc("type"))).isEqualTo("type.id");
        assertThat(OrganisationDto.mapOrdersDtoToDomain(Sort.Order.asc("country.name"))).isEqualTo("country.name.value");
        assertThat(OrganisationDto.mapOrdersDtoToDomain(Sort.Order.asc("childOrganisationIds"))).isEqualTo("childOrganisationIds");
    }

    @Test
    void organisationDto_mapOrdersDtoToDomain_unknownProperty_returnsAsIs() {
        assertThat(OrganisationDto.mapOrdersDtoToDomain(Sort.Order.asc("other"))).isEqualTo("other");
    }

    @Test
    void organisationDto_mapOrdersDomainToDto_knownProperties() {
        assertThat(OrganisationDto.mapOrdersDomainToDto(Sort.Order.asc("id.value"))).isEqualTo("id");
        assertThat(OrganisationDto.mapOrdersDomainToDto(Sort.Order.asc("name.value"))).isEqualTo("name");
        assertThat(OrganisationDto.mapOrdersDomainToDto(Sort.Order.asc("shortName.value"))).isEqualTo("shortName");
        assertThat(OrganisationDto.mapOrdersDomainToDto(Sort.Order.asc("type.id"))).isEqualTo("type");
        assertThat(OrganisationDto.mapOrdersDomainToDto(Sort.Order.asc("country.name.value"))).isEqualTo("country.name");
        assertThat(OrganisationDto.mapOrdersDomainToDto(Sort.Order.asc("childOrganisationIds"))).isEqualTo("childOrganisationIds");
    }

    @Test
    void organisationDto_mapOrdersDomainToDto_unknownProperty_returnsAsIs() {
        assertThat(OrganisationDto.mapOrdersDomainToDto(Sort.Order.asc("other"))).isEqualTo("other");
    }

    // -------------------------------------------------------------------------
    // OrganisationScoreDto
    // -------------------------------------------------------------------------

    @Test
    void organisationScoreDto_accessorsReturnCorrectValues() {
        PersonWithScoreDto pws = new PersonWithScoreDto(1L, 9.0, "H21");
        OrganisationScoreDto dto = new OrganisationScoreDto(null, 42.5, List.of(pws));
        assertThat(dto.organisation()).isNull();
        assertThat(dto.score()).isEqualTo(42.5);
        assertThat(dto.personWithScores()).hasSize(1);
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
    // EventDto — mapOrders + compareTo
    // -------------------------------------------------------------------------

    @Test
    void eventDto_mapOrdersDtoToDomain_knownProperties() {
        assertThat(EventDto.mapOrdersDtoToDomain(Sort.Order.asc("id"))).isEqualTo("id.value");
        assertThat(EventDto.mapOrdersDtoToDomain(Sort.Order.asc("name"))).isEqualTo("event.name.value");
        assertThat(EventDto.mapOrdersDtoToDomain(Sort.Order.asc("startTime"))).isEqualTo("startTime.value");
        assertThat(EventDto.mapOrdersDtoToDomain(Sort.Order.asc("state"))).isEqualTo("state.id");
        assertThat(EventDto.mapOrdersDtoToDomain(Sort.Order.asc("organisations"))).isEqualTo("organisations");
        assertThat(EventDto.mapOrdersDtoToDomain(Sort.Order.asc("discipline"))).isEqualTo("discipline.id");
        assertThat(EventDto.mapOrdersDtoToDomain(Sort.Order.asc("hasSplitTimes"))).isEqualTo("startTime.value");
        assertThat(EventDto.mapOrdersDtoToDomain(Sort.Order.asc("other"))).isEqualTo("id.value");
    }

    @Test
    void eventDto_mapOrdersDomainToDto_knownProperties() {
        assertThat(EventDto.mapOrdersDomainToDto(Sort.Order.asc("id.value"))).isEqualTo("id");
        assertThat(EventDto.mapOrdersDomainToDto(Sort.Order.asc("event.name.value"))).isEqualTo("name");
        assertThat(EventDto.mapOrdersDomainToDto(Sort.Order.asc("startTime.value"))).isEqualTo("startTime");
        assertThat(EventDto.mapOrdersDomainToDto(Sort.Order.asc("state.id"))).isEqualTo("state");
        assertThat(EventDto.mapOrdersDomainToDto(Sort.Order.asc("organisations"))).isEqualTo("organisations");
        assertThat(EventDto.mapOrdersDomainToDto(Sort.Order.asc("discipline.id"))).isEqualTo("discipline");
        assertThat(EventDto.mapOrdersDomainToDto(Sort.Order.asc("other"))).isEqualTo("other");
    }

    @Test
    void eventDto_compareTo_bothStartTimesNull_comparesByName() {
        EventDto a = new EventDto(1L, "Alpha", null, null, List.of(), null, false, null, false);
        EventDto b = new EventDto(2L, "Beta", null, null, List.of(), null, false, null, false);
        assertThat(a.compareTo(b)).isNegative();
    }

    @Test
    void eventDto_compareTo_firstStartTimeNull_secondNotNull_returnsPositive() {
        EventDto a = new EventDto(1L, "Alpha", null, null, List.of(), null, false, null, false);
        EventDto b = new EventDto(2L, "Beta", "2025-01-01", null, List.of(), null, false, null, false);
        assertThat(a.compareTo(b)).isPositive();
    }

    @Test
    void eventDto_compareTo_firstStartTimeNotNull_secondNull_returnsNegative() {
        EventDto a = new EventDto(1L, "Alpha", "2025-01-01", null, List.of(), null, false, null, false);
        EventDto b = new EventDto(2L, "Beta", null, null, List.of(), null, false, null, false);
        assertThat(a.compareTo(b)).isNegative();
    }

    @Test
    void eventDto_compareTo_sameStartTime_comparesByName() {
        EventDto a = new EventDto(1L, "Alpha", "2025-01-01", null, List.of(), null, false, null, false);
        EventDto b = new EventDto(2L, "Beta", "2025-01-01", null, List.of(), null, false, null, false);
        assertThat(a.compareTo(b)).isNegative();
    }

    @Test
    void eventDto_compareTo_sameStartTimeAndName_comparesById() {
        EventDto a = new EventDto(1L, "Same", "2025-01-01", null, List.of(), null, false, null, false);
        EventDto b = new EventDto(2L, "Same", "2025-01-01", null, List.of(), null, false, null, false);
        assertThat(a.compareTo(b)).isNegative();
    }

    // -------------------------------------------------------------------------
    // CupScoreDto — from() + compareTo
    // -------------------------------------------------------------------------

    @Test
    void cupScoreDto_from_mapsCorrectly() {
        CupScore score = CupScore.of(PersonId.of(1L), OrganisationId.of(1L), ClassResultShortName.of("H21"), 12.0);
        CupScoreDto dto = CupScoreDto.from(score);
        assertThat(dto.personId()).isEqualTo(1L);
        assertThat(dto.classShortName()).isEqualTo("H21");
        assertThat(dto.score()).isEqualTo(12.0);
    }

    @Test
    void cupScoreDto_compareTo_comparesByClassThenScoreThenPerson() {
        CupScoreDto a = new CupScoreDto(1L, "D21", 10.0);
        CupScoreDto b = new CupScoreDto(2L, "H21", 10.0);
        assertThat(a.compareTo(b)).isNegative(); // D21 < H21
    }

    @Test
    void cupScoreDto_compareTo_sameClassHigherScoreFirst() {
        CupScoreDto a = new CupScoreDto(1L, "H21", 15.0);
        CupScoreDto b = new CupScoreDto(2L, "H21", 10.0);
        assertThat(a.compareTo(b)).isPositive(); // 15 > 10
    }

    @Test
    void cupScoreDto_compareTo_sameClassAndScore_comparesByPerson() {
        CupScoreDto a = new CupScoreDto(1L, "H21", 10.0);
        CupScoreDto b = new CupScoreDto(2L, "H21", 10.0);
        assertThat(a.compareTo(b)).isNegative();
    }

    // -------------------------------------------------------------------------
    // CupScoreListDto — from()
    // -------------------------------------------------------------------------

    @Test
    void cupScoreListDto_from_withCreateTime_mapsCorrectly() {
        ZonedDateTime now = ZonedDateTime.now();
        CupScoreList csl = new CupScoreList(
                CupScoreListId.of(1L), CupId.of(2L), ResultListId.of(3L),
                List.of(), "creator", now);
        CupScoreListDto dto = CupScoreListDto.from(csl);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.cupId()).isEqualTo(2L);
        assertThat(dto.resultListId()).isEqualTo(3L);
        assertThat(dto.creator()).isEqualTo("creator");
        assertThat(dto.createTime()).isNotNull();
        assertThat(dto.cupScores()).isEmpty();
    }

    @Test
    void cupScoreListDto_from_withNullCreateTime_mapsCorrectly() {
        CupScoreList csl = new CupScoreList(
                CupScoreListId.of(1L), CupId.of(2L), ResultListId.of(3L),
                List.of(), null, null);
        CupScoreListDto dto = CupScoreListDto.from(csl);
        assertThat(dto.createTime()).isNull();
    }

    // -------------------------------------------------------------------------
    // PersonResultDto — compareTo-Branches
    // -------------------------------------------------------------------------

    @Test
    void personResultDto_accessorsReturnCorrectValues() {
        PersonResultDto dto = new PersonResultDto(1L, 2L, Duration.ofMinutes(30), "OK", 3L, (byte) 1);
        assertThat(dto.position()).isEqualTo(1L);
        assertThat(dto.personId()).isEqualTo(2L);
        assertThat(dto.runTime()).isEqualTo(Duration.ofMinutes(30));
        assertThat(dto.resultStatus()).isEqualTo("OK");
        assertThat(dto.organisationId()).isEqualTo(3L);
        assertThat(dto.raceNumber()).isEqualTo((byte) 1);
    }

    @Test
    void personResultDto_compareTo_comparesByPosition() {
        PersonResultDto a = new PersonResultDto(1L, 1L, Duration.ofMinutes(30), "OK", 1L, (byte) 1);
        PersonResultDto b = new PersonResultDto(2L, 2L, Duration.ofMinutes(35), "OK", 1L, (byte) 1);
        assertThat(a.compareTo(b)).isNegative();
    }

    @Test
    void personResultDto_compareTo_samePosition_comparesByRunTime() {
        PersonResultDto a = new PersonResultDto(1L, 1L, Duration.ofMinutes(30), "OK", 1L, (byte) 1);
        PersonResultDto b = new PersonResultDto(1L, 2L, Duration.ofMinutes(35), "OK", 1L, (byte) 1);
        assertThat(a.compareTo(b)).isNegative();
    }

    @Test
    void personResultDto_compareTo_samePositionAndRunTime_comparesByStatus() {
        PersonResultDto a = new PersonResultDto(1L, 1L, null, "DNS", 1L, (byte) 1);
        PersonResultDto b = new PersonResultDto(1L, 2L, null, "OK", 1L, (byte) 1);
        assertThat(a.compareTo(b)).isNegative();
    }

    @Test
    void personResultDto_compareTo_samePositionRunTimeAndStatus_comparesByPersonId() {
        PersonResultDto a = new PersonResultDto(1L, 1L, null, "OK", 1L, (byte) 1);
        PersonResultDto b = new PersonResultDto(1L, 2L, null, "OK", 1L, (byte) 1);
        assertThat(a.compareTo(b)).isNegative();
    }

    @Test
    void personResultDto_compareTo_sameUntilRaceNumber_comparesByRaceNumber() {
        PersonResultDto a = new PersonResultDto(1L, 1L, null, "OK", 1L, (byte) 1);
        PersonResultDto b = new PersonResultDto(1L, 1L, null, "OK", 1L, (byte) 2);
        assertThat(a.compareTo(b)).isNegative();
    }

    // -------------------------------------------------------------------------
    // EventCertificateDto — mapOrders
    // -------------------------------------------------------------------------

    @Test
    void eventCertificateDto_mapOrdersDtoToDomain_knownProperties() {
        assertThat(EventCertificateDto.mapOrdersDtoToDomain(Sort.Order.asc("id"))).isEqualTo("id.value");
        assertThat(EventCertificateDto.mapOrdersDtoToDomain(Sort.Order.asc("name"))).isEqualTo("name.value");
        assertThat(EventCertificateDto.mapOrdersDtoToDomain(Sort.Order.asc("other"))).isEqualTo("id.value");
    }

    @Test
    void eventCertificateDto_mapOrdersDomainToDto_knownProperties() {
        assertThat(EventCertificateDto.mapOrdersDomainToDto(Sort.Order.asc("id.value"))).isEqualTo("id");
        assertThat(EventCertificateDto.mapOrdersDomainToDto(Sort.Order.asc("name.value"))).isEqualTo("name");
        assertThat(EventCertificateDto.mapOrdersDomainToDto(Sort.Order.asc("other"))).isEqualTo("id");
    }

    // -------------------------------------------------------------------------
    // SplitTimeTableMetadataDto
    // -------------------------------------------------------------------------

    @Test
    void splitTimeTableMetadataDto_from_mapsCorrectly() {
        de.jobst.resulter.domain.analysis.SplitTimeTableMetadata metadata =
                new de.jobst.resulter.domain.analysis.SplitTimeTableMetadata(10, 8, 15, true, 3600.0);
        SplitTimeTableMetadataDto dto = SplitTimeTableMetadataDto.from(metadata);
        assertThat(dto.totalRunners()).isEqualTo(10);
        assertThat(dto.runnersWithCompleteSplits()).isEqualTo(8);
        assertThat(dto.totalControls()).isEqualTo(15);
        assertThat(dto.reliableData()).isTrue();
        assertThat(dto.winnerTime()).isEqualTo(3600.0);
    }

    // -------------------------------------------------------------------------
    // ResultListDto
    // -------------------------------------------------------------------------

    @Test
    void resultListDto_accessorsReturnCorrectValues() {
        ResultListDto dto = new ResultListDto(1L, 2L, 3L, "creator", "2025-01-01", "Active",
                List.of(), false, false, false);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.eventId()).isEqualTo(2L);
        assertThat(dto.raceId()).isEqualTo(3L);
        assertThat(dto.creator()).isEqualTo("creator");
        assertThat(dto.classResults()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // CupDetailedDto
    // -------------------------------------------------------------------------

    @Test
    void cupDetailedDto_accessorsReturnCorrectValues() {
        CupDetailedDto dto = new CupDetailedDto(5L, "NOR Cup", null, List.of(), List.of(),
                List.of(), List.of(), java.util.Map.of(), null);
        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.name()).isEqualTo("NOR Cup");
        assertThat(dto.events()).isEmpty();
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

    // -------------------------------------------------------------------------
    // PersonDto — mapOrdersDtoToDomain / mapOrdersDomainToDto
    // -------------------------------------------------------------------------

    @Test
    void personDto_mapOrdersDtoToDomain_id() {
        assertThat(PersonDto.mapOrdersDtoToDomain(Sort.Order.asc("id"))).isEqualTo("id.value");
    }

    @Test
    void personDto_mapOrdersDtoToDomain_familyName() {
        assertThat(PersonDto.mapOrdersDtoToDomain(Sort.Order.asc("familyName"))).isEqualTo("personName.familyName.value");
    }

    @Test
    void personDto_mapOrdersDtoToDomain_givenName() {
        assertThat(PersonDto.mapOrdersDtoToDomain(Sort.Order.asc("givenName"))).isEqualTo("personName.givenName.value");
    }

    @Test
    void personDto_mapOrdersDtoToDomain_gender() {
        assertThat(PersonDto.mapOrdersDtoToDomain(Sort.Order.asc("gender"))).isEqualTo("gender.id");
    }

    @Test
    void personDto_mapOrdersDtoToDomain_birthDate() {
        assertThat(PersonDto.mapOrdersDtoToDomain(Sort.Order.asc("birthDate"))).isEqualTo("birthDate.value");
    }

    @Test
    void personDto_mapOrdersDtoToDomain_default() {
        assertThat(PersonDto.mapOrdersDtoToDomain(Sort.Order.asc("unknown"))).isEqualTo("id.value");
    }

    @Test
    void personDto_mapOrdersDomainToDto_idValue() {
        assertThat(PersonDto.mapOrdersDomainToDto(Sort.Order.asc("id.value"))).isEqualTo("id");
    }

    @Test
    void personDto_mapOrdersDomainToDto_familyName() {
        assertThat(PersonDto.mapOrdersDomainToDto(Sort.Order.asc("personName.familyName.value"))).isEqualTo("familyName");
    }

    @Test
    void personDto_mapOrdersDomainToDto_givenName() {
        assertThat(PersonDto.mapOrdersDomainToDto(Sort.Order.asc("personName.givenName.value"))).isEqualTo("givenName");
    }

    @Test
    void personDto_mapOrdersDomainToDto_gender() {
        assertThat(PersonDto.mapOrdersDomainToDto(Sort.Order.asc("gender.id"))).isEqualTo("gender");
    }

    @Test
    void personDto_mapOrdersDomainToDto_birthDate() {
        assertThat(PersonDto.mapOrdersDomainToDto(Sort.Order.asc("birthDate.value"))).isEqualTo("birthDate");
    }

    @Test
    void personDto_mapOrdersDomainToDto_default() {
        assertThat(PersonDto.mapOrdersDomainToDto(Sort.Order.asc("unknown"))).isEqualTo("id");
    }

    // -------------------------------------------------------------------------
    // MediaFileDto — mapOrdersDtoToDomain / mapOrdersDomainToDto
    // -------------------------------------------------------------------------

    @Test
    void mediaFileDto_mapOrdersDtoToDomain_returnsProperty() {
        assertThat(MediaFileDto.mapOrdersDtoToDomain(Sort.Order.asc("fileName"))).isEqualTo("fileName");
    }

    @Test
    void mediaFileDto_mapOrdersDomainToDto_returnsProperty() {
        assertThat(MediaFileDto.mapOrdersDomainToDto(Sort.Order.asc("fileName"))).isEqualTo("fileName");
    }

    // -------------------------------------------------------------------------
    // SequenceRunnerSplitDto.from — formatTime / formatTimeBehind Branches
    // -------------------------------------------------------------------------

    @Test
    void sequenceRunnerSplitDto_from_withNullSplitTime_usesEmptyString() {
        SequenceRunnerSplit split = new SequenceRunnerSplit(
                PersonId.of(1L), "H21", 1, null, null, null);
        SequenceRunnerSplitDto dto = SequenceRunnerSplitDto.from(split);
        assertThat(dto.splitTime()).isEmpty();
        assertThat(dto.timeBehind()).isEmpty();
        assertThat(dto.legSplitTimes()).isEmpty();
    }

    @Test
    void sequenceRunnerSplitDto_from_withSplitTimeAndTimeBehind_formatsCorrectly() {
        SequenceRunnerSplit split = new SequenceRunnerSplit(
                PersonId.of(2L), "D21", 2, 125.0, 30.0, List.of(60.0, 65.0));
        SequenceRunnerSplitDto dto = SequenceRunnerSplitDto.from(split);
        assertThat(dto.splitTime()).isEqualTo("2:05");
        assertThat(dto.timeBehind()).isEqualTo("+0:30");
        assertThat(dto.legSplitTimesSeconds()).containsExactly(60.0, 65.0);
    }

    @Test
    void sequenceRunnerSplitDto_from_withZeroTimeBehind_usesEmptyString() {
        SequenceRunnerSplit split = new SequenceRunnerSplit(
                PersonId.of(3L), "H14", 1, 60.0, 0.0, List.of());
        SequenceRunnerSplitDto dto = SequenceRunnerSplitDto.from(split);
        assertThat(dto.timeBehind()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // RunnerSplitDto.from — formatTime / formatTimeBehind Branches
    // -------------------------------------------------------------------------

    @Test
    void runnerSplitDto_from_withNullSplitTime_usesEmptyString() {
        RunnerSplit split = new RunnerSplit(PersonId.of(1L), "H21", 1, null, null, false);
        RunnerSplitDto dto = RunnerSplitDto.from(split);
        assertThat(dto.splitTime()).isEmpty();
        assertThat(dto.timeBehind()).isEmpty();
    }

    @Test
    void runnerSplitDto_from_withSplitTimeAndBehind_formatsCorrectly() {
        RunnerSplit split = new RunnerSplit(PersonId.of(2L), "D21", 3, 90.0, 15.0, true);
        RunnerSplitDto dto = RunnerSplitDto.from(split);
        assertThat(dto.splitTime()).isEqualTo("1:30");
        assertThat(dto.timeBehind()).isEqualTo("+0:15");
        assertThat(dto.reversed()).isTrue();
    }

    @Test
    void runnerSplitDto_from_withZeroTimeBehind_usesEmptyString() {
        RunnerSplit split = new RunnerSplit(PersonId.of(3L), "H14", 1, 45.0, 0.0, false);
        RunnerSplitDto dto = RunnerSplitDto.from(split);
        assertThat(dto.timeBehind()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // ControlSegmentDto.from — unidirectional / bidirectional
    // -------------------------------------------------------------------------

    @Test
    void controlSegmentDto_from_unidirectional_usesArrow() {
        ControlSegment segment = new ControlSegment(
                ControlCode.of("31"), ControlCode.of("32"), List.of(), List.of("H21"), false);
        ControlSegmentDto dto = ControlSegmentDto.from(segment);
        assertThat(dto.segmentLabel()).isEqualTo("31 → 32");
        assertThat(dto.bidirectional()).isFalse();
        assertThat(dto.runnerSplits()).isEmpty();
    }

    @Test
    void controlSegmentDto_from_bidirectional_usesDoubleArrow() {
        ControlSegment segment = new ControlSegment(
                ControlCode.of("31"), ControlCode.of("32"), List.of(), List.of(), true);
        ControlSegmentDto dto = ControlSegmentDto.from(segment);
        assertThat(dto.segmentLabel()).isEqualTo("31 ↔ 32");
        assertThat(dto.bidirectional()).isTrue();
    }

    // -------------------------------------------------------------------------
    // CupDto — mapOrdersDtoToDomain / mapOrdersDomainToDto
    // -------------------------------------------------------------------------

    @Test
    void cupDto_mapOrdersDtoToDomain_id() {
        assertThat(CupDto.mapOrdersDtoToDomain(Sort.Order.asc("id"))).isEqualTo("id.value");
    }

    @Test
    void cupDto_mapOrdersDtoToDomain_name() {
        assertThat(CupDto.mapOrdersDtoToDomain(Sort.Order.asc("name"))).isEqualTo("name.value");
    }

    @Test
    void cupDto_mapOrdersDtoToDomain_year() {
        assertThat(CupDto.mapOrdersDtoToDomain(Sort.Order.asc("year"))).isEqualTo("year");
    }

    @Test
    void cupDto_mapOrdersDtoToDomain_default() {
        assertThat(CupDto.mapOrdersDtoToDomain(Sort.Order.asc("unknown"))).isEqualTo("id.value");
    }

    @Test
    void cupDto_mapOrdersDomainToDto_idValue() {
        assertThat(CupDto.mapOrdersDomainToDto(Sort.Order.asc("id.value"))).isEqualTo("id");
    }

    @Test
    void cupDto_mapOrdersDomainToDto_nameValue() {
        assertThat(CupDto.mapOrdersDomainToDto(Sort.Order.asc("name.value"))).isEqualTo("name");
    }

    @Test
    void cupDto_mapOrdersDomainToDto_year() {
        assertThat(CupDto.mapOrdersDomainToDto(Sort.Order.asc("year"))).isEqualTo("year");
    }

    @Test
    void cupDto_mapOrdersDomainToDto_default() {
        assertThat(CupDto.mapOrdersDomainToDto(Sort.Order.asc("unknown"))).isEqualTo("id");
    }

    // -------------------------------------------------------------------------
    // ControlSequenceSegmentDto
    // -------------------------------------------------------------------------

    @Test
    void controlSequenceSegmentDto_from_withEmptyRunnerSplits() {
        ControlSequenceSegment segment = new ControlSequenceSegment(
                List.of(ControlCode.of("31"), ControlCode.of("32"), ControlCode.of("33")),
                List.of(),
                List.of("H21"));
        ControlSequenceSegmentDto dto = ControlSequenceSegmentDto.from(segment);
        assertThat(dto.segmentLabel()).isEqualTo("31 → 32 → 33");
        assertThat(dto.controls()).containsExactly("31", "32", "33");
        assertThat(dto.runnerSplits()).isEmpty();
        assertThat(dto.classes()).containsExactly("H21");
    }

    // -------------------------------------------------------------------------
    // ResultListKeyDto
    // -------------------------------------------------------------------------

    @Test
    void resultListKeyDto_from_mapsAllFields() {
        PersonRaceResult prr = PersonRaceResult.of(
                "H21", 1L, null, null, 100.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult person = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(prr));
        ClassResult cr = ClassResult.of("Herren 21", "H21", Gender.M, List.of(person), null);
        ResultList resultList = new ResultList(
                ResultListId.of(5L), EventId.of(1L), RaceId.of(2L), "test", null, null, List.of(cr));
        Event event = Event.of(1L, "Sprint");
        Race race = Race.of(RaceId.of(2L), EventId.of(1L), "Sprint", (byte) 1);

        ResultListKeyDto dto = ResultListKeyDto.from(resultList, event, race);
        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.event().id()).isEqualTo(1L);
        assertThat(dto.race().id()).isEqualTo(2L);
    }

    // -------------------------------------------------------------------------
    // SplitTimeTableCellDto
    // -------------------------------------------------------------------------

    @Test
    void splitTimeTableCellDto_from_mapsAllFields() {
        SplitTimeTableCell cell = new SplitTimeTableCell(
                "101", 120.0, 1, 30.0, 2, false, ErrorSeverity.NONE, null, true, false);
        SplitTimeTableCellDto dto = SplitTimeTableCellDto.from(cell);
        assertThat(dto.controlCode()).isEqualTo("101");
        assertThat(dto.cumulativeTime()).isEqualTo(120.0);
        assertThat(dto.cumulativePosition()).isEqualTo(1);
        assertThat(dto.segmentTime()).isEqualTo(30.0);
        assertThat(dto.segmentPosition()).isEqualTo(2);
        assertThat(dto.isError()).isFalse();
        assertThat(dto.errorSeverity()).isEqualTo("NONE");
        assertThat(dto.errorMagnitude()).isNull();
        assertThat(dto.isBestCumulative()).isTrue();
        assertThat(dto.isBestSegment()).isFalse();
    }

    @Test
    void splitTimeTableCellDto_from_withError() {
        SplitTimeTableCell cell = new SplitTimeTableCell(
                "102", 200.0, 3, 50.0, 5, true, ErrorSeverity.HIGH, 0.35, false, false);
        SplitTimeTableCellDto dto = SplitTimeTableCellDto.from(cell);
        assertThat(dto.isError()).isTrue();
        assertThat(dto.errorSeverity()).isEqualTo("HIGH");
        assertThat(dto.errorMagnitude()).isEqualTo(0.35);
    }

    // -------------------------------------------------------------------------
    // SplitTimeTableRowDto
    // -------------------------------------------------------------------------

    @Test
    void splitTimeTableRowDto_from_mapsAllFields() {
        SplitTimeTableCell cell = new SplitTimeTableCell(
                "F", 300.0, 1, 60.0, 1, false, ErrorSeverity.NONE, null, true, true);
        SplitTimeTableRow row = new SplitTimeTableRow(
                42L, "Müller, Hans", "H21E", List.of(cell), false, false, 300.0, 1);
        SplitTimeTableRowDto dto = SplitTimeTableRowDto.from(row);
        assertThat(dto.personId()).isEqualTo(42L);
        assertThat(dto.personName()).isEqualTo("Müller, Hans");
        assertThat(dto.className()).isEqualTo("H21E");
        assertThat(dto.cells()).hasSize(1);
        assertThat(dto.hasIncompleteSplits()).isFalse();
        assertThat(dto.notCompeting()).isFalse();
        assertThat(dto.finishTime()).isEqualTo(300.0);
        assertThat(dto.position()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // SplitTimeTableMetadataDto
    // -------------------------------------------------------------------------

    @Test
    void splitTimeTableMetadataDto_from_mapsAllFields() {
        SplitTimeTableMetadata meta = new SplitTimeTableMetadata(10, 8, 15, true, 180.0);
        SplitTimeTableMetadataDto dto = SplitTimeTableMetadataDto.from(meta);
        assertThat(dto.totalRunners()).isEqualTo(10);
        assertThat(dto.runnersWithCompleteSplits()).isEqualTo(8);
        assertThat(dto.totalControls()).isEqualTo(15);
        assertThat(dto.reliableData()).isTrue();
        assertThat(dto.winnerTime()).isEqualTo(180.0);
    }

    // -------------------------------------------------------------------------
    // SplitTimeTableDto
    // -------------------------------------------------------------------------

    @Test
    void splitTimeTableDto_from_mapsAllFields() {
        SplitTimeTableMetadata meta = new SplitTimeTableMetadata(5, 5, 10, true, 200.0);
        SplitTimeTable table = new SplitTimeTable(
                "CLASS", "H21E", List.of("H21E"), List.of("S", "101", "F"), List.of(), meta);
        SplitTimeTableDto dto = SplitTimeTableDto.from(table);
        assertThat(dto.groupByType()).isEqualTo("CLASS");
        assertThat(dto.groupId()).isEqualTo("H21E");
        assertThat(dto.groupNames()).containsExactly("H21E");
        assertThat(dto.controlCodes()).containsExactly("S", "101", "F");
        assertThat(dto.rows()).isEmpty();
        assertThat(dto.metadata().totalRunners()).isEqualTo(5);
    }

    @Test
    void mriStatisticsDto_from_mapsAllFields() {
        MriStatistics stats = new MriStatistics(10, 4, 8, 2, 3, 3, 0.1, 0.05);
        MriStatisticsDto dto = MriStatisticsDto.from(stats);
        assertThat(dto.totalRunners()).isEqualTo(10);
        assertThat(dto.runnersWithMistakes()).isEqualTo(4);
        assertThat(dto.totalMistakes()).isEqualTo(8);
        assertThat(dto.panicReactions()).isEqualTo(2);
        assertThat(dto.iceManReactions()).isEqualTo(3);
        assertThat(dto.resignerReactions()).isEqualTo(3);
        assertThat(dto.averageMRI()).isEqualTo(0.1);
        assertThat(dto.medianMRI()).isEqualTo(0.05);
    }

    @Test
    void hangingStatisticsDto_from_mapsAllFields() {
        HangingStatistics stats = new HangingStatistics(20, 5, 12, 2, 3, 1.5, 1.2);
        HangingStatisticsDto dto = HangingStatisticsDto.from(stats);
        assertThat(dto.totalRunners()).isEqualTo(20);
        assertThat(dto.runnersWithHanging()).isEqualTo(5);
        assertThat(dto.totalHangingSegments()).isEqualTo(12);
        assertThat(dto.highHangingRunners()).isEqualTo(2);
        assertThat(dto.moderateHangingRunners()).isEqualTo(3);
        assertThat(dto.averageHangingIndex()).isEqualTo(1.5);
        assertThat(dto.medianHangingIndex()).isEqualTo(1.2);
    }

    @Test
    void mentalResilienceAnalysisDto_from_mapsAllFields() {
        MriStatistics stats = new MriStatistics(5, 2, 3, 1, 1, 0, null, null);
        MentalResilienceAnalysis analysis = new MentalResilienceAnalysis(
                ResultListId.of(7L), EventId.of(3L), List.of(), stats);
        MentalResilienceAnalysisDto dto = MentalResilienceAnalysisDto.from(analysis);
        assertThat(dto.resultListId()).isEqualTo(7L);
        assertThat(dto.eventId()).isEqualTo(3L);
        assertThat(dto.runnerProfiles()).isEmpty();
        assertThat(dto.statistics().totalRunners()).isEqualTo(5);
    }

    @Test
    void hangingAnalysisDto_from_mapsAllFields() {
        HangingStatistics stats = new HangingStatistics(8, 3, 6, 1, 2, null, null);
        HangingAnalysis analysis = new HangingAnalysis(
                ResultListId.of(11L), EventId.of(4L), List.of(), stats);
        HangingAnalysisDto dto = HangingAnalysisDto.from(analysis);
        assertThat(dto.resultListId()).isEqualTo(11L);
        assertThat(dto.eventId()).isEqualTo(4L);
        assertThat(dto.runnerProfiles()).isEmpty();
        assertThat(dto.statistics().runnersWithHanging()).isEqualTo(3);
    }
}
