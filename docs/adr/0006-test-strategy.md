# ADR 0006: Test strategy

> [Deutsch](0006-test-strategy.de.md) · **English**

- **Status:** accepted
- **Date:** 2023-12-17
- **Note:** Point 5 is superseded by [ADR 0022](0022-unit-tests-in-ci.md).
- **Context:** Backend tests with JUnit since project start, frontend tests with Vitest since the frontend start (2023-12-17), Playwright E2E since 2024-06-19

## Context

The project consists of a Spring Boot backend and a Vue frontend
([ADR 0001](0001-hexagonal-architecture-backend.md),
[ADR 0007](0007-frontend-stack-and-structure.md)). The domain logic
(result lists, cup points, split-time analysis) is computation-heavy and error-prone;
the frontend has extensive forms and, since 2026, a graphical certificate editor.

## Decision

1. **Backend: unit tests on domain and application.** `backend/src/test` tests the
   domain (including value objects, cup points, analyses) and the services with JUnit 5 and
   Mockito. `EqualsVerifier` checks `equals`/`hashCode` of the domain classes
   (`EqualsVerifierTestCase`). `JMoleculesRulesUnitTests` checks architecture rules
   ([ADR 0001](0001-hexagonal-architecture-backend.md)).

2. **Backend: adapters and integration.** Web and import tests are located under
   `adapter/driver/web` (`XMLImportServiceTest`, `BffControllerIntegrationTest`,
   `DatabaseCreateTest`, `NPlusOneReviewIT`). Testcontainers (PostgreSQL) is available as a
   dependency and is used in the profile `e2e-frontend-tests`
   ([ADR 0005](0005-e2e-database-isolation.md)). The in-memory repositories allow
   service tests without a database.

3. **Frontend: Vitest.** Configuration in `vitest.config.ts` (environment `jsdom`,
   files under `frontend/test/**`, specifications for models, services, stores and
   the certificate geometry). Run with `pnpm test:unit`.

4. **Frontend: Playwright E2E with database isolation.** Tests are in `frontend/e2e`,
   run in parallel (4 workers locally, 1 in CI) in Chromium, Firefox and Edge; the
   login is set up once (`auth-setup.ts`) and the session is reused.
   Backend and frontend must be running beforehand, see `TESTING.md`.

5. **CI does not run tests (as of the workflows).** `build.yml` and the
   `quality-gate` jobs in `build-paketo.yml` run `mvn compile` and `pnpm build`
   (type check and build), not `mvn test`, `vitest` or Playwright. Lint and
   unit tests run locally via the pre-commit hook (`lint-staged`) and by hand.
   Whether this is intended: rationale not documented.

## Consequences

**Positive**

- The domain logic has a broad unit test base (over 1500 `@Test` methods in the backend).
- E2E tests are isolated from each other and reproducible.

**Negative / costs**

- Since CI only compiles, quality assurance depends on local runs.
- E2E runs need Docker, running servers and credentials for a test user.
- `backend/src/it` (architecture test) is not included as a source folder
  according to `pom.xml`, see ADR 0001.

## Alternatives (not documented, obvious candidates)

- **Run tests in CI** (`mvn verify`, `pnpm test:unit`).
- **H2 instead of Testcontainers** for integration tests: faster, but not the same
  database as in production.

## Related documents

- [`TESTING.md`](../../frontend/TESTING.md)
- [`vitest.config.ts`](../../frontend/vitest.config.ts)
- [`playwright.config.ts`](../../frontend/playwright.config.ts)
- [`frontend/e2e/README.md`](../../frontend/e2e/README.md)
- [`EqualsVerifierTestCase.java`](../../backend/src/test/java/de/jobst/resulter/domain/EqualsVerifierTestCase.java)
- [`build.yml`](../../.github/workflows/build.yml)
- [`build-paketo.yml`](../../.github/workflows/build-paketo.yml)
