# ADR 0001: Hexagonale Architektur im Backend

> **Deutsch** · [English](0001-hexagonal-architecture-backend.md)

- **Status:** akzeptiert
- **Datum:** 2023-12-02
- **Kontext:** Projektstart Resulter, Spring-Boot-Backend (`backend/`)

## Kontext

Resulter verarbeitet Ergebnisdateien von Orientierungsläufen und berechnet daraus
Ergebnislisten, Cup-Punkte und Auswertungen. Die fachlichen Regeln sollen unabhängig
von Web-Schicht und Datenbankzugriff bleiben. Schon der erste Commit enthält einen
ArchUnit-Test für die Schichtgrenzen, und der zweite Commit des Tages
(`saving event without hexagonal architecture conflicts`) richtet den ersten
Persistenz-Adapter an dieser Architektur aus.

## Entscheidung

1. **Drei Paketschichten unter `de.jobst.resulter`:** `domain` (Fachmodell),
   `application` (Services und Ports) und `adapter` (Anbindung nach außen).
   Spring-Konfiguration liegt getrennt in `springapp`.

2. **Ports gehören zur Anwendung.** Die Schnittstellen (`EventRepository`,
   `CertificateService` u. a.) liegen in `application.port`; die Implementierungen
   `*ServiceImpl` liegen in `application`.

3. **Adapter nach Richtung getrennt.** `adapter/driver/web` enthält die REST-Controller
   mit `dto` und `mapper`; `adapter/driven` enthält die Persistenz:
   `jdbc` (Spring Data JDBC, siehe [ADR 0002](0002-spring-data-jdbc-instead-of-jpa.de.md)) und
   `inmemory` (Repositories, die über `resulter.repository.inmemory=true` aktiviert werden).

4. **Domänenmodell mit Aggregaten und Value Objects.** Wurzel-Entitäten (Event, Cup,
   Person, …) und Value Objects (`EventId`, `EventName`, …) sind in
   `domain/aggregates.md` beschrieben. Die Paketschichten sind zusätzlich mit
   jMolecules-Annotationen (`@DomainLayer`, `@Adapter`, `@InfrastructureLayer`) markiert.

5. **Die Regeln werden im Test geprüft.** `HexagonalArchitectureTest`
   (ArchUnit, Tag `architecture`) verlangt: Domäne hängt von nichts außerhalb der
   Domäne ab, Anwendung hängt nicht von Adaptern ab, Adapter hängen nicht
   voneinander ab. `JMoleculesRulesUnitTests` prüft die jMolecules-Regeln.

## Konsequenzen

**Positiv**

- Fachlogik und Auswertungen sind ohne Web und Datenbank testbar; die
  `inmemory`-Repositories ermöglichen Tests und lokalen Betrieb ohne Datenbank.
- Verstöße gegen die Abhängigkeitsrichtung fallen im Test auf.

**Negativ / Kosten**

- Pro Aggregat gibt es Domänenklasse, Dbo, Spring-Data-Repository und Adapter, dazu
  DTO und Mapper auf der Web-Seite.
- `HexagonalArchitectureTest` liegt unter `backend/src/it/java`. Die `pom.xml` bindet
  dieses Verzeichnis nicht als Quellordner ein; der Standard-Maven-Build führt den Test
  daher vermutlich nicht aus (nicht geprüft, ob eine andere Konfiguration ihn startet).
- Die Regelsätze von ArchUnit und jMolecules müssen gepflegt werden.

## Alternativen (nicht dokumentiert, naheliegend)

- **Klassische Schichtenarchitektur** (Controller, Service, Repository ohne Ports):
  weniger Klassen, aber die Fachlogik hängt dann am Persistenzmodell.
- **Fachliche Module statt technischer Schichten** (etwa mit Spring Modulith):
  `spring-modulith-docs` ist als Abhängigkeit eingetragen, eine Modulstruktur lässt
  sich im Code nicht belegen.

## Verwandte Dokumente

- [`HexagonalArchitectureTest.java`](../../backend/src/it/java/de/jobst/resulter/HexagonalArchitectureTest.java)
- [`JMoleculesRulesUnitTests.java`](../../backend/src/test/java/de/jobst/resulter/domain/JMoleculesRulesUnitTests.java)
- [`aggregates.md`](../../backend/src/main/java/de/jobst/resulter/domain/aggregates.md)
- [`adapter/package-info.java`](../../backend/src/main/java/de/jobst/resulter/adapter/package-info.java)
- [`pom.xml` des Backends](../../backend/pom.xml)
- [README, Abschnitt Architecture](../../README.md)
