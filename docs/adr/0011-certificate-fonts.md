# ADR 0011: Embedded certificate fonts, licenses and loading in the editor

> [Deutsch](0011-certificate-fonts.de.md) · **English**

- **Status:** accepted
- **Date:** 2024-04-04
- **Context:** Commits `ced63824` and `e9b93051` (fonts in the backend, 2024-04-04), Montserrat on 2025-10-06, fonts in the frontend on 2026-11-25 (`56f60966`)

## Context

Certificates should be able to use more than the standard PDF fonts. The font is specified
in the layout JSON ([ADR 0009](0009-certificate-layout-as-json-flowing-text-with-itext.md)).
Since the graphical view ([ADR 0012](0012-certificate-editor-with-three-modes.md)), the
frontend also needs the same fonts so that the preview shows the real characters.

## Decision

1. **Fonts are stored as TrueType files in the backend.**
   `backend/src/main/resources/fonts` contains Amaranth (Regular, Bold, Italic,
   BoldItalic), Montserrat (four styles), Satisfy and Tangerine (Regular each).
   `CertificateServiceImpl.loadFont` loads them from the classpath (`fonts/<file name>`);
   the standard PDF fonts (Courier, Helvetica, Times-Roman, Symbol, ZapfDingbats)
   are available without a file.

2. **Approval through the JSON schema.** Only font names from `definitions.font` in
   `certificate_schema.json` are valid ([ADR 0010](0010-certificate-json-schema-validation.md)).
   A new font needs the file in the backend, a schema entry and
   entries in `FONTS`, `BOLD_FONTS` and `ITALIC_FONTS` in the frontend
   (`certificateLayout.ts`). The procedure is described in
   `doc/itext-font-integration-guide-generic.md` (bilingual).

3. **Licenses.** According to `assets/fonts/README.md`, Amaranth, Montserrat and
   Tangerine are under the SIL Open Font License 1.1, Satisfy under the Apache License 2.0.
   The license texts are in the frontend next to the font copies
   (`OFL-amaranth.txt`, `OFL-montserrat.txt`, `OFL-tangerine.txt`,
   `LICENSE-satisfy-apache-2.0.txt`); the information comes from the Google Fonts repository.

4. **Copies for the frontend.** The frontend contains copies of the same files under
   `src/features/certificate/assets/fonts`, included through `certificateFonts.css`
   (`@font-face`, `font-display: swap`).

5. **Loaded only on the certificate pages.** `certificateFonts.css` is imported only by
   `CertificateLayoutCanvas.vue`. The certificate pages are separate
   routes that are loaded later via `import()`; the browser fetches the font files only
   when a text uses them. They are not loaded on the other pages.

## Consequences

**Positive**

- The graphical view shows the same characters as the PDF; the text width is measured in
  the browser with the real font.
- Fonts do not burden the other pages or the startup bundle.

**Negative / costs**

- The fonts exist twice in the repository (backend and frontend) and must be kept
  in sync; the copy is not automated.
- The backend `fonts` folder contains no license texts; they exist only in the frontend.
  Whether delivering the backend meets the license terms (passing on the
  license texts) is not documented.
- Custom fonts outside this list are shown only approximately in the editor;
  `certificateCanvas.ts` has no mapping for them.

## Alternatives (not documented, obvious candidates)

- **Serve fonts only from the backend** and load them in the frontend via an endpoint:
  no duplicates, but one more request.
- **Use only standard fonts.**

## Related documents

- [`backend/src/main/resources/fonts`](../../backend/src/main/resources/fonts)
- [`certificate_schema.json`](../../backend/src/main/resources/schema/certificate_schema.json)
- [`certificateFonts.css`](../../frontend/src/features/certificate/assets/certificateFonts.css)
- [`assets/fonts/README.md`](../../frontend/src/features/certificate/assets/fonts/README.md)
- [`certificateLayout.ts`](../../frontend/src/features/certificate/model/certificateLayout.ts)
- [`itext-font-integration-guide-generic.md`](../../doc/itext-font-integration-guide-generic.md)
