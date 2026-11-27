# ADR 0014: Import von Ergebnisdateien im IOF-XML-Standard über generierte JAXB-Klassen

> **Deutsch** · [English](0014-iof-xml-import-of-result-files.md)

- **Status:** akzeptiert
- **Datum:** 2023-12-09
- **Kontext:** `XMLImportService` seit 2023-12-09; `IOF.xsd` seit 2023-12-02; die JAXB-Klassen werden seit 2023-12-07 beim Build erzeugt (Revert des Commits „drop jaxb2-maven-plugin because no longer needed“)

## Kontext

Resulter bekommt seine Daten aus Ergebnisdateien von Orientierungsläufen. Das README
beschreibt das so: Ergebnisdateien werden eingelesen, daraus entstehen Ergebnislisten.
Im Verzeichnis `import_files/` liegen Beispieldateien verschiedener Veranstaltungen
(XML mit Zwischenzeiten). Die Wahl des Formats begründet der Projektinhaber damit, dass
IOF-XML der Standard im Orientierungslauf ist.

## Entscheidung

1. **Eingangsformat ist IOF Data Standard 3.0 (XML).** Das Schema `IOF.xsd` liegt im
   Repository (`adapter/driver/web/IOF.xsd`, Namespace
   `http://www.orienteering.org/datastandard/3.0`).

2. **Klassen werden aus dem Schema generiert.** Das `jaxb2-maven-plugin` erzeugt bei
   `generate-sources` mit `xjc` das Paket `de.jobst.resulter.adapter.driver.web.jaxb`
   in `target/generated-sources` (Bindings in `global.xjb`). `jaxb-runtime` liest die
   Dateien; `XmlParser` und `XmlConfig` konfigurieren das Einlesen.

3. **Der Import liegt im Web-Adapter.** `POST /upload` (`XmlController`, Datei als
   `file`-Parameter, höchstens 20 MB laut `spring.servlet.multipart`) ruft
   `XMLImportService.importFile` auf. Der Dienst legt Veranstaltung, Länder,
   Organisationen, Personen, Strecken, Ergebnislisten, Rennen und Zwischenzeiten über die
   Anwendungsdienste an. Er übersetzt die JAXB-Typen in das Domänenmodell, sodass die
   Domäne nichts von JAXB weiß ([ADR 0001](0001-hexagonal-architecture-backend.de.md)).
   Der Import ist transaktional (`@Transactional`).

4. **Nur der Upload durch Administratoren.** `POST /upload` ist in der
   Sicherheitskonfiguration auf die Rolle `ADMIN` beschränkt.

5. **Gefilterte Starter.** Ergebnisse mit Status „nicht gestartet“ oder „nicht gemeldet“
   werden nicht übernommen (`isReallyStarted`); pro Rennen zählt die passende Rennnummer.

## Konsequenzen

**Positiv**

- Ein standardisiertes, schemabeschriebenes Austauschformat.
- Typisierte Verarbeitung statt manuellem XML-Parsing.

**Negativ / Kosten**

- Die generierten Klassen entstehen erst beim Build; ohne vorherigen Build fehlen
  sie in der IDE.
- Der Importdienst ist mit rund 28 KB eine der größten Klassen des Backends und
  hängt von vielen Anwendungsdiensten ab; er liegt im Adapter, nicht in der Anwendung.
- Nur IOF 3.0 wird unterstützt; Dateien in anderen Formaten oder Versionen werden nicht
  gelesen (nicht ausdrücklich geprüft).

## Alternativen (nicht dokumentiert, naheliegend)

- **Manuelles XML-Parsing** (DOM/StAX) ohne generierte Klassen.
- **Eigenes oder anderes Austauschformat** (CSV, JSON).

## Verwandte Dokumente

- [`IOF.xsd`](../../backend/src/main/java/de/jobst/resulter/adapter/driver/web/IOF.xsd)
- [`XMLImportService.java`](../../backend/src/main/java/de/jobst/resulter/adapter/driver/web/XMLImportService.java)
- [`XmlController.java`](../../backend/src/main/java/de/jobst/resulter/adapter/driver/web/XmlController.java)
- [`global.xjb`](../../backend/src/main/resources/global.xjb)
- [`pom.xml` des Backends](../../backend/pom.xml)
- [`XMLImportServiceTest.java`](../../backend/src/test/java/de/jobst/resulter/adapter/driver/web/XMLImportServiceTest.java)
