# ADR 0003: PostgreSQL as database, Liquibase for the schema, H2 for local profiles

> [Deutsch](0003-postgresql-and-liquibase.de.md) · **English**

- **Status:** accepted
- **Date:** 2023-12-27
- **Context:** Commit `b0f5b92b` ("Postgresql database as backend database (via docker-compose)"); Liquibase and H2 have been in the backend since the first commit (2023-12-02)

## Context

The first version of the backend used H2 and Liquibase. On 2023-12-27 PostgreSQL was
added as the backend database, started via Docker Compose. The schema is described in
Liquibase changelogs and is not generated from the entities.

## Decision

1. **PostgreSQL is the database for development and production.** The `dev` profile
   connects to a local PostgreSQL instance; `compose.yml` in the repository root
   (linked from the backend) starts it (`postgres:16`), and
   `spring-boot-docker-compose` can bring it up at startup
   (`spring.docker.compose.lifecycle-management=start_only`). The `prod` profile reads
   host, port and database name from environment variables.

2. **Liquibase owns the schema.** `db.changelog-master.yaml` includes all YAML files
   from `db/changelog/jdbc_migrations` via `includeAll` (currently 43 files,
   numbered `000000` to `000042` plus initial data `0001001` to `0001003` for
   countries, organisations and cups). `spring.liquibase.enabled=true` in `dev` and
   `prod`; `liquibase.properties` sets `strict=true`. A schema change is a
   new changelog file.

3. **PostgreSQL-specific parts are guarded.** The changeset
   `000000_pgsql_extensions_only_on_postgres` creates the `hstore` extension and
   runs only on PostgreSQL (`dbms: postgresql`, otherwise `MARK_RAN`).

4. **H2 remains for local runs without a database server.** The `inmem` profile
   (`InmemDatasourceConfig`, `JdbcConfigurationForH2`) uses an H2 file
   `./testdb`; `src/test/resources/persistence.properties` points to an
   H2 in-memory database. The integration environment for E2E tests, in contrast, uses
   real PostgreSQL in Testcontainers (see
   [ADR 0005](0005-e2e-database-isolation.md)).

Rationale for PostgreSQL over H2 in production (project owner): PostgreSQL is the best
available open-source database for professional applications. It is performant,
feature-rich and has been well maintained for a long time.

## Consequences

**Positive**

- Traceable, versioned schema changes; the same changelogs run in
  development, production and in the E2E test databases.
- Local development without Docker is possible with `inmem`.

**Negative / costs**

- Differences between H2 and PostgreSQL (such as `hstore`) have to be taken into
  account in the changesets; H2 runs are not a full substitute for PostgreSQL.
- Data migrations and schema changes require manually written changesets.

## Alternatives (not documented, obvious candidates)

- **Generate the schema from the entities** (`hbm2ddl` style): faster, but without
  versioned migrations for production. `persistence.properties` still contains
  `hibernate.hbm2ddl.auto=create` as a leftover from the JPA era.
- **Flyway** instead of Liquibase.

## Related documents

- [`db.changelog-master.yaml`](../../backend/src/main/resources/db/changelog/db.changelog-master.yaml)
- [`jdbc_migrations`](../../backend/src/main/resources/db/changelog/jdbc_migrations)
- [`000000_pgsql_extensions_only_on_postgres.yaml`](../../backend/src/main/resources/db/changelog/jdbc_migrations/000000_pgsql_extensions_only_on_postgres.yaml)
- [`application-dev.properties`](../../backend/src/main/resources/application-dev.properties)
- [`application-prod.properties`](../../backend/src/main/resources/application-prod.properties)
- [`application-inmem.properties`](../../backend/src/main/resources/application-inmem.properties)
- [`compose.yml`](../../compose.yml)
- [`JdbcConfigurationForH2.java`](../../backend/src/main/java/de/jobst/resulter/adapter/driven/jdbc/JdbcConfigurationForH2.java)
