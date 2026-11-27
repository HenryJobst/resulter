# Architecture Decision Records (ADR)

> [Deutsch](README.de.md) · **English**

An ADR records an architecture or technology decision: context, the decision taken,
consequences and alternatives. ADRs are not overwritten when something changes; a new
decision supersedes the old one, and the old one is marked "superseded". This keeps it
traceable why the code looks the way it does.

The ADRs in this directory were compiled retroactively from the code, configuration,
scripts, documents and Git history of the repository. The date is that of the first
commit that introduces the decision. Where a rationale is not documented in the
repository, the respective ADR says so explicitly.

The English text is the primary version; the German version has the same file name with
the suffix `.de.md`.

## Status values

| Status | Meaning |
|---|---|
| proposed | Up for discussion, not yet implemented |
| accepted | In force and implemented in the repository |
| superseded | Replaced by a newer ADR (reference to it in the header) |
| rejected | Examined and deliberately not implemented |

## Creating a new ADR

1. Pick the next free number (four digits, consecutive) and create a file
   `NNNN-short-slug.md`. The German version carries the suffix `.de.md`
   (`NNNN-short-slug.de.md`).
2. Structure as in the existing ADRs: title `# ADR NNNN: …`; header lines Status, Date,
   Context; sections Context, Decision (numbered), Consequences (Positive /
   Negative / costs), Alternatives, Related documents (relative links to real files).
3. Record only what can be verified in the repository; mark anything not documented as
   such. Do not include credentials, tokens or passwords.
4. If the ADR supersedes an older one, set the status of the older one to "superseded"
   and link both.
5. Add to the table below.

## Overview

| No. | Title | Status | Date |
|---|---|---|---|
| 0001 | [Hexagonal architecture in the backend](0001-hexagonal-architecture-backend.md) | accepted | 2023-12-02 |
| 0002 | [Spring Data JDBC instead of JPA](0002-spring-data-jdbc-instead-of-jpa.md) | accepted | 2024-01-14 |
| 0003 | [PostgreSQL as database, Liquibase for the schema, H2 for local profiles](0003-postgresql-and-liquibase.md) | accepted | 2023-12-27 |
| 0004 | [Backend-for-Frontend with Keycloak and session cookie](0004-bff-with-keycloak-and-session-cookie.md) | accepted | 2025-12-25 |
| 0005 | [E2E tests with a separate database per test (X-DB-Identifier)](0005-e2e-database-isolation.md) | accepted | 2024-07-01 |
| 0006 | [Test strategy](0006-test-strategy.md) | accepted | 2023-12-17 |
| 0007 | [Frontend stack and feature-oriented structure](0007-frontend-stack-and-structure.md) | accepted | 2023-12-17 |
| 0008 | [OpenVue instead of PrimeVue as UI library](0008-openvue-instead-of-primevue.md) | accepted | 2026-11-24 |
| 0009 | [Certificate layout as JSON with a flowing-text model, rendering with iText in the backend](0009-certificate-layout-as-json-flowing-text-with-itext.md) | accepted | 2024-03-01 |
| 0010 | [JSON schema validation of the certificate layout in backend and frontend](0010-certificate-json-schema-validation.md) | accepted | 2024-05-25 |
| 0011 | [Embedded certificate fonts, licenses and loading in the editor](0011-certificate-fonts.md) | accepted | 2024-04-04 |
| 0012 | [Certificate editor with three modes (graphical, form, JSON)](0012-certificate-editor-with-three-modes.md) | accepted | 2026-11-24 |
| 0013 | [Geometry of the graphical certificate view from PDF measurements](0013-certificate-geometry-from-pdf-measurements.md) | accepted | 2026-11-24 |
| 0014 | [Import of result files in the IOF XML standard via generated JAXB classes](0014-iof-xml-import-of-result-files.md) | accepted | 2023-12-09 |
| 0015 | [Evaluations as domain strategies and application services (cups, split time analysis)](0015-evaluations-cup-strategies-and-split-time-analysis.md) | accepted | 2024-01-09 |
| 0016 | [Container images with Paketo Buildpacks](0016-container-images-with-paketo-buildpacks.md) | accepted | 2026-01-23 |
| 0017 | [Releases via release branches and version script](0017-release-branches-and-version-script.md) | accepted | 2026-05-22 |
| 0018 | [Dependency updates with Renovate and controlled auto-merge](0018-renovate-and-auto-merge.md) | accepted | 2026-05-21 |
| 0019 | [Operational metrics with Micrometer, Prometheus and Grafana](0019-observability-with-prometheus-and-grafana.md) | accepted | 2025-02-16 |
| 0020 | [License CC BY-NC-ND 4.0](0020-license-cc-by-nc-nd-4-0.md) | accepted | 2023-12-16 |
| 0021 | [Bilingual application and documentation (English and German)](0021-bilingual-application-and-documentation.md) | accepted | 2026-11-27 |
