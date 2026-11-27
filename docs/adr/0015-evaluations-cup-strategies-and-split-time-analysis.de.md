# ADR 0015: Auswertungen als Domänen-Strategien und Anwendungsdienste (Cups, Splitzeiten-Analyse)

> **Deutsch** · [English](0015-evaluations-cup-strategies-and-split-time-analysis.md)

- **Status:** akzeptiert
- **Datum:** 2024-01-09
- **Kontext:** Cup-Berechnung (Strategien seit 2024-01-09, `CupType` seit 2023-12-31); Splitzeiten-Analyse seit 2025-12-03 (Ranking), erweitert um Abschnittsfolgen (Dokument vom 2026-02-25)

## Kontext

Das README nennt als Funktionen die Berechnung von Cup- und Ranglistenpunkten für den
deutschen Nebel-Cup, den Kristall-Cup und die Nord-Ost-Rangliste sowie eine
Splitzeiten-Analyse (Ranking, mentale Belastbarkeit, Anomalieerkennung,
Hängererkennung). Die Wertungsregeln unterscheiden sich je Cup; die Analysen sind
rechenintensiv und dürfen die Antworten nicht unbegrenzt aufblähen.

## Entscheidung

1. **Je Cup-Typ eine Berechnungsstrategie in der Domäne.** `CupType` kennt `ADD`, `NOR`,
   `KJ`, `NEBEL` und `KRISTALL`. Das Interface `CupTypeCalculationStrategy`
   (`domain/scoring`) berechnet aus Cup, Einzelergebnissen und Organisationszuordnung die
   `CupScore`-Liste und kann die Zahl der gewerteten Läufe und Klassennamen anpassen. Es
   gibt je eine Implementierung (`NebelCalculationStrategy`, `KristallCalculationStrategy`,
   `KJCalculationStrategy`, `NORCalculationStrategy`, `AddCalculationStrategy`).
   Regeln wie auszuschließende Klassen und Organisationen stehen im Code der Strategie.

2. **Cup-Orchestrierung in der Anwendung.** `CupServiceImpl`, `CupQueryServiceImpl`
   und `ChampionshipFilterServiceImpl` laden Daten über die Ports und setzen Strategien
   ein; Ergebnisse (`CupScoreList`) werden in der Datenbank gespeichert
   ([ADR 0002](0002-spring-data-jdbc-instead-of-jpa.de.md)).

3. **Splitzeiten-Analyse in `application/analysis` mit Modell in `domain/analysis`.**
   Eigene Dienste für Ranking (`SplitTimeRankingServiceImpl`), Tabelle
   (`SplitTimeTableServiceImpl`), mentale Belastbarkeit, Anomalieerkennung und
   Hängererkennung. Zugriff über `SplitTimeAnalysisController` unter
   `/split_time_analysis/result_list/{id}/…`.

4. **Abschnittsfolgen sind optional und begrenzt.** Das Ranking kann neben einzelnen
   Abschnitten zusammenhängende Folgen auswerten (`includeSequences`, `sequenceMinControls`,
   Standard 3). Ohne die Parameter bleibt das Verhalten unverändert. Zur Begrenzung von
   Antwortgröße und Laufzeit gelten höchstens 100 Läufer je Folge und 500 Folgen je
   Antwort, mit deterministischer Sortierung vor dem Abschneiden.

5. **Fachtests.** Unter `src/test` gibt es Tests für `AddCalculationStrategy`,
   `KJCalculationStrategy`, `KristallCalculationStrategy`, die gemeinsame
   Strategie-Schnittstelle und alle Analyse-Dienste ([ADR 0006](0006-test-strategy.de.md));
   Tests für `NebelCalculationStrategy` und `NORCalculationStrategy` wurden nicht gefunden.

## Konsequenzen

**Positiv**

- Neue Cup-Typen lassen sich durch eine weitere Strategie ergänzen; die Regeln sind
  getrennt testbar.
- Die Analysen sind additiv und rückwärtskompatibel erweitert; die Grenzen schützen die API.

**Negativ / Kosten**

- Wertungsregeln stehen als Konstanten im Code (z. B. ausgeschlossene Klassen); eine
  Änderung erfordert eine Neuauslieferung. Das ist laut Projektinhaber aus Aufwandsgründen so
  entschieden worden und soll noch umgebaut werden. Das README nennt „Generalisierung von
  Cups“ als mögliche Erweiterung.
- Die Analyse-Dienste sind groß (`SplitTimeRankingServiceImpl` rund 44 KB).
- Die Schwellen der Analysen (Anomalien, Hänger) sind fachliche Annahmen, die iterativ
  angepasst wurden („Iteration 5“, Stand 2025-12-10, in `doc/si-metrics`); für die
  Hängererkennung wurde keine eigene Beschreibung gefunden.

## Alternativen (nicht dokumentiert, naheliegend)

- **Wertungsregeln in der Datenbank oder Konfiguration** statt im Code; geplanter Umbau
  (Generalisierung von Cups, siehe README).

## Verwandte Dokumente

- [`CupType.java`](../../backend/src/main/java/de/jobst/resulter/domain/CupType.java)
- [`CupTypeCalculationStrategy.java`](../../backend/src/main/java/de/jobst/resulter/domain/scoring/CupTypeCalculationStrategy.java)
- [`CupServiceImpl.java`](../../backend/src/main/java/de/jobst/resulter/application/CupServiceImpl.java)
- [`SplitTimeAnalysisController.java`](../../backend/src/main/java/de/jobst/resulter/adapter/driver/web/SplitTimeAnalysisController.java)
- [`SplitTimeRankingServiceImpl.java`](../../backend/src/main/java/de/jobst/resulter/application/analysis/SplitTimeRankingServiceImpl.java)
- [`split-time-sequence-ranking.md`](../../doc/split-time-sequence-ranking.de.md)
- [`anomalie-erkennungs-schwellenwerte.md`](../../doc/si-metrics/anomaly-detection-thresholds.de.md)
- [`mental-resilience-index-metric.md`](../../doc/si-metrics/mental-resilience-index-metric.de.md)
- [README, Abschnitt Overview](../../README.de.md)
