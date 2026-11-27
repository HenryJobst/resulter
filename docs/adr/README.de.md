# Architecture Decision Records (ADR)

> **Deutsch** · [English](README.md)

Ein ADR hält eine Architektur- oder Technologieentscheidung fest: Kontext, getroffene
Entscheidung, Konsequenzen und Alternativen. ADR werden nicht überschrieben, wenn sich
etwas ändert; eine neue Entscheidung ersetzt die alte, und die alte wird als „ersetzt“
markiert. So bleibt nachvollziehbar, warum der Code so aussieht.

Die ADR dieses Verzeichnisses wurden nachträglich aus Code, Konfiguration, Skripten,
Dokumenten und der Git-Historie des Repositorys zusammengestellt. Das Datum ist das des
ersten Commits, der die Entscheidung einführt. Wo eine Begründung im Repository nicht
belegt ist, steht das ausdrücklich im jeweiligen ADR.

## Statuswerte

| Status | Bedeutung |
|---|---|
| vorgeschlagen | Zur Diskussion, noch nicht umgesetzt |
| akzeptiert | Gilt und ist im Repository umgesetzt |
| ersetzt | Durch ein neueres ADR abgelöst (Verweis darauf im Kopf) |
| verworfen | Geprüft und bewusst nicht umgesetzt |

## Neues ADR anlegen

1. Nächste freie Nummer wählen (vierstellig, fortlaufend) und eine Datei
   `NNNN-kurzer-slug.md` anlegen.
2. Aufbau wie in den vorhandenen ADR: Titel `# ADR NNNN: …`; Kopfzeilen Status, Datum,
   Kontext; Abschnitte Kontext, Entscheidung (nummeriert), Konsequenzen (Positiv /
   Negativ / Kosten), Alternativen, Verwandte Dokumente (relative Links auf echte Dateien).
3. Nur festhalten, was im Repository belegbar ist; nicht Dokumentiertes als solches
   kennzeichnen. Keine Zugangsdaten, Tokens oder Passwörter aufnehmen.
4. Ersetzt das ADR ein älteres, dessen Status auf „ersetzt“ setzen und beide verlinken.
5. Die Tabelle unten ergänzen.

## Übersicht

| Nr. | Titel | Status | Datum |
|---|---|---|---|
| 0001 | [Hexagonale Architektur im Backend](0001-hexagonal-architecture-backend.de.md) | akzeptiert | 2023-12-02 |
| 0002 | [Spring Data JDBC statt JPA](0002-spring-data-jdbc-instead-of-jpa.de.md) | akzeptiert | 2024-01-14 |
| 0003 | [PostgreSQL als Datenbank, Liquibase für das Schema, H2 für lokale Profile](0003-postgresql-and-liquibase.de.md) | akzeptiert | 2023-12-27 |
| 0004 | [Backend-for-Frontend mit Keycloak und Sitzungscookie](0004-bff-with-keycloak-and-session-cookie.de.md) | akzeptiert | 2025-12-25 |
| 0005 | [E2E-Tests mit eigener Datenbank je Test (X-DB-Identifier)](0005-e2e-database-isolation.de.md) | akzeptiert | 2024-07-01 |
| 0006 | [Teststrategie](0006-test-strategy.de.md) | akzeptiert | 2023-12-17 |
| 0007 | [Frontend-Stack und feature-orientierte Struktur](0007-frontend-stack-and-structure.de.md) | akzeptiert | 2023-12-17 |
| 0008 | [OpenVue statt PrimeVue als UI-Bibliothek](0008-openvue-instead-of-primevue.de.md) | akzeptiert | 2026-11-24 |
| 0009 | [Urkunden-Layout als JSON mit Fließtextmodell, Rendering mit iText im Backend](0009-certificate-layout-as-json-flowing-text-with-itext.de.md) | akzeptiert | 2024-03-01 |
| 0010 | [JSON-Schema-Validierung des Urkunden-Layouts in Backend und Frontend](0010-certificate-json-schema-validation.de.md) | akzeptiert | 2024-05-25 |
| 0011 | [Eingebundene Urkunden-Schriften, Lizenzen und Laden im Editor](0011-certificate-fonts.de.md) | akzeptiert | 2024-04-04 |
| 0012 | [Urkunden-Editor mit drei Modi (Grafisch, Formular, JSON)](0012-certificate-editor-with-three-modes.de.md) | akzeptiert | 2026-11-24 |
| 0013 | [Geometrie der grafischen Urkundenansicht aus PDF-Messungen](0013-certificate-geometry-from-pdf-measurements.de.md) | akzeptiert | 2026-11-24 |
| 0014 | [Import von Ergebnisdateien im IOF-XML-Standard über generierte JAXB-Klassen](0014-iof-xml-import-of-result-files.de.md) | akzeptiert | 2023-12-09 |
| 0015 | [Auswertungen als Domänen-Strategien und Anwendungsdienste (Cups, Splitzeiten-Analyse)](0015-evaluations-cup-strategies-and-split-time-analysis.de.md) | akzeptiert | 2024-01-09 |
| 0016 | [Container-Images mit Paketo Buildpacks](0016-container-images-with-paketo-buildpacks.de.md) | akzeptiert | 2026-01-23 |
| 0017 | [Release über Release-Branches und Versionsskript](0017-release-branches-and-version-script.de.md) | akzeptiert | 2026-05-22 |
| 0018 | [Dependency-Updates mit Renovate und kontrolliertem Auto-Merge](0018-renovate-and-auto-merge.de.md) | akzeptiert | 2026-05-21 |
| 0019 | [Betriebsmetriken mit Micrometer, Prometheus und Grafana](0019-observability-with-prometheus-and-grafana.de.md) | akzeptiert | 2025-02-16 |
| 0020 | [Lizenz CC BY-NC-ND 4.0](0020-license-cc-by-nc-nd-4-0.de.md) | akzeptiert | 2023-12-16 |
| 0021 | [Zweisprachige Anwendung und Dokumentation (Englisch und Deutsch)](0021-bilingual-application-and-documentation.de.md) | akzeptiert | 2026-11-27 |
