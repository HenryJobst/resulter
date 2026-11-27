# ADR 0009: Certificate layout as JSON with a flowing-text model, rendered with iText in the backend

> [Deutsch](0009-certificate-layout-as-json-flowing-text-with-itext.de.md) · **English**

- **Status:** accepted
- **Date:** 2024-03-01
- **Context:** First certificate PDFs in the backend (iText since 2024-03-01, `CertificateServiceImpl`); media (logo) in certificates since 2024-03-17

## Context

A certificate template can be designed for an event; each participant
gets a PDF from it with their result data. The template must be stored in the database,
edited by the user and turned into a PDF in the backend.

## Decision

1. **The layout is a JSON text.** It is stored as `layoutDescription` on the
   event certificate (`EventCertificate`, table `event_certificate`). Structure:
   `document` (fonts, optional margins) and `paragraphs`. Each paragraph has
   spacing (`marginTop`, `marginBottom`, `marginLeft`, `marginRight`), `tabStops`
   (position and alignment) and a list of `blocks`.

2. **Flowing-text model instead of absolute positions.** Blocks (text or medium) follow
   one another within a paragraph and jump to tab stops (`tabPosition`);
   paragraphs flow downward. There are no x/y coordinates. The model corresponds to
   iText objects: `Paragraph`, `Text`, `TabStop`, `Image`. The plan
   `docs/plans/certificate-layout-editor.md` explicitly states that a free
   drag-and-drop canvas does not fit the renderer and that switching to x/y coordinates
   would break existing layouts.

3. **iText renders in the backend.** `CertificateServiceImpl` creates an A4 PDF
   (`itext-core`), fits the background image (blank certificate from the media list)
   to the page and places the paragraphs on top. Default margins are 30/30/20/20 pt if
   the layout specifies none.

4. **Placeholders.** `TextBlockProcessor` replaces `{{GIVEN_NAME}}`, `{{FAMILY_NAME}}`,
   `{{EVENT_NAME}}`, `{{RESULT_POSITION}}`, `{{RESULT_TIME}}`, `{{CATEGORY}}` and
   `{{ORGANISATION}}` with the participant's data.

5. **Errors become visible as a PDF.** If the layout fails the schema validation
   ([ADR 0010](0010-certificate-json-schema-validation.md)), the service returns a PDF
   `LayoutDescriptionErrors.pdf` with the errors instead of the certificate.

## Consequences

**Positive**

- The layout is plain text, can be versioned, copied and extended without a
  database schema change.
- The renderer model is small and maps directly to iText objects.

**Negative / costs**

- Free placement is not possible; the position results from spacing and
  tab stops, which makes graphical editing harder
  ([ADR 0013](0013-certificate-geometry-from-pdf-measurements.md)).
- iText determines text and page breaks; the frontend cannot reproduce them exactly.
- One fixture (`bbm-mittel-2024.json`) still uses the old flat format without `blocks`
  and fails the schema validation (note in the plan).

## Alternatives (not documented, obvious candidates)

- **Absolute positioning** (x/y): rejected in the plan as "large, breaks existing layouts".
- **HTML/CSS templates with conversion to PDF**.

## Related documents

- [`CertificateServiceImpl.java`](../../backend/src/main/java/de/jobst/resulter/application/certificate/CertificateServiceImpl.java)
- [`ParagraphDefinition.java`](../../backend/src/main/java/de/jobst/resulter/application/certificate/ParagraphDefinition.java)
- [`TextBlockProcessor.java`](../../backend/src/main/java/de/jobst/resulter/application/certificate/TextBlockProcessor.java)
- [`test_layout_description_1.json`](../../backend/src/test/resources/certificate/test_layout_description_1.json)
- [`urkunden-layout-editor.md`](../plans/certificate-layout-editor.md)
- [`itext-font-integration-guide-generic.md`](../../doc/itext-font-integration-guide-generic.md)
