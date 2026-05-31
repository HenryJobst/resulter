package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.aggregations.CupOverallStatistics;
import de.jobst.resulter.domain.aggregations.CupStatistics;
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
    // EventMapper — toKeyDto
    // -------------------------------------------------------------------------

    @Test
    void eventMapper_toKeyDto_mapsCorrectly() {
        Event event = Event.of(42L, "Stadtlauf");
        var dto = EventMapper.toKeyDto(event);
        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.name()).isEqualTo("Stadtlauf");
    }
}
