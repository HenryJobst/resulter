# ADR 0005: E2E tests with a separate database per test (X-DB-Identifier)

> [Deutsch](0005-e2e-database-isolation.de.md) · **English**

- **Status:** accepted
- **Date:** 2024-07-01
- **Context:** Playwright E2E tests against the real backend; `DataSourceManager` (2024-07-01), Testcontainers profile (2024-06-26)

## Context

The Playwright tests (`frontend/e2e`) run against a running backend. If several tests
share one database, they influence each other and have to clean up their data
themselves. `frontend/e2e/E2E_DATABASE_ISOLATION.md` describes the problem
(manual cleanup, interference, parallelism) and the chosen solution.

Important for delimitation: `X-DB-Identifier` is **not multi-tenancy** of the
application. The classes are restricted to the `e2e-frontend-tests` profile and are not
active in production.

## Decision

1. **One PostgreSQL instance per test.** `POST /createDatabase` starts a new PostgreSQL
   container with its own database via Testcontainers, applies the Liquibase changelogs
   ([ADR 0003](0003-postgresql-and-liquibase.md)) and returns a UUID. The endpoint is
   protected by a dedicated API token filter (`CreateDatabaseApiTokenFilter`); the token
   comes from the configuration.

2. **Routing via header or cookie.** `DataSourceInterceptor` reads `X-DB-Identifier`
   from the header (preferred, for API calls) or from the cookie of the same name
   (browser tests, so that the OAuth2 flow does not interfere). The selected `DataSource`
   is held in a `ThreadLocal` (`DataSourceContextHolder`).

3. **Dynamic routing of the data source.** `DynamicRoutingDataSource` returns the
   DataSource from the `ThreadLocal` per request, otherwise the default DataSource.

4. **Automatic cleanup.** `DataSourceCleanupScheduler` checks every 10 seconds; data
   sources that are no longer in use are closed and their container is stopped. The
   default inactivity time is set to 90 seconds in the code
   (`DataSourceManager.defaultTimeout`); `E2E_DATABASE_ISOLATION.md` states 30 seconds.

5. **Playwright helpers.** `e2e/helpers/database.ts` and `factories.ts` create the
   database and set the cookie or header.

In the `e2e-frontend-tests` profile, CORS allows the additional header `X-DB-Identifier`
(`cors.extra-allowed-headers`).

## Consequences

**Positive**

- Tests are independent of each other and can run in parallel; cleanup code in the tests is no longer needed.
- Every test database has the real schema from the Liquibase changelogs.

**Negative / costs**

- Docker and Testcontainers are required for E2E runs; every test starts a container.
- The container image tag in the code is `postgres:latest` and therefore differs from the
  version in `compose.yml` (`postgres:16`).
- The solution requires its own backend profile (`start-e2e-frontend-tests.sh`) and
  an additional security component (token filter for `/createDatabase`).
- The documentation and the code state different timeouts (30 s and 90 s).

## Alternatives (not documented, obvious candidates)

- **Shared test database with cleanup in every test:** the problem that
  `E2E_DATABASE_ISOLATION.md` describes as "Before".
- **One schema per test in one instance:** fewer containers, less isolation.

## Related documents

- [`E2E_DATABASE_ISOLATION.md`](../../frontend/e2e/E2E_DATABASE_ISOLATION.md)
- [`frontend/e2e/README.md`](../../frontend/e2e/README.md)
- [`DataSourceManager.java`](../../backend/src/main/java/de/jobst/resulter/springapp/config/DataSourceManager.java)
- [`DataSourceInterceptor.java`](../../backend/src/main/java/de/jobst/resulter/springapp/config/DataSourceInterceptor.java)
- [`DynamicRoutingDataSource.java`](../../backend/src/main/java/de/jobst/resulter/springapp/config/DynamicRoutingDataSource.java)
- [`application-e2e-frontend-tests.properties`](../../backend/src/main/resources/application-e2e-frontend-tests.properties)
- [`start-e2e-frontend-tests.sh`](../../backend/start-e2e-frontend-tests.sh)
- [`database.ts`](../../frontend/e2e/helpers/database.ts)
