# ADR 0015: Evaluations as domain strategies and application services (cups, split time analysis)

> [Deutsch](0015-evaluations-cup-strategies-and-split-time-analysis.de.md) · **English**

- **Status:** accepted
- **Date:** 2024-01-09
- **Context:** Cup calculation (strategies since 2024-01-09, `CupType` since 2023-12-31); split time analysis since 2025-12-03 (ranking), extended by section sequences (document of 2026-02-25)

## Context

The README lists as features the calculation of cup and ranking points for the
German Nebel-Cup, the Kristall-Cup and the Nord-Ost-Rangliste, as well as a
split time analysis (ranking, mental resilience, anomaly detection,
stall detection). The scoring rules differ per cup; the analyses are
computationally expensive and must not inflate the responses without limit.

## Decision

1. **One calculation strategy per cup type in the domain.** `CupType` knows `ADD`, `NOR`,
   `KJ`, `NEBEL` and `KRISTALL`. The interface `CupTypeCalculationStrategy`
   (`domain/scoring`) calculates the `CupScore` list from cup, individual results and
   organization assignment, and can adjust the number of counted races and class names. There
   is one implementation each (`NebelCalculationStrategy`, `KristallCalculationStrategy`,
   `KJCalculationStrategy`, `NORCalculationStrategy`, `AddCalculationStrategy`).
   Rules such as classes and organizations to exclude are in the strategy code.

2. **Cup orchestration in the application.** `CupServiceImpl`, `CupQueryServiceImpl`
   and `ChampionshipFilterServiceImpl` load data through the ports and apply strategies;
   results (`CupScoreList`) are stored in the database
   ([ADR 0002](0002-spring-data-jdbc-instead-of-jpa.md)).

3. **Split time analysis in `application/analysis` with the model in `domain/analysis`.**
   Separate services for ranking (`SplitTimeRankingServiceImpl`), table
   (`SplitTimeTableServiceImpl`), mental resilience, anomaly detection and
   stall detection. Access through `SplitTimeAnalysisController` under
   `/split_time_analysis/result_list/{id}/…`.

4. **Section sequences are optional and bounded.** Besides individual sections, the ranking can
   evaluate contiguous sequences (`includeSequences`, `sequenceMinControls`,
   default 3). Without the parameters the behavior is unchanged. To limit
   response size and runtime, at most 100 runners per sequence and 500 sequences per
   response apply, with deterministic sorting before truncation.

5. **Domain tests.** Under `src/test` there are tests for `AddCalculationStrategy`,
   `KJCalculationStrategy`, `KristallCalculationStrategy`, the common
   strategy interface and all analysis services ([ADR 0006](0006-test-strategy.md));
   no tests were found for `NebelCalculationStrategy` and `NORCalculationStrategy`.

## Consequences

**Positive**

- New cup types can be added through another strategy; the rules can be
  tested separately.
- The analyses were extended additively and backward compatibly; the limits protect the API.

**Negative / costs**

- Scoring rules are constants in the code (e.g. excluded classes); a
  change requires a new deployment. According to the project owner, this was decided
  for reasons of effort and is to be reworked. The README names "generalization of
  cups" as a possible extension.
- The analysis services are large (`SplitTimeRankingServiceImpl` around 44 KB).
- The thresholds of the analyses (anomalies, stalls) are domain assumptions that were
  adjusted iteratively ("iteration 5", as of 2025-12-10, in `doc/si-metrics`); no separate
  description was found for stall detection.

## Alternatives (not documented, obvious candidates)

- **Scoring rules in the database or configuration** instead of in the code; planned rework
  (generalization of cups, see README).

## Related documents

- [`CupType.java`](../../backend/src/main/java/de/jobst/resulter/domain/CupType.java)
- [`CupTypeCalculationStrategy.java`](../../backend/src/main/java/de/jobst/resulter/domain/scoring/CupTypeCalculationStrategy.java)
- [`CupServiceImpl.java`](../../backend/src/main/java/de/jobst/resulter/application/CupServiceImpl.java)
- [`SplitTimeAnalysisController.java`](../../backend/src/main/java/de/jobst/resulter/adapter/driver/web/SplitTimeAnalysisController.java)
- [`SplitTimeRankingServiceImpl.java`](../../backend/src/main/java/de/jobst/resulter/application/analysis/SplitTimeRankingServiceImpl.java)
- [`split-time-sequence-ranking.md`](../../doc/split-time-sequence-ranking.md)
- [`anomalie-erkennungs-schwellenwerte.md`](../../doc/si-metrics/anomalie-erkennungs-schwellenwerte.md)
- [`mental-resilience-index-metric.md`](../../doc/si-metrics/mental-resilience-index-metric.md)
- [README, Overview section](../../README.md)
