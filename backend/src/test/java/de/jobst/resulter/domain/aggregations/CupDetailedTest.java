package de.jobst.resulter.domain.aggregations;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CupDetailedTest {

    // -------------------------------------------------------------------------
    // Nicht-gruppiert (NOR/ADD) — aggregatedPersonScoresList wird durchgereicht
    // -------------------------------------------------------------------------

    @Test
    void constructor_notGroupedByOrganisation_keepsAggregatedPersonScores() {
        Cup cup = Cup.of(1L, "NOR Cup", CupType.NOR, Year.of(2025), List.of());
        AggregatedPersonScores h21Scores = new AggregatedPersonScores(
                ClassResultShortName.of("H21"),
                List.of(new PersonWithScore(PersonId.of(1L), 12.0, ClassResultShortName.of("H21"))));

        CupDetailed detailed = new CupDetailed(cup, List.of(), List.of(h21Scores), Map.of(), emptyStatistics());

        assertThat(detailed.getAggregatedPersonScoresList()).containsExactly(h21Scores);
        assertThat(detailed.getOverallOrganisationScores()).isEmpty();
    }

    @Test
    void constructor_notGroupedByOrganisation_withEmptyData_producesEmptyResults() {
        Cup cup = Cup.of(1L, "NOR Cup", CupType.NOR, Year.of(2025), List.of());

        CupDetailed detailed = new CupDetailed(cup, List.of(), List.of(), Map.of(), emptyStatistics());

        assertThat(detailed.getAggregatedPersonScoresList()).isEmpty();
        assertThat(detailed.getOverallOrganisationScores()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Gruppiert nach Organisation (KRISTALL/NEBEL/KJ) — initializeGroupedByOrganisationData
    // -------------------------------------------------------------------------

    @Test
    void constructor_groupedByOrganisation_buildsOverallScoresSortedByScoreDescending() {
        Cup cup = Cup.of(1L, "Kristall Cup", CupType.KRISTALL, Year.of(2025), List.of());

        Organisation orgA = Organisation.of(1L, "Verein A", "VA");
        Organisation orgB = Organisation.of(2L, "Verein B", "VB");

        // OrgA: 10 Punkte in H21, OrgB: 8 Punkte in D21
        OrganisationScore scoreOrgA = new OrganisationScore(orgA, 10.0,
                List.of(new PersonWithScore(PersonId.of(1L), 10.0, ClassResultShortName.of("H21"))));
        OrganisationScore scoreOrgB = new OrganisationScore(orgB, 8.0,
                List.of(new PersonWithScore(PersonId.of(2L), 8.0, ClassResultShortName.of("D21"))));

        EventRacesCupScore eventScore = new EventRacesCupScore(
                Event.of("Test Event"),
                List.of(new RaceOrganisationGroupedCupScore(
                        Race.of(EventId.of(1L), (byte) 1),
                        List.of(scoreOrgA, scoreOrgB))),
                List.of());

        CupDetailed detailed = new CupDetailed(cup, List.of(eventScore), List.of(), Map.of(), emptyStatistics());

        List<OrganisationScore> scores = detailed.getOverallOrganisationScores();
        assertThat(scores).hasSize(2);
        // Absteigende Sortierung nach Gesamtpunkten
        assertThat(scores.get(0).organisation()).isEqualTo(orgA);
        assertThat(scores.get(0).score()).isEqualTo(10.0);
        assertThat(scores.get(1).organisation()).isEqualTo(orgB);
        assertThat(scores.get(1).score()).isEqualTo(8.0);
    }

    @Test
    void constructor_groupedByOrganisation_aggregatesScoresAcrossMultipleRaces() {
        Cup cup = Cup.of(1L, "Kristall Cup", CupType.KRISTALL, Year.of(2025), List.of());

        Organisation orgA = Organisation.of(1L, "Verein A", "VA");

        // Zwei Rennen, OrgA hat 10 + 8 = 18 Punkte gesamt
        OrganisationScore race1Score = new OrganisationScore(orgA, 10.0,
                List.of(new PersonWithScore(PersonId.of(1L), 10.0, ClassResultShortName.of("H21"))));
        OrganisationScore race2Score = new OrganisationScore(orgA, 8.0,
                List.of(new PersonWithScore(PersonId.of(1L), 8.0, ClassResultShortName.of("H21"))));

        EventRacesCupScore eventScore = new EventRacesCupScore(
                Event.of("Test Event"),
                List.of(
                        new RaceOrganisationGroupedCupScore(Race.of(EventId.of(1L), (byte) 1), List.of(race1Score)),
                        new RaceOrganisationGroupedCupScore(Race.of(EventId.of(1L), (byte) 2), List.of(race2Score))),
                List.of());

        CupDetailed detailed = new CupDetailed(cup, List.of(eventScore), List.of(), Map.of(), emptyStatistics());

        assertThat(detailed.getOverallOrganisationScores()).hasSize(1);
        assertThat(detailed.getOverallOrganisationScores().getFirst().score()).isEqualTo(18.0);
    }

    @Test
    void constructor_groupedByOrganisation_fillsMissingClassesWithZeroForEachOrg() {
        Cup cup = Cup.of(1L, "Kristall Cup", CupType.KRISTALL, Year.of(2025), List.of());

        Organisation orgA = Organisation.of(1L, "Verein A", "VA");
        Organisation orgB = Organisation.of(2L, "Verein B", "VB");

        // OrgA hat H21, OrgB hat D21 — beide sollen danach beide Klassen haben
        OrganisationScore scoreOrgA = new OrganisationScore(orgA, 10.0,
                List.of(new PersonWithScore(PersonId.of(1L), 10.0, ClassResultShortName.of("H21"))));
        OrganisationScore scoreOrgB = new OrganisationScore(orgB, 8.0,
                List.of(new PersonWithScore(PersonId.of(2L), 8.0, ClassResultShortName.of("D21"))));

        EventRacesCupScore eventScore = new EventRacesCupScore(
                Event.of("Test Event"),
                List.of(new RaceOrganisationGroupedCupScore(
                        Race.of(EventId.of(1L), (byte) 1),
                        List.of(scoreOrgA, scoreOrgB))),
                List.of());

        CupDetailed detailed = new CupDetailed(cup, List.of(eventScore), List.of(), Map.of(), emptyStatistics());

        OrganisationScore resultOrgA = detailed.getOverallOrganisationScores().stream()
                .filter(s -> s.organisation().equals(orgA)).findFirst().orElseThrow();
        OrganisationScore resultOrgB = detailed.getOverallOrganisationScores().stream()
                .filter(s -> s.organisation().equals(orgB)).findFirst().orElseThrow();

        // Beide Orgs enthalten Einträge für alle vorkommenden Klassen (H21 + D21)
        assertThat(resultOrgA.personWithScores().stream()
                .map(p -> p.classResultShortName().value()).toList())
                .containsExactlyInAnyOrder("H21", "D21");
        assertThat(resultOrgB.personWithScores().stream()
                .map(p -> p.classResultShortName().value()).toList())
                .containsExactlyInAnyOrder("H21", "D21");

        // OrgB hat 0.0 für H21 (Platzhalter)
        double orgBH21Score = resultOrgB.personWithScores().stream()
                .filter(p -> p.classResultShortName().value().equals("H21"))
                .mapToDouble(PersonWithScore::score).sum();
        assertThat(orgBH21Score).isEqualTo(0.0);
    }

    @Test
    void constructor_groupedByOrganisation_withEmptyEventScores_producesEmptyOverallScores() {
        Cup cup = Cup.of(1L, "Kristall Cup", CupType.KRISTALL, Year.of(2025), List.of());

        CupDetailed detailed = new CupDetailed(cup, List.of(), List.of(), Map.of(), emptyStatistics());

        assertThat(detailed.getOverallOrganisationScores()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Getter für konstruierte Felder
    // -------------------------------------------------------------------------

    @Test
    void getters_returnValuesFromConstructor() {
        Cup cup = Cup.of(1L, "NOR Cup", CupType.NOR, Year.of(2025), List.of());
        Person person = Person.of("Mustermann", "Max", null, Gender.M);
        Map<PersonId, Person> personsById = Map.of(PersonId.of(1L), person);
        CupStatistics stats = emptyStatistics();

        CupDetailed detailed = new CupDetailed(cup, List.of(), List.of(), personsById, stats);

        assertThat(detailed.getPersonsById()).isEqualTo(personsById);
        assertThat(detailed.getCupStatistics()).isEqualTo(stats);
        assertThat(detailed.getEventRacesCupScore()).isEmpty();
        assertThat(detailed.getId()).isEqualTo(cup.getId());
        assertThat(detailed.getType()).isEqualTo(CupType.NOR);
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private static CupStatistics emptyStatistics() {
        return new CupStatistics(CupOverallStatistics.of(0, 0, 0, 0), List.of());
    }
}
