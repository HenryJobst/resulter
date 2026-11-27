# ADR 0013: Geometry of the graphical certificate view from PDF measurements

> [Deutsch](0013-certificate-geometry-from-pdf-measurements.de.md) · **English**

- **Status:** accepted
- **Date:** 2026-11-24
- **Context:** Commit `200477f2` ("graphical layout view and margin fix"), follow-up in `56f60966` (2026-11-25, "graphical view matches the PDF")

## Context

The graphical view ([ADR 0012](0012-certificate-editor-with-three-modes.md)) is meant to show
where iText places the blocks. The position results from paragraph spacing,
line height, tab stops and font ([ADR 0009](0009-certificate-layout-as-json-flowing-text-with-itext.md));
the frontend cannot run iText and has no access to the backend logic.

## Decision

1. **The frontend replicates the typesetting rules.** `model/certificateCanvas.ts` converts
   the layout JSON into boxes (`CanvasBox`) with a position in points from the top left
   corner of the page. It computes them purely from the layout, without a backend call.

2. **The constants were measured on real PDFs.** The PDFs measured are those the
   backend renders (text positions read out with pdf.js):
   - Paragraph spacing above and below is 4 pt; a specified spacing replaces it
     (it is not added).
   - Line height and baseline depend on the font and are fitted per font
     (factor times font size), e.g. Helvetica 1.499 and 1.056, Amaranth 1.964 and 1.426.
     The values were fitted to the PDFs of 13 real certificates (deviation
     under 0.1 pt according to a comment, at most 1 pt according to the commit message). Tangerine is
     an estimate.
   - Tab stops are used in ascending order; a block is placed at the next stop after
     the end of the previous one, after that at the next multiple of 50. Without tab stops,
     the iText default applies: one centered stop at 261.5.
   - Default font size 12; image height roughly width times 0.5.

3. **The measured values are pinned by tests.**
   `test/features/certificate/certificateCanvas.spec.ts` pins them; if the
   backend rendering changes, the test fails or the values must be measured again.

4. **Text width measured in the browser.** The original estimate of the text width was
   replaced on 2026-11-25 by measuring in the browser with the real font
   ([ADR 0011](0011-certificate-fonts.md)). Real font metrics are still missing for
   fonts that are not embedded.

5. **Sample data as in the backend preview.** Placeholders are replaced by the same
   dummy values (`Max Mustermann`, `Beispiel-OL`, ...).

6. **The PDF preview remains the source of truth.** The document states explicitly: the view
   is an approximation, the preview in the PDF decides.

## Consequences

**Positive**

- Graphical editing is possible without a round trip to the backend and, according to
  the measurement, hits paragraphs and tab stops to within about 1 pt.
- The assumptions are traceable as constants and tests.

**Negative / costs**

- The values are a copy of the iText behavior; a change of the iText version,
  the fonts or the renderer can make them inaccurate (new measurement needed).
- Line and page breaks of long texts are not represented; the height of images is
  estimated because the real aspect ratio is missing.
- Custom `.ttf` fonts are shown with a similar web font.
- The background image is the backend thumbnail and therefore blurry.

## Alternatives (not documented, obvious candidates)

- **The backend delivers block positions** (exact widths and breaks) and the frontend
  overlays boxes on the PDF preview. The plan names this as a possible next stage.
- **Only the PDF preview, without its own geometry.**

## Related documents

- [`certificateCanvas.ts`](../../frontend/src/features/certificate/model/certificateCanvas.ts)
- [`certificateCanvas.spec.ts`](../../frontend/test/features/certificate/certificateCanvas.spec.ts)
- [`urkunden-layout-editor.md`](../plans/certificate-layout-editor.md)
- [`CertificateServiceImpl.java`](../../backend/src/main/java/de/jobst/resulter/application/certificate/CertificateServiceImpl.java)
