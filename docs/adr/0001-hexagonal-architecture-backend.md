# ADR 0001: Hexagonal architecture in the backend

> [Deutsch](0001-hexagonal-architecture-backend.de.md) · **English**

- **Status:** accepted
- **Date:** 2023-12-02
- **Context:** Start of the Resulter project, Spring Boot backend (`backend/`)

## Context

Resulter processes result files from orienteering events and uses them to compute
result lists, cup points and evaluations. The business rules should remain independent
of the web layer and database access. The very first commit contains an ArchUnit test
for the layer boundaries, and the second commit of that day
(`saving event without hexagonal architecture conflicts`) aligns the first persistence
adapter with this architecture.

## Decision

1. **Three package layers under `de.jobst.resulter`:** `domain` (business model),
   `application` (services and ports) and `adapter` (connections to the outside).
   Spring configuration is kept separately in `springapp`.

2. **Ports belong to the application.** The interfaces (`EventRepository`,
   `CertificateService` and others) are located in `application.port`; the
   implementations `*ServiceImpl` are located in `application`.

3. **Adapters separated by direction.** `adapter/driver/web` contains the REST
   controllers with `dto` and `mapper`; `adapter/driven` contains persistence:
   `jdbc` (Spring Data JDBC, see [ADR 0002](0002-spring-data-jdbc-instead-of-jpa.md)) and
   `inmemory` (repositories activated via `resulter.repository.inmemory=true`).

4. **Domain model with aggregates and value objects.** Root entities (Event, Cup,
   Person, …) and value objects (`EventId`, `EventName`, …) are described in
   `domain/aggregates.md`. The package layers are additionally marked with
   jMolecules annotations (`@DomainLayer`, `@Adapter`, `@InfrastructureLayer`).

5. **The rules are verified in tests.** `HexagonalArchitectureTest`
   (ArchUnit, tag `architecture`) requires: the domain depends on nothing outside the
   domain, the application does not depend on adapters, adapters do not depend on each
   other. `JMoleculesRulesUnitTests` checks the jMolecules rules.

## Consequences

**Positive**

- Business logic and evaluations can be tested without web and database; the
  `inmemory` repositories allow tests and local operation without a database.
- Violations of the dependency direction are caught in the test.

**Negative / costs**

- Each aggregate has a domain class, Dbo, Spring Data repository and adapter, plus
  DTO and mapper on the web side.
- `HexagonalArchitectureTest` is located under `backend/src/it/java`. The `pom.xml` does
  not include this directory as a source folder; the standard Maven build therefore
  probably does not run the test (not verified whether another configuration starts it).
- The rule sets of ArchUnit and jMolecules have to be maintained.

## Alternatives (not documented, obvious candidates)

- **Classic layered architecture** (controller, service, repository without ports):
  fewer classes, but the business logic then depends on the persistence model.
- **Business modules instead of technical layers** (for example with Spring Modulith):
  `spring-modulith-docs` is listed as a dependency, but a module structure cannot be
  verified in the code.

## Related documents

- [`HexagonalArchitectureTest.java`](../../backend/src/it/java/de/jobst/resulter/HexagonalArchitectureTest.java)
- [`JMoleculesRulesUnitTests.java`](../../backend/src/test/java/de/jobst/resulter/domain/JMoleculesRulesUnitTests.java)
- [`aggregates.md`](../../backend/src/main/java/de/jobst/resulter/domain/aggregates.md)
- [`adapter/package-info.java`](../../backend/src/main/java/de/jobst/resulter/adapter/package-info.java)
- [`pom.xml` of the backend](../../backend/pom.xml)
- [README, section Architecture](../../README.md)
