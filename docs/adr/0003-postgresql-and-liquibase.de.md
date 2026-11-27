# ADR 0003: PostgreSQL als Datenbank, Liquibase für das Schema, H2 für lokale Profile

> **Deutsch** · [English](0003-postgresql-and-liquibase.md)

- **Status:** akzeptiert
- **Datum:** 2023-12-27
- **Kontext:** Commit `b0f5b92b` („Postgresql database as backend database (via docker-compose)“); Liquibase und H2 sind seit dem ersten Commit (2023-12-02) im Backend

## Kontext

Der erste Stand des Backends nutzte H2 und Liquibase. Am 2023-12-27 kam PostgreSQL
als Backend-Datenbank hinzu, gestartet über Docker Compose. Das Schema ist in
Liquibase-Changelogs beschrieben und wird nicht aus den Entitäten erzeugt.

## Entscheidung

1. **PostgreSQL ist die Datenbank für Entwicklung und Betrieb.** Das Profil `dev`
   verbindet sich mit einer lokalen PostgreSQL-Instanz; `compose.yml` im
   Repository-Wurzelverzeichnis (im Backend verlinkt) startet sie (`postgres:16`), und
   `spring-boot-docker-compose` kann sie beim Start hochfahren
   (`spring.docker.compose.lifecycle-management=start_only`). Das Profil `prod` liest
   Host, Port und Datenbankname aus Umgebungsvariablen.

2. **Liquibase besitzt das Schema.** `db.changelog-master.yaml` bindet per `includeAll`
   alle YAML-Dateien aus `db/changelog/jdbc_migrations` ein (derzeit 43 Dateien,
   nummeriert `000000` bis `000042` plus Anfangsdaten `0001001` bis `0001003` für
   Länder, Organisationen und Cups). `spring.liquibase.enabled=true` in `dev` und
   `prod`; `liquibase.properties` setzt `strict=true`. Eine Änderung am Schema ist eine
   neue Changelog-Datei.

3. **PostgreSQL-spezifisches ist abgesichert.** Der Changeset
   `000000_pgsql_extensions_only_on_postgres` legt die Erweiterung `hstore` an und
   läuft nur auf PostgreSQL (`dbms: postgresql`, sonst `MARK_RAN`).

4. **H2 bleibt für datenbanklose lokale Läufe.** Das Profil `inmem`
   (`InmemDatasourceConfig`, `JdbcConfigurationForH2`) nutzt eine H2-Datei
   `./testdb`; `src/test/resources/persistence.properties` zeigt auf eine
   H2-In-Memory-Datenbank. Die Integrationsumgebung für E2E-Tests nutzt dagegen
   echtes PostgreSQL in Testcontainers (siehe
   [ADR 0005](0005-e2e-database-isolation.de.md)).

Begründung für PostgreSQL gegenüber H2 im Betrieb (Projektinhaber): PostgreSQL ist die beste
verfügbare Open-Source-Datenbank für professionelle Anwendungen. Sie ist performant,
funktionsreich und seit langer Zeit gut gepflegt.

## Konsequenzen

**Positiv**

- Nachvollziehbare, versionierte Schemaänderungen; dieselben Changelogs laufen in
  Entwicklung, Betrieb und in den E2E-Testdatenbanken.
- Lokale Entwicklung ohne Docker ist mit `inmem` möglich.

**Negativ / Kosten**

- Unterschiede zwischen H2 und PostgreSQL (etwa `hstore`) müssen in den Changesets
  berücksichtigt werden; H2-Läufe sind kein vollständiger Ersatz für PostgreSQL.
- Datenmigrationen und Änderungen am Schema erfordern manuell geschriebene Changesets.

## Alternativen (nicht dokumentiert, naheliegend)

- **Schema aus den Entitäten erzeugen** (`hbm2ddl`-artig): schneller, aber ohne
  versionierte Migrationen für den Betrieb. `persistence.properties` enthält noch
  `hibernate.hbm2ddl.auto=create` als Überbleibsel der JPA-Zeit.
- **Flyway** statt Liquibase.

## Verwandte Dokumente

- [`db.changelog-master.yaml`](../../backend/src/main/resources/db/changelog/db.changelog-master.yaml)
- [`jdbc_migrations`](../../backend/src/main/resources/db/changelog/jdbc_migrations)
- [`000000_pgsql_extensions_only_on_postgres.yaml`](../../backend/src/main/resources/db/changelog/jdbc_migrations/000000_pgsql_extensions_only_on_postgres.yaml)
- [`application-dev.properties`](../../backend/src/main/resources/application-dev.properties)
- [`application-prod.properties`](../../backend/src/main/resources/application-prod.properties)
- [`application-inmem.properties`](../../backend/src/main/resources/application-inmem.properties)
- [`compose.yml`](../../compose.yml)
- [`JdbcConfigurationForH2.java`](../../backend/src/main/java/de/jobst/resulter/adapter/driven/jdbc/JdbcConfigurationForH2.java)
