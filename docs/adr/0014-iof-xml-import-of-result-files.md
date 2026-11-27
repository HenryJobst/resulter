# ADR 0014: Import of result files in the IOF XML standard via generated JAXB classes

> [Deutsch](0014-iof-xml-import-of-result-files.de.md) · **English**

- **Status:** accepted
- **Date:** 2023-12-09
- **Context:** `XMLImportService` since 2023-12-09; `IOF.xsd` since 2023-12-02; the JAXB classes have been generated during the build since 2023-12-07 (revert of the commit "drop jaxb2-maven-plugin because no longer needed")

## Context

Resulter gets its data from result files of orienteering events. The README
describes it this way: result files are read in, and result lists are created from them.
The directory `import_files/` contains sample files from various events
(XML with split times). The project owner justifies the choice of format by saying that
IOF XML is the standard in orienteering.

## Decision

1. **The input format is IOF Data Standard 3.0 (XML).** The schema `IOF.xsd` is in the
   repository (`adapter/driver/web/IOF.xsd`, namespace
   `http://www.orienteering.org/datastandard/3.0`).

2. **Classes are generated from the schema.** The `jaxb2-maven-plugin` generates the package
   `de.jobst.resulter.adapter.driver.web.jaxb` with `xjc` during
   `generate-sources` in `target/generated-sources` (bindings in `global.xjb`). `jaxb-runtime` reads
   the files; `XmlParser` and `XmlConfig` configure the reading.

3. **The import lives in the web adapter.** `POST /upload` (`XmlController`, file as
   `file` parameter, at most 20 MB according to `spring.servlet.multipart`) calls
   `XMLImportService.importFile`. The service creates event, countries,
   organizations, persons, courses, result lists, races and split times through the
   application services. It translates the JAXB types into the domain model so that the
   domain knows nothing about JAXB ([ADR 0001](0001-hexagonal-architecture-backend.md)).
   The import is transactional (`@Transactional`).

4. **Upload by administrators only.** `POST /upload` is restricted to the role `ADMIN`
   in the security configuration.

5. **Filtered starters.** Results with the status "not started" or "not entered"
   are not imported (`isReallyStarted`); per race, the matching race number counts.

## Consequences

**Positive**

- A standardized, schema-described exchange format.
- Typed processing instead of manual XML parsing.

**Negative / costs**

- The generated classes only exist after the build; without a prior build they are
  missing in the IDE.
- At around 28 KB, the import service is one of the largest classes of the backend and
  depends on many application services; it lives in the adapter, not in the application.
- Only IOF 3.0 is supported; files in other formats or versions are not
  read (not explicitly verified).

## Alternatives (not documented, obvious candidates)

- **Manual XML parsing** (DOM/StAX) without generated classes.
- **A custom or different exchange format** (CSV, JSON).

## Related documents

- [`IOF.xsd`](../../backend/src/main/java/de/jobst/resulter/adapter/driver/web/IOF.xsd)
- [`XMLImportService.java`](../../backend/src/main/java/de/jobst/resulter/adapter/driver/web/XMLImportService.java)
- [`XmlController.java`](../../backend/src/main/java/de/jobst/resulter/adapter/driver/web/XmlController.java)
- [`global.xjb`](../../backend/src/main/resources/global.xjb)
- [`pom.xml` of the backend](../../backend/pom.xml)
- [`XMLImportServiceTest.java`](../../backend/src/test/java/de/jobst/resulter/adapter/driver/web/XMLImportServiceTest.java)
