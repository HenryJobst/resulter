# ADR 0002: Spring Data JDBC instead of JPA

> [Deutsch](0002-spring-data-jdbc-instead-of-jpa.de.md) · **English**

- **Status:** accepted
- **Date:** 2024-01-14
- **Context:** Switch of the persistence layer in the backend (commit `04ed998f`, "major refactoring of jpa entities to spring data jdbc entities")

## Context

The backend started on 2023-12-02 with `spring-boot-starter-data-jpa`; the adapters
were called `EventRepositoryDataJpaAdapter` and were located in the package
`adapter/driven/jpa`. On 2024-01-14 the entities were converted to Spring Data JDBC in
a single commit (package `adapter/driven/jdbc`, classes `*Dbo`, `*JdbcRepository`,
`*RepositoryDataJdbcAdapter`). The commit message gives no reason. The rationale comes
from the project owner: Spring Data JDBC is more lightweight than JPA.

## Decision

1. **Spring Data JDBC is the persistence technology.** `spring-boot-starter-data-jdbc`
   is the only Spring Data dependency; `spring-boot-starter-data-jpa` was removed.

2. **Persistence model separate from the domain model.** Each aggregate has a `*Dbo`,
   a `*JdbcRepository` (Spring Data) and an adapter `*RepositoryDataJdbcAdapter` that
   implements the port from `application.port` (see
   [ADR 0001](0001-hexagonal-architecture-backend.md)).
   Relationships between aggregates run via IDs; `DboResolver` and
   `DboResolvers` resolve them during mapping.

3. **Custom queries for evaluations.** Where the standard repository is not enough,
   there are custom repositories (`EventJdbcRepositoryCustom`,
   `CupScoreListJdbcCustomRepository` and others) as well as `DashboardJdbcRepository`.

4. **Query behavior is tested.** `NPlusOneReviewIT` examines accesses for
   N+1 patterns; `datasource-proxy` and `p6spy` are listed as dependencies.

Rationale for the switch (project owner): Spring Data JDBC is more lightweight than JPA.

## Consequences

**Positive**

- No persistence context, no lazy-loading effects; what is loaded is
  visible in the adapter.
- The schema is maintained exclusively through Liquibase
  ([ADR 0003](0003-postgresql-and-liquibase.md)).

**Negative / costs**

- The mapping between Dbo and domain is written by hand.
- Complex queries and aggregate relationships require custom SQL or
  custom repository implementations.
- `.cursorrules` still describes `JpaRepository` and JPQL and is therefore outdated
  compared to the code.

## Alternatives (not documented, obvious candidates)

- **JPA with Hibernate** (initial state until 2024-01-14): less manual work for
  mapping, but heavier (persistence context, lazy loading).
- **Plain JdbcTemplate/jOOQ:** more control, but no basic repository functions.

## Related documents

- [`pom.xml` of the backend](../../backend/pom.xml)
- [`adapter/driven/jdbc`](../../backend/src/main/java/de/jobst/resulter/adapter/driven/jdbc)
- [`EventRepositoryDataJdbcAdapter.java`](../../backend/src/main/java/de/jobst/resulter/adapter/driven/jdbc/EventRepositoryDataJdbcAdapter.java)
- [`NPlusOneReviewIT.java`](../../backend/src/test/java/de/jobst/resulter/adapter/driver/web/NPlusOneReviewIT.java)
- [`.cursorrules`](../../.cursorrules)
- [README, section Technologies](../../README.md)
