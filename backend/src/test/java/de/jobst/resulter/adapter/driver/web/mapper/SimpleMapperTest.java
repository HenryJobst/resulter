package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.domain.*;
import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.adapter.driver.web.dto.ResultListDto;
import de.jobst.resulter.domain.aggregations.CupOverallStatistics;
import de.jobst.resulter.domain.aggregations.CupStatistics;
import de.jobst.resulter.domain.aggregations.EventRacesCupScore;
import de.jobst.resulter.domain.aggregations.OrganisationStatistics;
import de.jobst.resulter.domain.aggregations.RaceClassResultGroupedCupScore;
import de.jobst.resulter.domain.aggregations.RaceOrganisationGroupedCupScore;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class SimpleMapperTest {

    // -------------------------------------------------------------------------
    // OrganisationTypeMapper
    // -------------------------------------------------------------------------

    @Test
    void organisationTypeMapper_toDto_mapsValue() {
        var dto = OrganisationTypeMapper.toDto(OrganisationType.CLUB);
        assertThat(dto.id()).isEqualTo("Club");
    }

    // -------------------------------------------------------------------------
    // CountryMapper
    // -------------------------------------------------------------------------

    @Test
    void countryMapper_toKeyDto_withId_usesId() {
        Country country = Country.of(5L, "DE", "Deutschland");
        var dto = CountryMapper.toKeyDto(country);
        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.name()).isEqualTo("Deutschland");
    }

    @Test
    void countryMapper_toKeyDto_withEmptyId_usesZero() {
        Country country = Country.of(null, "AT", "Österreich");
        var dto = CountryMapper.toKeyDto(country);
        assertThat(dto.id()).isEqualTo(0L);
        assertThat(dto.name()).isEqualTo("Österreich");
    }

    // -------------------------------------------------------------------------
    // CourseMapper
    // -------------------------------------------------------------------------

    @Test
    void courseMapper_toDto_mapsAllFields() {
        Course course = Course.of(EventId.of(1L), "Kurz", 3.2, 120.0, 15);
        var dto = CourseMapper.toDto(course);
        assertThat(dto.name()).isEqualTo("Kurz");
        assertThat(dto.length()).isEqualTo(3.2);
        assertThat(dto.climb()).isEqualTo(120.0);
        assertThat(dto.controls()).isEqualTo(15);
    }

    @Test
    void courseMapper_toDtos_returnsListOfDtos() {
        Course c1 = Course.of(EventId.of(1L), "Lang", 8.0, 300.0, 25);
        Course c2 = Course.of(EventId.of(1L), "Mittel", 5.0, 200.0, 18);
        var dtos = CourseMapper.toDtos(List.of(c1, c2));
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).name()).isEqualTo("Lang");
        assertThat(dtos.get(1).name()).isEqualTo("Mittel");
    }

    // -------------------------------------------------------------------------
    // PersonKeyMapper — leere PersonId → 0
    // -------------------------------------------------------------------------

    @Test
    void personKeyMapper_toDto_withEmptyPersonId_usesZero() {
        Person person = Person.of(null, "Schmidt", "Anna", null, Gender.F);
        var dto = PersonKeyMapper.toDto(person);
        assertThat(dto.id()).isEqualTo(0L);
        assertThat(dto.familyName()).isEqualTo("Schmidt");
        assertThat(dto.givenName()).isEqualTo("Anna");
    }

    // -------------------------------------------------------------------------
    // Private-Konstruktoren (Reflection)
    // -------------------------------------------------------------------------

    @Test
    void countryMapper_privateConstructor_isAccessible() throws Exception {
        Constructor<CountryMapper> c = CountryMapper.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertThatCode(c::newInstance).doesNotThrowAnyException();
    }

    @Test
    void personKeyMapper_privateConstructor_isAccessible() throws Exception {
        Constructor<PersonKeyMapper> c = PersonKeyMapper.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertThatCode(c::newInstance).doesNotThrowAnyException();
    }

    // -------------------------------------------------------------------------
    // CourseMapper — Default-Konstruktor
    // -------------------------------------------------------------------------

    @Test
    void courseMapper_canBeInstantiated() {
        assertThat(new CourseMapper()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // EventCertificateStatMapper — Default-Konstruktor
    // -------------------------------------------------------------------------

    @Test
    void eventCertificateStatMapper_canBeInstantiated() {
        assertThat(new EventCertificateStatMapper()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // PersonWithScoreMapper
    // -------------------------------------------------------------------------

    @Test
    void personWithScoreMapper_toDto_mapsCorrectly() {
        PersonWithScore pws = new PersonWithScore(PersonId.of(7L), 8.5, ClassResultShortName.of("H21"));
        var dto = PersonWithScoreMapper.toDto(pws);
        assertThat(dto.personId()).isEqualTo(7L);
        assertThat(dto.score()).isEqualTo(8.5);
        assertThat(dto.classShortName()).isEqualTo("H21");
    }

    @Test
    void personWithScoreMapper_toDtos_returnsList() {
        PersonWithScore pws = new PersonWithScore(PersonId.of(1L), 5.0, ClassResultShortName.of("D18"));
        var dtos = PersonWithScoreMapper.toDtos(List.of(pws));
        assertThat(dtos).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // ClassResultScoreMapper
    // -------------------------------------------------------------------------

    @Test
    void classResultScoreMapper_toDto_mapsCorrectly() {
        ClassResultScores scores = new ClassResultScores(ClassResultShortName.of("H14"), List.of());
        var dto = ClassResultScoreMapper.toDto(scores);
        assertThat(dto.classResultShortName()).isEqualTo("H14");
        assertThat(dto.personWithScores()).isEmpty();
    }

    @Test
    void classResultScoreMapper_toDtos_returnsList() {
        ClassResultScores scores = new ClassResultScores(ClassResultShortName.of("D21"), List.of());
        var dtos = ClassResultScoreMapper.toDtos(List.of(scores));
        assertThat(dtos).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // CupOverallStatisticsMapper
    // -------------------------------------------------------------------------

    @Test
    void cupOverallStatisticsMapper_toDto_mapsAllFields() {
        CupOverallStatistics stats = CupOverallStatistics.of(100, 10, 200, 20);
        var dto = CupOverallStatisticsMapper.toDto(stats);
        assertThat(dto.totalRunners()).isEqualTo(100);
        assertThat(dto.totalOrganisations()).isEqualTo(10);
        assertThat(dto.totalStarts()).isEqualTo(200);
        assertThat(dto.totalNonScoringStarts()).isEqualTo(20);
    }

    // -------------------------------------------------------------------------
    // CupStatisticsMapper
    // -------------------------------------------------------------------------

    @Test
    void cupStatisticsMapper_toDto_mapsCorrectly() {
        CupOverallStatistics overall = CupOverallStatistics.of(50, 5, 100, 5);
        CupStatistics stats = new CupStatistics(overall, List.of());
        var dto = CupStatisticsMapper.toDto(stats, Map.of(), Map.of());
        assertThat(dto.overallStatistics()).isNotNull();
        assertThat(dto.organisationStatistics()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // RaceClassResultGroupedCupScoreMapper
    // -------------------------------------------------------------------------

    @Test
    void raceClassResultGroupedCupScoreMapper_toDto_withEmptyList() {
        Race race = Race.of(EventId.of(1L), "Sprint", (byte) 1);
        RaceClassResultGroupedCupScore grouped = new RaceClassResultGroupedCupScore(race, List.of());
        var dto = RaceClassResultGroupedCupScoreMapper.toDto(grouped);
        assertThat(dto.classResultScores()).isEmpty();
    }

    @Test
    void raceClassResultGroupedCupScoreMapper_toDto_withNullList() {
        Race race = Race.of(EventId.of(1L), "Mittel", (byte) 2);
        RaceClassResultGroupedCupScore grouped = new RaceClassResultGroupedCupScore(race, null);
        var dto = RaceClassResultGroupedCupScoreMapper.toDto(grouped);
        assertThat(dto.classResultScores()).isEmpty();
    }

    @Test
    void raceClassResultGroupedCupScoreMapper_toDtos_returnsList() {
        Race race = Race.of(EventId.of(1L), "Lang", (byte) 1);
        var dtos = RaceClassResultGroupedCupScoreMapper.toDtos(
                List.of(new RaceClassResultGroupedCupScore(race, List.of())));
        assertThat(dtos).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // CupStatisticsMapper — Default-Konstruktor
    // -------------------------------------------------------------------------

    @Test
    void cupStatisticsMapper_canBeInstantiated() {
        assertThat(new CupStatisticsMapper()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // RaceOrganisationGroupedCupScoreMapper
    // -------------------------------------------------------------------------

    @Test
    void raceOrganisationGroupedCupScoreMapper_toDto_withEmptyList() {
        Race race = Race.of(EventId.of(1L), "Sprint", (byte) 1);
        RaceOrganisationGroupedCupScore grouped = new RaceOrganisationGroupedCupScore(race, List.of());
        var dto = RaceOrganisationGroupedCupScoreMapper.toDto(grouped, Map.of(), Map.of());
        assertThat(dto.organisationScores()).isEmpty();
    }

    @Test
    void raceOrganisationGroupedCupScoreMapper_toDto_withNullList() {
        Race race = Race.of(EventId.of(1L), "Mittel", (byte) 2);
        RaceOrganisationGroupedCupScore grouped = new RaceOrganisationGroupedCupScore(race, null);
        var dto = RaceOrganisationGroupedCupScoreMapper.toDto(grouped, Map.of(), Map.of());
        assertThat(dto.organisationScores()).isEmpty();
    }

    @Test
    void raceOrganisationGroupedCupScoreMapper_canBeInstantiated() {
        assertThat(new RaceOrganisationGroupedCupScoreMapper()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // EventResultsMapper — Default-Konstruktor
    // -------------------------------------------------------------------------

    @Test
    void eventResultsMapper_canBeInstantiated() {
        assertThat(new EventResultsMapper()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // RaceMapper — Default-Konstruktor + true-branch (Race mit gesetzter Id)
    // -------------------------------------------------------------------------

    @Test
    void raceMapper_canBeInstantiated() {
        assertThat(new RaceMapper()).isNotNull();
    }

    @Test
    void raceMapper_toDtoStatic_withNonEmptyId_usesId() {
        Race race = Race.of(RaceId.of(3L), EventId.of(1L), "Lang", (byte) 1);
        var dto = RaceMapper.toDtoStatic(race);
        assertThat(dto.id()).isEqualTo(3L);
        assertThat(dto.name()).isEqualTo("Lang");
    }

    @Test
    void raceMapper_toDtos_returnsList() {
        Race race = Race.of(EventId.of(1L), "Sprint", (byte) 1);
        var dtos = RaceMapper.toDtos(List.of(race));
        assertThat(dtos).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // OrganisationMapper
    // -------------------------------------------------------------------------

    @Test
    void organisationMapper_privateConstructor_isAccessible() throws Exception {
        Constructor<OrganisationMapper> c = OrganisationMapper.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertThatCode(c::newInstance).doesNotThrowAnyException();
    }

    @Test
    void organisationMapper_toDtos_returnsList() {
        Organisation org = Organisation.of("TSB OJ", "TSB");
        var dtos = OrganisationMapper.toDtos(List.of(org), Map.of(), Map.of());
        assertThat(dtos).hasSize(1);
    }

    @Test
    void organisationMapper_toKeyDto_mapsNameAndId() {
        Organisation org = Organisation.of(7L, "OLOV", "O");
        var dto = OrganisationMapper.toKeyDto(org);
        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.name()).isEqualTo("OLOV");
    }

    @Test
    void organisationMapper_toDto_withoutCountry() {
        Organisation org = Organisation.of("TSB OJ", "TSB");
        var dto = OrganisationMapper.toDto(org, Map.of(), Map.of());
        assertThat(dto.name()).isEqualTo("TSB OJ");
        assertThat(dto.country()).isNull();
    }

    @Test
    void organisationMapper_toDto_withCountry() {
        Country country = Country.of(1L, "DE", "Deutschland");
        Organisation org = Organisation.of("TSB OJ", "TSB", CountryId.of(1L));
        var dto = OrganisationMapper.toDto(org, Map.of(CountryId.of(1L), country), Map.of());
        assertThat(dto.country()).isNotNull();
        assertThat(dto.country().name()).isEqualTo("Deutschland");
    }

    // -------------------------------------------------------------------------
    // OrganisationStatisticsMapper — Default-Konstruktor + Tests
    // -------------------------------------------------------------------------

    @Test
    void organisationStatisticsMapper_canBeInstantiated() {
        assertThat(new OrganisationStatisticsMapper()).isNotNull();
    }

    @Test
    void organisationStatisticsMapper_toDto_mapsCorrectly() {
        Organisation org = Organisation.of("OLOV", "O");
        OrganisationStatistics stats = OrganisationStatistics.of(org, 10, 20, 2);
        var dto = OrganisationStatisticsMapper.toDto(stats, Map.of(), Map.of());
        assertThat(dto.runnerCount()).isEqualTo(10);
        assertThat(dto.totalStarts()).isEqualTo(20);
    }

    @Test
    void organisationStatisticsMapper_toDtos_returnsList() {
        Organisation org = Organisation.of("OLOV", "O");
        OrganisationStatistics stats = OrganisationStatistics.of(org, 5, 10, 1);
        var dtos = OrganisationStatisticsMapper.toDtos(List.of(stats), Map.of(), Map.of());
        assertThat(dtos).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // PersonResultMapper
    // -------------------------------------------------------------------------

    @Test
    void personResultMapper_toDto_withOrganisationAndRunTime() {
        PersonRaceResult rr = PersonRaceResult.of("H21", 1L, null, null, 1000.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult person = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(7L), OrganisationId.of(3L), List.of(rr));
        var dto = PersonResultMapper.toDto(person);
        assertThat(dto.personId()).isEqualTo(7L);
        assertThat(dto.organisationId()).isEqualTo(3L);
        assertThat(dto.runTime()).isNotNull();
    }

    @Test
    void personResultMapper_toDto_withoutOrganisationAndNullRunTime() {
        PersonRaceResult rr = PersonRaceResult.of("H21", 1L, null, null, null, null, (byte) 1, ResultStatus.DID_NOT_START);
        PersonResult person = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(5L), null, List.of(rr));
        var dto = PersonResultMapper.toDto(person);
        assertThat(dto.organisationId()).isNull();
        assertThat(dto.runTime()).isNull();
    }

    @Test
    void personResultMapper_toDtos_returnsList() {
        PersonRaceResult rr = PersonRaceResult.of("H21", 1L, null, null, 500.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult person = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(rr));
        var dtos = PersonResultMapper.toDtos(List.of(person));
        assertThat(dtos).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // ClassResultMapper
    // -------------------------------------------------------------------------

    @Test
    void classResultMapper_toDto_withoutCourseId() {
        ClassResult cr = ClassResult.of("Herren 21", "H21", null, List.of(), null);
        var dto = ClassResultMapper.toDto(cr);
        assertThat(dto.shortName()).isEqualTo("H21");
        assertThat(dto.courseId()).isNull();
        assertThat(dto.personResults()).isEmpty();
    }

    @Test
    void classResultMapper_toDto_withCourseId() {
        ClassResult cr = ClassResult.of("Damen 21", "D21", null, List.of(), CourseId.of(5L));
        var dto = ClassResultMapper.toDto(cr);
        assertThat(dto.courseId()).isEqualTo(5L);
    }

    // -------------------------------------------------------------------------
    // OrganisationScoreMapper — Default-Konstruktor + Tests
    // -------------------------------------------------------------------------

    @Test
    void organisationScoreMapper_canBeInstantiated() {
        assertThat(new OrganisationScoreMapper()).isNotNull();
    }

    @Test
    void organisationScoreMapper_toDto_withEmptyPersonList() {
        Organisation org = Organisation.of("TSB", "T");
        de.jobst.resulter.domain.aggregations.OrganisationScore score =
                new de.jobst.resulter.domain.aggregations.OrganisationScore(org, 42.0, List.of());
        var dto = OrganisationScoreMapper.toDto(score, Map.of(), Map.of());
        assertThat(dto.score()).isEqualTo(42.0);
        assertThat(dto.personWithScores()).isEmpty();
    }

    @Test
    void organisationScoreMapper_toDtos_returnsList() {
        Organisation org = Organisation.of("OLOV", "O");
        de.jobst.resulter.domain.aggregations.OrganisationScore score =
                new de.jobst.resulter.domain.aggregations.OrganisationScore(org, 10.0, List.of());
        var dtos = OrganisationScoreMapper.toDtos(List.of(score), Map.of(), Map.of());
        assertThat(dtos).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // CupMapper
    // -------------------------------------------------------------------------

    @Test
    void cupMapper_canBeInstantiated() {
        assertThat(new CupMapper()).isNotNull();
    }

    @Test
    void cupMapper_toDto_withId() {
        Cup cup = Cup.of(7L, "NOR Cup", CupType.NOR, java.time.Year.of(2025), List.of());
        var dto = CupMapper.toDto(cup, Map.of());
        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.name()).isEqualTo("NOR Cup");
        assertThat(dto.events()).isEmpty();
    }

    @Test
    void cupMapper_toDto_withoutId() {
        Cup cup = Cup.of(null, "KJ Cup", CupType.KJ, java.time.Year.of(2025), List.of());
        var dto = CupMapper.toDto(cup, Map.of());
        assertThat(dto.id()).isEqualTo(0L);
    }

    @Test
    void cupMapper_toDtos_returnsList() {
        Cup cup = Cup.of(1L, "Test", CupType.NOR, java.time.Year.of(2025), List.of());
        var dtos = CupMapper.toDtos(List.of(cup), Map.of());
        assertThat(dtos).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // EventRacesCupScoreMapper
    // -------------------------------------------------------------------------

    @Test
    void eventRacesCupScoreMapper_canBeInstantiated() {
        assertThat(new EventRacesCupScoreMapper()).isNotNull();
    }

    @Test
    void eventRacesCupScoreMapper_toDto_withEmptyLists() {
        Event event = Event.of(1L, "Sprint");
        EventDto eventDto = new EventDto(1L, "Sprint", null, null, List.of(), null, false, null, false);
        EventRacesCupScore score = new EventRacesCupScore(event, List.of(), List.of());
        var dto = EventRacesCupScoreMapper.toDto(score, eventDto, Map.of(), Map.of());
        assertThat(dto.raceOrganisationGroupedCupScores()).isEmpty();
        assertThat(dto.raceClassResultGroupedCupScores()).isEmpty();
    }

    @Test
    void eventRacesCupScoreMapper_toDtos_filtersUnknownEvents() {
        Event event = Event.of(1L, "Lauf");
        EventRacesCupScore score = new EventRacesCupScore(event, List.of(), List.of());
        // leere eventDtosById → wird herausgefiltert
        var dtos = EventRacesCupScoreMapper.toDtos(List.of(score), Map.of(), Map.of(), Map.of());
        assertThat(dtos).isEmpty();
    }

    // -------------------------------------------------------------------------
    // EventMapper — toKeyDto + toDto + toDtos
    // -------------------------------------------------------------------------

    @Test
    void eventMapper_toKeyDto_mapsCorrectly() {
        Event event = Event.of(42L, "Stadtlauf");
        var dto = EventMapper.toKeyDto(event);
        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.name()).isEqualTo("Stadtlauf");
    }

    @Test
    void eventMapper_toKeyDto_withNullId_usesZero() {
        Event event = Event.of(null, "Test");
        var dto = EventMapper.toKeyDto(event);
        assertThat(dto.id()).isEqualTo(0L);
    }

    @Test
    void eventMapper_toDto_withMinimalEvent() {
        Event event = Event.of(1L, "Sprint");
        var dto = EventMapper.toDto(event, Map.of(), Map.of(), false);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Sprint");
        assertThat(dto.startTime()).isNull();
        assertThat(dto.certificate()).isNull();
    }

    @Test
    void eventMapper_toDtos_returnsList() {
        Event event = Event.of(1L, "Lauf");
        var dtos = EventMapper.toDtos(List.of(event), Map.of(EventId.of(1L), false), Map.of(), Map.of());
        assertThat(dtos).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // EventCertificateMapper
    // -------------------------------------------------------------------------

    @Test
    void eventCertificateMapper_toDto_withNoEventAndNoMediaFile() {
        EventCertificate cert = EventCertificate.of(1L, "Urkunde", null, null, null, true);
        var dto = EventCertificateMapper.toDto(cert, Map.of(), Map.of(), "/tmp/");
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Urkunde");
        assertThat(dto.event()).isNull();
        assertThat(dto.blankCertificate()).isNull();
        assertThat(dto.primary()).isTrue();
    }

    @Test
    void eventCertificateMapper_toDto_withEvent() {
        Event event = Event.of(5L, "Stadtlauf");
        EventCertificate cert = EventCertificate.of(2L, "Zertifikat", EventId.of(5L), null, null, false);
        var dto = EventCertificateMapper.toDto(cert, Map.of(EventId.of(5L), event), Map.of(), "/tmp/");
        assertThat(dto.event()).isNotNull();
        assertThat(dto.event().id()).isEqualTo(5L);
    }

    @Test
    void eventCertificateMapper_toDtos_returnsList() {
        EventCertificate cert = EventCertificate.of(1L, "Test", null, null, null, false);
        var dtos = EventCertificateMapper.toDtos(List.of(cert), Map.of(), Map.of(), "/tmp/");
        assertThat(dtos).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // EventResultsMapper — toDto (exercising ResultListMapper.toDto)
    // -------------------------------------------------------------------------

    @Test
    void eventResultsMapper_toDto_withSingleResultList() {
        PersonRaceResult prr = PersonRaceResult.of(
                "H21", 1L, null, null, 120.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult person = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(prr));
        ClassResult cr = ClassResult.of("Herren 21", "H21", Gender.M, List.of(person), null);
        ResultList resultList = new ResultList(
                ResultListId.of(1L), EventId.of(1L), RaceId.of(1L),
                "test", null, null, List.of(cr));
        Event event = Event.of(1L, "Sprint");

        var dto = EventResultsMapper.toDto(event, false, List.of(resultList));
        assertThat(dto.resultLists()).hasSize(1);
        ResultListDto rlDto = dto.resultLists().stream().findFirst().orElseThrow();
        assertThat(rlDto.id()).isEqualTo(1L);
        assertThat(rlDto.isSplitTimeAvailable()).isFalse();
    }

    @Test
    void eventResultsMapper_toDto_withSplitTimeListId() {
        PersonRaceResult prr = new PersonRaceResult(
                ClassResultShortName.of("H21"), PersonId.of(2L),
                DateTime.empty(), DateTime.empty(),
                PunchTime.of(90.0), Position.of(1L), ResultStatus.OK,
                RaceNumber.of((byte) 1), SplitTimeListId.of(42L));
        PersonResult person = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(2L), null, List.of(prr));
        ClassResult cr = ClassResult.of("Damen 21", "D21", Gender.F, List.of(person), null);
        ResultList resultList = new ResultList(
                ResultListId.of(2L), EventId.of(1L), RaceId.of(1L),
                "test", null, null, List.of(cr));
        Event event = Event.of(1L, "Mitteldistanz");

        var dto = EventResultsMapper.toDto(event, false, List.of(resultList));
        Boolean isSplitTimeAvailable = dto.resultLists().stream().findFirst().orElseThrow().isSplitTimeAvailable();
        assertThat(isSplitTimeAvailable).isTrue();
    }

    // -------------------------------------------------------------------------
    // PersonMapper
    // -------------------------------------------------------------------------

    @Test
    void personMapper_privateConstructor_isAccessible() throws Exception {
        Constructor<PersonMapper> c = PersonMapper.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertThatCode(c::newInstance).doesNotThrowAnyException();
    }

    @Test
    void personMapper_toDto_withId_usesId() {
        Person person = Person.of(42L, "Müller", "Hans", null, Gender.M);
        var dto = PersonMapper.toDto(person);
        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.familyName()).isEqualTo("Müller");
        assertThat(dto.givenName()).isEqualTo("Hans");
        assertThat(dto.showMergeButton()).isFalse();
        assertThat(dto.birthDate()).isNull();
    }

    @Test
    void personMapper_toDto_withoutId_usesZero() {
        Person person = Person.of(null, "Schneider", "Eva", null, Gender.F);
        var dto = PersonMapper.toDto(person);
        assertThat(dto.id()).isEqualTo(0L);
    }

    @Test
    void personMapper_toDto_withBirthDate_mapsBirthDate() {
        java.time.LocalDate date = java.time.LocalDate.of(1990, 5, 15);
        Person person = Person.of(1L, "Braun", "Klaus", date, Gender.M);
        var dto = PersonMapper.toDto(person);
        assertThat(dto.birthDate()).isEqualTo(date);
    }

    @Test
    void personMapper_toDto_showMergeButton_true() {
        Person person = Person.of(3L, "Koch", "Anna", null, Gender.F);
        var dto = PersonMapper.toDto(person, true);
        assertThat(dto.showMergeButton()).isTrue();
    }

    @Test
    void personMapper_toDtos_simpleList() {
        Person p1 = Person.of(1L, "Weber", "Fritz", null, Gender.M);
        Person p2 = Person.of(2L, "Bauer", "Lena", null, Gender.F);
        var dtos = PersonMapper.toDtos(java.util.List.of(p1, p2));
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).familyName()).isEqualTo("Weber");
        assertThat(dtos.get(1).familyName()).isEqualTo("Bauer");
    }

    @Test
    void personMapper_toDtos_withShowMergeButton() {
        Person person = Person.of(5L, "Huber", "Max", null, Gender.M);
        var dtos = PersonMapper.toDtos(java.util.List.of(person), true);
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).showMergeButton()).isTrue();
    }

    @Test
    void personMapper_toDtos_withGroupLeaderIds_marksLeader() {
        Person leader = Person.of(10L, "König", "Karl", null, Gender.M);
        Person member = Person.of(20L, "Richter", "Lisa", null, Gender.F);
        var dtos = PersonMapper.toDtos(java.util.List.of(leader, member), java.util.Set.of(10L));
        assertThat(dtos.get(0).showMergeButton()).isTrue();
        assertThat(dtos.get(1).showMergeButton()).isFalse();
    }
}
