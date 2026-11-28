# ADR 0006: Teststrategie

> **Deutsch** · [English](0006-test-strategy.md)

- **Status:** akzeptiert
- **Datum:** 2023-12-17
- **Hinweis:** Punkt 5 ist durch [ADR 0022](0022-unit-tests-in-ci.de.md) abgelöst.
- **Kontext:** Backend-Tests mit JUnit seit dem Projektstart, Frontend-Tests mit Vitest ab dem Frontend-Start (2023-12-17), Playwright-E2E ab 2024-06-19

## Kontext

Das Projekt besteht aus einem Spring-Boot-Backend und einem Vue-Frontend
([ADR 0001](0001-hexagonal-architecture-backend.de.md),
[ADR 0007](0007-frontend-stack-and-structure.de.md)). Die Fachlogik
(Ergebnislisten, Cup-Punkte, Splitzeiten-Analyse) ist rechenintensiv und fehleranfällig;
das Frontend hat umfangreiche Formulare und seit 2026 einen grafischen Urkunden-Editor.

## Entscheidung

1. **Backend: Unit-Tests auf Domäne und Anwendung.** `backend/src/test` testet die
   Domäne (u. a. Value Objects, Cup-Punkte, Analysen) und die Services mit JUnit 5 und
   Mockito. `EqualsVerifier` prüft `equals`/`hashCode` der Domänenklassen
   (`EqualsVerifierTestCase`). `JMoleculesRulesUnitTests` prüft Architekturregeln
   ([ADR 0001](0001-hexagonal-architecture-backend.de.md)).

2. **Backend: Adapter und Integration.** Web- und Import-Tests liegen unter
   `adapter/driver/web` (`XMLImportServiceTest`, `BffControllerIntegrationTest`,
   `DatabaseCreateTest`, `NPlusOneReviewIT`). Testcontainers (PostgreSQL) steht als
   Abhängigkeit bereit und wird im Profil `e2e-frontend-tests` benutzt
   ([ADR 0005](0005-e2e-database-isolation.de.md)). Die In-Memory-Repositories erlauben
   Service-Tests ohne Datenbank.

3. **Frontend: Vitest.** Konfiguration in `vitest.config.ts` (Umgebung `jsdom`,
   Dateien unter `frontend/test/**`, Spezifikationen für Modelle, Services, Stores und
   die Urkunden-Geometrie). Aufruf mit `pnpm test:unit`.

4. **Frontend: Playwright-E2E mit Datenbankisolation.** Tests liegen in `frontend/e2e`,
   laufen parallel (lokal 4 Worker, in CI 1) in Chromium, Firefox und Edge; die
   Anmeldung wird einmal aufgebaut (`auth-setup.ts`) und die Sitzung wiederverwendet.
   Backend und Frontend müssen vorher laufen, siehe `TESTING.md`.

5. **CI führt keine Tests aus (Stand der Workflows).** `build.yml` und die
   `quality-gate`-Jobs in `build-paketo.yml` führen `mvn compile` bzw. `pnpm build`
   (Typprüfung und Build) aus, nicht `mvn test`, `vitest` oder Playwright. Lint und
   Unit-Tests laufen lokal über den Pre-Commit-Hook (`lint-staged`) und von Hand.
   Ob das beabsichtigt ist: Begründung nicht dokumentiert.

## Konsequenzen

**Positiv**

- Die Fachlogik hat eine breite Unit-Test-Basis (über 1500 `@Test`-Methoden im Backend).
- E2E-Tests sind voneinander isoliert und reproduzierbar.

**Negativ / Kosten**

- Da die CI nur kompiliert, hängt die Qualitätssicherung an lokalen Läufen.
- E2E-Läufe brauchen Docker, laufende Server und Zugangsdaten für einen Testbenutzer.
- `backend/src/it` (Architekturtest) wird laut `pom.xml` nicht als Quellordner
  eingebunden, siehe ADR 0001.

## Alternativen (nicht dokumentiert, naheliegend)

- **Tests in der CI ausführen** (`mvn verify`, `pnpm test:unit`).
- **H2 statt Testcontainers** für Integrationstests: schneller, aber nicht dieselbe
  Datenbank wie im Betrieb.

## Verwandte Dokumente

- [`TESTING.md`](../../frontend/TESTING.de.md)
- [`vitest.config.ts`](../../frontend/vitest.config.ts)
- [`playwright.config.ts`](../../frontend/playwright.config.ts)
- [`frontend/e2e/README.md`](../../frontend/e2e/README.de.md)
- [`EqualsVerifierTestCase.java`](../../backend/src/test/java/de/jobst/resulter/domain/EqualsVerifierTestCase.java)
- [`build.yml`](../../.github/workflows/build.yml)
- [`build-paketo.yml`](../../.github/workflows/build-paketo.yml)
