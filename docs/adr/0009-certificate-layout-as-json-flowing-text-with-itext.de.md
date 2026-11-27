# ADR 0009: Urkunden-Layout als JSON mit Fließtextmodell, Rendering mit iText im Backend

> **Deutsch** · [English](0009-certificate-layout-as-json-flowing-text-with-itext.md)

- **Status:** akzeptiert
- **Datum:** 2024-03-01
- **Kontext:** Erste Urkunden-PDFs im Backend (iText ab 2024-03-01, `CertificateServiceImpl`); Medien (Logo) in Urkunden ab 2024-03-17

## Kontext

Zu einer Veranstaltung lässt sich eine Urkundenvorlage gestalten; jeder Teilnehmer
bekommt daraus ein PDF mit seinen Ergebnisdaten. Die Vorlage muss in der Datenbank
gespeichert, vom Benutzer bearbeitet und im Backend in ein PDF verwandelt werden.

## Entscheidung

1. **Das Layout ist ein JSON-Text.** Er liegt als `layoutDescription` an der
   Veranstaltungsurkunde (`EventCertificate`, Tabelle `event_certificate`). Aufbau:
   `document` (Schriften, optional Ränder) und `paragraphs`. Jeder Absatz hat
   Abstände (`marginTop`, `marginBottom`, `marginLeft`, `marginRight`), `tabStops`
   (Position und Ausrichtung) und eine Liste von `blocks`.

2. **Fließtextmodell statt absoluter Positionen.** Blöcke (Text oder Medium) stehen
   hintereinander in einem Absatz und springen auf Tabulatoren (`tabPosition`);
   Absätze fließen untereinander. Es gibt keine x/y-Koordinaten. Das Modell entspricht
   iText-Objekten: `Paragraph`, `Text`, `TabStop`, `Image`. Der Plan
   `docs/plans/certificate-layout-editor.de.md` hält ausdrücklich fest, dass ein freier
   Drag-and-Drop-Canvas nicht zum Renderer passt und eine Umstellung auf x/y-Koordinaten
   bestehende Layouts brechen würde.

3. **iText rendert im Backend.** `CertificateServiceImpl` erzeugt ein A4-PDF
   (`itext-core`), legt das Hintergrundbild (leere Urkunde aus der Medienliste) passend
   auf die Seite und setzt die Absätze darüber. Standardränder sind 30/30/20/20 pt, wenn
   das Layout keine angibt.

4. **Platzhalter.** `TextBlockProcessor` ersetzt `{{GIVEN_NAME}}`, `{{FAMILY_NAME}}`,
   `{{EVENT_NAME}}`, `{{RESULT_POSITION}}`, `{{RESULT_TIME}}`, `{{CATEGORY}}` und
   `{{ORGANISATION}}` durch die Daten des Teilnehmers.

5. **Fehler werden als PDF sichtbar.** Besteht das Layout die Schema-Prüfung nicht
   ([ADR 0010](0010-certificate-json-schema-validation.de.md)), liefert der Dienst statt der
   Urkunde ein PDF `LayoutDescriptionErrors.pdf` mit den Fehlern.

## Konsequenzen

**Positiv**

- Das Layout ist ein einfacher Text, lässt sich versionieren, kopieren und ohne
  Schemaänderung der Datenbank erweitern.
- Das Renderer-Modell ist klein und bildet direkt auf iText-Objekte ab.

**Negativ / Kosten**

- Freie Platzierung ist nicht möglich; die Position ergibt sich aus Abständen und
  Tabulatoren, was die grafische Bearbeitung erschwert
  ([ADR 0013](0013-certificate-geometry-from-pdf-measurements.de.md)).
- Text- und Seitenumbruch bestimmt iText; das Frontend kann sie nicht exakt nachbilden.
- Ein Fixture (`bbm-mittel-2024.json`) nutzt noch das alte flache Format ohne `blocks`
  und besteht die Schema-Prüfung nicht (Hinweis im Plan).

## Alternativen (nicht dokumentiert, naheliegend)

- **Absolute Positionierung** (x/y): im Plan als „groß, bricht bestehende Layouts“
  verworfen.
- **HTML-/CSS-Vorlagen mit Konvertierung nach PDF**.

## Verwandte Dokumente

- [`CertificateServiceImpl.java`](../../backend/src/main/java/de/jobst/resulter/application/certificate/CertificateServiceImpl.java)
- [`ParagraphDefinition.java`](../../backend/src/main/java/de/jobst/resulter/application/certificate/ParagraphDefinition.java)
- [`TextBlockProcessor.java`](../../backend/src/main/java/de/jobst/resulter/application/certificate/TextBlockProcessor.java)
- [`test_layout_description_1.json`](../../backend/src/test/resources/certificate/test_layout_description_1.json)
- [`urkunden-layout-editor.md`](../plans/certificate-layout-editor.de.md)
- [`itext-font-integration-guide-generic.md`](../../doc/itext-font-integration-guide-generic.de.md)
