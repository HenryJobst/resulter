# ADR 0005: E2E-Tests mit eigener Datenbank je Test (X-DB-Identifier)

> **Deutsch** · [English](0005-e2e-database-isolation.md)

- **Status:** akzeptiert
- **Datum:** 2024-07-01
- **Kontext:** Playwright-E2E-Tests gegen das echte Backend; `DataSourceManager` (2024-07-01), Testcontainers-Profil (2024-06-26)

## Kontext

Die Playwright-Tests (`frontend/e2e`) laufen gegen ein laufendes Backend. Teilen sich
mehrere Tests eine Datenbank, beeinflussen sie sich gegenseitig und müssen ihre Daten
selbst aufräumen. `frontend/e2e/E2E_DATABASE_ISOLATION.md` beschreibt das Problem
(manuelles Aufräumen, Interferenz, Parallelität) und die gewählte Lösung.

Wichtig zur Abgrenzung: `X-DB-Identifier` ist **keine Mandantenfähigkeit** der
Anwendung. Die Klassen sind auf das Profil `e2e-frontend-tests` beschränkt und im Betrieb
nicht aktiv.

## Entscheidung

1. **Eine PostgreSQL-Instanz pro Test.** `POST /createDatabase` startet über
   Testcontainers einen neuen PostgreSQL-Container mit eigener Datenbank, wendet die
   Liquibase-Changelogs an ([ADR 0003](0003-postgresql-and-liquibase.de.md)) und gibt eine
   UUID zurück. Der Endpunkt ist durch einen eigenen API-Token-Filter
   (`CreateDatabaseApiTokenFilter`) geschützt; der Token kommt aus der Konfiguration.

2. **Routing über Header oder Cookie.** `DataSourceInterceptor` liest `X-DB-Identifier`
   aus dem Header (bevorzugt, für API-Aufrufe) oder aus dem gleichnamigen Cookie
   (Browser-Tests, damit der OAuth2-Ablauf nicht stört). Die gewählte `DataSource` steht
   in einem `ThreadLocal` (`DataSourceContextHolder`).

3. **Dynamisches Routing der Datenquelle.** `DynamicRoutingDataSource` liefert pro
   Anfrage die DataSource aus dem `ThreadLocal`, sonst die Standard-DataSource.

4. **Automatisches Aufräumen.** `DataSourceCleanupScheduler` prüft alle 10 Sekunden; nicht
   mehr genutzte Datenquellen werden geschlossen und ihr Container gestoppt. Die
   Standard-Inaktivitätszeit steht im Code auf 90 Sekunden
   (`DataSourceManager.defaultTimeout`); `E2E_DATABASE_ISOLATION.md` nennt 30 Sekunden.

5. **Playwright-Helfer.** `e2e/helpers/database.ts` und `factories.ts` legen die
   Datenbank an und setzen Cookie oder Header.

CORS erlaubt im Profil `e2e-frontend-tests` den zusätzlichen Header `X-DB-Identifier`
(`cors.extra-allowed-headers`).

## Konsequenzen

**Positiv**

- Tests sind voneinander unabhängig und können parallel laufen; Aufräumcode in den Tests entfällt.
- Jede Testdatenbank hat das echte Schema aus den Liquibase-Changelogs.

**Negativ / Kosten**

- Docker und Testcontainers sind für E2E-Läufe nötig; jeder Test startet einen Container.
- Der Container-Image-Tag ist im Code `postgres:latest` und weicht damit von der
  Version in `compose.yml` (`postgres:16`) ab.
- Die Lösung verlangt ein eigenes Backend-Profil (`start-e2e-frontend-tests.sh`) und
  ein zusätzliches Sicherheitsbauteil (Token-Filter für `/createDatabase`).
- Die Dokumentation und der Code nennen unterschiedliche Timeouts (30 s und 90 s).

## Alternativen (nicht dokumentiert, naheliegend)

- **Gemeinsame Testdatenbank mit Aufräumen in jedem Test:** das Problem, das
  `E2E_DATABASE_ISOLATION.md` als „Before“ beschreibt.
- **Ein Schema pro Test in einer Instanz:** weniger Container, weniger Isolation.

## Verwandte Dokumente

- [`E2E_DATABASE_ISOLATION.md`](../../frontend/e2e/E2E_DATABASE_ISOLATION.md)
- [`frontend/e2e/README.md`](../../frontend/e2e/README.md)
- [`DataSourceManager.java`](../../backend/src/main/java/de/jobst/resulter/springapp/config/DataSourceManager.java)
- [`DataSourceInterceptor.java`](../../backend/src/main/java/de/jobst/resulter/springapp/config/DataSourceInterceptor.java)
- [`DynamicRoutingDataSource.java`](../../backend/src/main/java/de/jobst/resulter/springapp/config/DynamicRoutingDataSource.java)
- [`application-e2e-frontend-tests.properties`](../../backend/src/main/resources/application-e2e-frontend-tests.properties)
- [`start-e2e-frontend-tests.sh`](../../backend/start-e2e-frontend-tests.sh)
- [`database.ts`](../../frontend/e2e/helpers/database.ts)
