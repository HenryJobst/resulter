package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class NORCalculationStrategyTest {

    // -------------------------------------------------------------------------
    // calculateNorPoints — Punktetabelle
    // -------------------------------------------------------------------------

    @Test
    void calculateNorPoints_shouldReturn12ForExactBestTime() {
        assertThat(NORCalculationStrategy.calculateNorPoints(1000.0, 1000.0)).isEqualTo(12);
    }

    @ParameterizedTest(name = "currentTime={1} → {2} Punkte")
    @CsvSource({
        // Exakt an der Grenze jeder Stufe (≤ Faktor × bestTime)
        "1000.0, 1050.0, 11",   // exakt 1.05× → 11
        "1000.0, 1100.0, 10",   // exakt 1.10× → 10
        "1000.0, 1150.0,  9",   // exakt 1.15× →  9
        "1000.0, 1200.0,  8",   // exakt 1.20× →  8
        "1000.0, 1250.0,  7",   // exakt 1.25× →  7
        "1000.0, 1350.0,  6",   // exakt 1.35× →  6
        "1000.0, 1500.0,  5",   // exakt 1.50× →  5
        "1000.0, 1700.0,  4",   // exakt 1.70× →  4
        "1000.0, 2000.0,  3",   // exakt 2.00× →  3
        "1000.0, 3000.0,  2",   // exakt 3.00× →  2
    })
    void calculateNorPoints_shouldReturnCorrectPointsAtExactBoundary(
            double bestTime, double currentTime, int expectedPoints) {
        assertThat(NORCalculationStrategy.calculateNorPoints(bestTime, currentTime))
                .isEqualTo(expectedPoints);
    }

    @ParameterizedTest(name = "currentTime={1} → {2} Punkte (knapp unter Grenze)")
    @CsvSource({
        "1000.0, 1049.9, 11",
        "1000.0, 1099.9, 10",
        "1000.0, 1149.9,  9",
        "1000.0, 1199.9,  8",
        "1000.0, 1249.9,  7",
        "1000.0, 1349.9,  6",
        "1000.0, 1499.9,  5",
        "1000.0, 1699.9,  4",
        "1000.0, 1999.9,  3",
        "1000.0, 2999.9,  2",
    })
    void calculateNorPoints_shouldReturnCorrectPointsJustBelowBoundary(
            double bestTime, double currentTime, int expectedPoints) {
        assertThat(NORCalculationStrategy.calculateNorPoints(bestTime, currentTime))
                .isEqualTo(expectedPoints);
    }

    @Test
    void calculateNorPoints_shouldReturn1WhenMoreThanTripleBestTime() {
        assertThat(NORCalculationStrategy.calculateNorPoints(1000.0, 3000.1)).isEqualTo(1);
        assertThat(NORCalculationStrategy.calculateNorPoints(1000.0, 9999.0)).isEqualTo(1);
    }

    @Test
    void calculateNorPoints_shouldThrowWhenCurrentTimeFasterThanBestTime() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> NORCalculationStrategy.calculateNorPoints(1000.0, 999.9));
    }

    // -------------------------------------------------------------------------
    // calculate — vollständige Score-Pipeline
    // -------------------------------------------------------------------------

    @Test
    void calculate_shouldReturnEmptyListForNoResults() {
        NORCalculationStrategy strategy = new NORCalculationStrategy(null);
        Cup cup = Cup.of(1L, "NOR Cup", CupType.NOR, Year.of(2025), List.of());

        List<CupScore> scores = strategy.calculate(cup, List.of(), Map.of());

        assertThat(scores).isEmpty();
    }

    @Test
    void calculate_shouldAssign12PointsToWinner() {
        NORCalculationStrategy strategy = new NORCalculationStrategy(null);
        Cup cup = Cup.of(1L, "NOR Cup", CupType.NOR, Year.of(2025), List.of());

        PersonRaceResult winner = PersonRaceResult.of("H21", 1L, null, null, 1000.0, 1L, (byte) 1, ResultStatus.OK);
        OrganisationId orgId = OrganisationId.of(10L);

        List<CupScore> scores = strategy.calculate(cup, List.of(winner), Map.of(PersonId.of(1L), orgId));

        assertThat(scores).hasSize(1);
        assertThat(scores.getFirst().score()).isEqualTo(12.0);
        assertThat(scores.getFirst().personId()).isEqualTo(PersonId.of(1L));
        assertThat(scores.getFirst().organisationId()).isEqualTo(orgId);
    }

    @Test
    void calculate_shouldAssignCorrectPointsToAllRunners() {
        NORCalculationStrategy strategy = new NORCalculationStrategy(null);
        Cup cup = Cup.of(1L, "NOR Cup", CupType.NOR, Year.of(2025), List.of());

        // Erster: 1000s → 12 Punkte; Zweiter: 1050s = exakt 1.05× → 11 Punkte; Dritter: 1500s = 1.50× → 5 Punkte
        PersonRaceResult first  = PersonRaceResult.of("H21", 1L, null, null, 1000.0, 1L, (byte) 1, ResultStatus.OK);
        PersonRaceResult second = PersonRaceResult.of("H21", 2L, null, null, 1050.0, 2L, (byte) 1, ResultStatus.OK);
        PersonRaceResult third  = PersonRaceResult.of("H21", 3L, null, null, 1500.0, 3L, (byte) 1, ResultStatus.OK);

        Map<PersonId, OrganisationId> orgByPerson = Map.of(
                PersonId.of(1L), OrganisationId.of(10L),
                PersonId.of(2L), OrganisationId.of(11L),
                PersonId.of(3L), OrganisationId.of(12L)
        );

        List<CupScore> scores = strategy.calculate(cup, List.of(first, second, third), orgByPerson);

        assertThat(scores).hasSize(3);
        assertThat(scores.get(0).score()).isEqualTo(12.0);
        assertThat(scores.get(1).score()).isEqualTo(11.0);
        assertThat(scores.get(2).score()).isEqualTo(5.0);
    }

    // -------------------------------------------------------------------------
    // harmonizeClassResultShortName
    // -------------------------------------------------------------------------

    @Test
    void harmonizeClassResultShortName_shouldNormalizeMainMenLongClasses() {
        NORCalculationStrategy strategy = new NORCalculationStrategy(null);

        assertThat(strategy.harmonizeClassResultShortName(ClassResultShortName.of("H21")).value()).isEqualTo("H19L");
        assertThat(strategy.harmonizeClassResultShortName(ClassResultShortName.of("H21L")).value()).isEqualTo("H19L");
        assertThat(strategy.harmonizeClassResultShortName(ClassResultShortName.of("H20")).value()).isEqualTo("H19L");
    }

    @Test
    void harmonizeClassResultShortName_shouldNormalizeMainMenShortClasses() {
        NORCalculationStrategy strategy = new NORCalculationStrategy(null);

        assertThat(strategy.harmonizeClassResultShortName(ClassResultShortName.of("H21K")).value()).isEqualTo("H19K");
        assertThat(strategy.harmonizeClassResultShortName(ClassResultShortName.of("H19K")).value()).isEqualTo("H19K");
    }

    @Test
    void harmonizeClassResultShortName_shouldNormalizeMainWomenLongClasses() {
        NORCalculationStrategy strategy = new NORCalculationStrategy(null);

        assertThat(strategy.harmonizeClassResultShortName(ClassResultShortName.of("D21")).value()).isEqualTo("D19L");
        assertThat(strategy.harmonizeClassResultShortName(ClassResultShortName.of("D21L")).value()).isEqualTo("D19L");
    }

    @Test
    void harmonizeClassResultShortName_shouldNormalizeMainWomenShortClasses() {
        NORCalculationStrategy strategy = new NORCalculationStrategy(null);

        assertThat(strategy.harmonizeClassResultShortName(ClassResultShortName.of("D21K")).value()).isEqualTo("D19K");
        assertThat(strategy.harmonizeClassResultShortName(ClassResultShortName.of("D21B")).value()).isEqualTo("D19K");
    }

    @Test
    void harmonizeClassResultShortName_shouldStripWhitespaceAndRemoveDash() {
        NORCalculationStrategy strategy = new NORCalculationStrategy(null);

        // "H19-L" enthält einen Bindestrich, der entfernt wird → "H19L" → mainClassMenLong
        assertThat(strategy.harmonizeClassResultShortName(ClassResultShortName.of(" H19-L ")).value()).isEqualTo("H19L");
    }

    @Test
    void harmonizeClassResultShortName_shouldLeaveUnknownClassUnchanged() {
        NORCalculationStrategy strategy = new NORCalculationStrategy(null);

        assertThat(strategy.harmonizeClassResultShortName(ClassResultShortName.of("D10")).value()).isEqualTo("D10");
        assertThat(strategy.harmonizeClassResultShortName(ClassResultShortName.of("HAK")).value()).isEqualTo("HAK");
    }

    // -------------------------------------------------------------------------
    // valid(ClassResult) — Klassen-Sperrfilter
    // -------------------------------------------------------------------------

    @Test
    void valid_classResult_shouldRejectSkippedClasses() {
        NORCalculationStrategy strategy = new NORCalculationStrategy(null);

        for (String skipped : List.of("BK", "BL", "Beg", "Trim", "Hasen", "Beginner",
                "OffK", "OffL", "KL", "Begleitung")) {
            assertThat(strategy.valid(classResult(skipped)))
                    .as("Klasse '%s' sollte abgelehnt werden", skipped)
                    .isFalse();
        }
    }

    @Test
    void valid_classResult_shouldAcceptRegularClasses() {
        NORCalculationStrategy strategy = new NORCalculationStrategy(null);

        assertThat(strategy.valid(classResult("H21"))).isTrue();
        assertThat(strategy.valid(classResult("D21"))).isTrue();
        assertThat(strategy.valid(classResult("H19L"))).isTrue();
    }

    // -------------------------------------------------------------------------
    // getBestOfRacesCount
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "racesCount={0} → bestOf={1}")
    @CsvSource({
        "1, 1",
        "2, 2",
        "3, 2",
        "4, 3",
        "5, 3",
        "6, 4",
        "10, 6",
    })
    void getBestOfRacesCount_shouldReturnHalfPlusOne(int racesCount, int expectedBestOf) {
        NORCalculationStrategy strategy = new NORCalculationStrategy(null);
        assertThat(strategy.getBestOfRacesCount(racesCount)).isEqualTo(expectedBestOf);
    }

    // -------------------------------------------------------------------------
    // valid(PersonResult) — Organisationszugehörigkeit prüfen
    // -------------------------------------------------------------------------

    @Test
    void valid_personResult_throwsWhenOrganisationByIdNull() {
        NORCalculationStrategy strategy = new NORCalculationStrategy(null);
        PersonResult pr = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), OrganisationId.of(10L), null);
        assertThatIllegalArgumentException().isThrownBy(() -> strategy.valid(pr));
    }

    @Test
    void valid_personResult_throwsWhenNorOrganisationNull() {
        // organisationById ohne eine Org mit ShortName "NOR" → norOrganisation bleibt null
        Organisation club = Organisation.of(1L, "Testclub", "TC");
        Map<OrganisationId, Organisation> orgById = Map.of(OrganisationId.of(1L), club);
        NORCalculationStrategy strategy = new NORCalculationStrategy(orgById);
        PersonResult pr = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), OrganisationId.of(1L), null);
        assertThatIllegalArgumentException().isThrownBy(() -> strategy.valid(pr));
    }

    @Test
    void valid_personResult_returnsTrueWhenOrgBelongsToNOR() {
        // NOR-Dachverband enthält Club als Kind
        OrganisationId norId  = OrganisationId.of(100L);
        OrganisationId clubId = OrganisationId.of(101L);

        Organisation club = new Organisation(
                clubId,
                OrganisationName.of("Testclub"),
                OrganisationShortName.of("TC"),
                OrganisationType.fromValue("Club"),
                null,
                new ArrayList<>());

        Organisation norOrg = new Organisation(
                norId,
                OrganisationName.of("Norges Orienteringsforbund"),
                OrganisationShortName.of("NOR"),
                OrganisationType.fromValue("NationalFederation"),
                null,
                List.of(clubId));

        Map<OrganisationId, Organisation> orgById = Map.of(norId, norOrg, clubId, club);
        NORCalculationStrategy strategy = new NORCalculationStrategy(orgById);

        PersonResult pr = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), clubId, null);
        assertThat(strategy.valid(pr)).isTrue();
    }

    @Test
    void valid_personResult_returnsFalseWhenOrgNotInNOR() {
        OrganisationId norId      = OrganisationId.of(100L);
        OrganisationId foreignId  = OrganisationId.of(200L);

        Organisation norOrg = new Organisation(
                norId,
                OrganisationName.of("Norges Orienteringsforbund"),
                OrganisationShortName.of("NOR"),
                OrganisationType.fromValue("NationalFederation"),
                null,
                new ArrayList<>());

        Organisation foreign = new Organisation(
                foreignId,
                OrganisationName.of("Foreignclub"),
                OrganisationShortName.of("FC"),
                OrganisationType.fromValue("Club"),
                null,
                new ArrayList<>());

        Map<OrganisationId, Organisation> orgById = Map.of(norId, norOrg, foreignId, foreign);
        NORCalculationStrategy strategy = new NORCalculationStrategy(orgById);

        PersonResult pr = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), foreignId, null);
        assertThat(strategy.valid(pr)).isFalse();
    }

    // -------------------------------------------------------------------------
    // valid(Organisation)
    // -------------------------------------------------------------------------

    @Test
    void valid_organisation_returnsFalseWhenNorOrganisationNull() {
        Organisation club = Organisation.of(1L, "Testclub", "TC");
        NORCalculationStrategy strategy = new NORCalculationStrategy(Map.of(OrganisationId.of(1L), club));
        assertThat(strategy.valid(club)).isFalse();
    }

    @Test
    void valid_organisation_returnsTrueForDirectNorOrg() {
        OrganisationId norId = OrganisationId.of(100L);
        Organisation norOrg = new Organisation(
                norId,
                OrganisationName.of("Norges Orienteringsforbund"),
                OrganisationShortName.of("NOR"),
                OrganisationType.fromValue("NationalFederation"),
                null,
                new ArrayList<>());
        Map<OrganisationId, Organisation> orgById = Map.of(norId, norOrg);
        NORCalculationStrategy strategy = new NORCalculationStrategy(orgById);
        assertThat(strategy.valid(norOrg)).isTrue();
    }

    @Test
    void valid_personResult_returnsFalseWhenPersonOrgIdNotInMap() {
        OrganisationId norId  = OrganisationId.of(100L);
        OrganisationId clubId = OrganisationId.of(101L);

        Organisation club = new Organisation(
                clubId,
                OrganisationName.of("Testclub"),
                OrganisationShortName.of("TC"),
                OrganisationType.fromValue("Club"),
                null,
                new ArrayList<>());

        Organisation norOrg = new Organisation(
                norId,
                OrganisationName.of("Norges Orienteringsforbund"),
                OrganisationShortName.of("NOR"),
                OrganisationType.fromValue("NationalFederation"),
                null,
                List.of(clubId));

        Map<OrganisationId, Organisation> orgById = Map.of(norId, norOrg, clubId, club);
        NORCalculationStrategy strategy = new NORCalculationStrategy(orgById);

        // Person's orgId is not in the map at all → optionalOrganisation.isEmpty()
        OrganisationId unknownId = OrganisationId.of(999L);
        PersonResult pr = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), unknownId, null);
        assertThat(strategy.valid(pr)).isFalse();
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private static ClassResult classResult(String shortName) {
        return ClassResult.of(shortName, shortName, null, null, null);
    }
}
