# ADR 0022: Unit tests in CI

> [Deutsch](0022-unit-tests-in-ci.de.md) · **English**

- **Status:** accepted
- **Date:** 2026-11-28
- **Context:** The architecture documentation ([`architecture.md`](../architecture.md)) showed that CI ran no application tests. This ADR supersedes point 5 of [ADR 0006](0006-test-strategy.md).

## Context

[ADR 0006](0006-test-strategy.md) recorded that `build.yml` ran `mvn compile` and `pnpm build` only.
Tests ran locally, through the pre-commit hook and by hand. A local run on 2026-11-28 showed that
the unit tests are fast and need no database or Docker: 742 frontend tests in about 35 seconds, and
1740 backend tests with one error. The error came from `ResultListServiceTest`, which loads the
media path through `Dotenv.load()` and found no `.env` in `backend/`. The rationale for having no
tests in CI is not documented in the repository.

## Decision

1. **CI runs the unit tests of backend and frontend.** `build.yml` has a step `Run backend tests`
   (`mvn --batch-mode test`) and a step `Run frontend unit tests` (`pnpm test:unit`). A failing test
   fails the workflow.

2. **The test step is separate from the build step.** The Maven build keeps its retry loop for
   transient repository errors; the tests run once after it, so a failing test is not repeated.

3. **The backend step creates a `.env`.** It writes `backend/.env` with
   `RESULTER_MEDIA_FILE_PATH=src/test/resources/certificate/` before the tests, because
   `ResultListServiceTest` needs the variable and the images of its layout. The images are test
   resources; the directory `data/` is ignored by Git, so a copy of `.env.example` would point to
   files that do not exist on a runner. The `.env` file is ignored by Git and is not committed.

4. **End-to-end tests stay out of CI.** They need Keycloak, Testcontainers and three browsers; the
   decision on a separate job is left open.

5. **The image workflow is unchanged.** `build-paketo.yml` still compiles and builds without tests
   and builds the images with `-Dmaven.test.skip=true`.

## Consequences

**Positive**

- A failing unit test is visible on every push, not only in local runs.
- The existing test base (over 1700 backend and 700 frontend tests) protects the main branch.

**Negative / costs**

- The CI run gets longer; the backend duration on a runner has not been measured.
- Tests that depend on the local environment, such as the `.env` file, show up in CI and must be
  fixed or prepared there.
- The workflow change has not been run on GitHub when this ADR was written.
- The Spring and database tests (`*IT`, `@SpringBootTest`) run as part of `mvn test` and were not
  examined one by one for Docker needs on a runner.

## Alternatives (not documented, obvious candidates)

- **Unit tests only on release branches:** shorter normal runs, but errors are found late.
- **A separate test job** next to the build job: more parallelism, but the dependencies are
  installed twice.
- **Include `mvn verify` and the `*IT` tests:** broader, but needs to be checked for Docker and
  runtime first.

## Related documents

- [ADR 0006](0006-test-strategy.md)
- [`build.yml`](../../.github/workflows/build.yml)
- [`build-paketo.yml`](../../.github/workflows/build-paketo.yml)
- [`.env.example`](../../.env.example)
- [`ResultListServiceTest.java`](../../backend/src/test/java/de/jobst/resulter/application/ResultListServiceTest.java)
- [`vitest.config.ts`](../../frontend/vitest.config.ts)
