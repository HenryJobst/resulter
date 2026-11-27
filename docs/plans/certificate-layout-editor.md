# Plan: Visual Editor for Certificate Layouts

> [Deutsch](certificate-layout-editor.de.md) · **English**

## Context
`layoutDescription` is a JSON string that is currently edited by hand in `CertificateForm.vue` (lines 214–219, `NumberedTextarea`). Errors only show up in the error PDF. Goal: near-WYSIWYG editing without changing the backend or the storage format.

## Feasibility
Yes, feasible. Important: the model is **flowing text** (paragraphs, tab stops, margins, blocks), not absolutely positioned. A free drag-and-drop canvas (Konva/Fabric) does not fit the renderer (iText `Paragraph` + `Tab`). It would force the backend to switch to x/y coordinates (large effort, breaks existing layouts).

## Recommended approach: structured form editor + live PDF preview
The PDF from the backend is the only true WYSIWYG reference; the existing preview (`PUT /event/{id}/certificate`, `VuePdfEmbed`) stays alongside and reloads automatically (debounced).

1. **New component** `frontend/src/features/certificate/widgets/CertificateLayoutEditor.vue`, `v-model` = the JSON string (parse/stringify). Invalid JSON → fall back to text mode.
2. **Switch** in `CertificateForm.vue`: tab "Visual" / "JSON" (`NumberedTextarea` stays as the expert mode, both write the same `layoutDescription`).
3. **Editor content** (OpenVue only, no new dependency except possibly `vuedraggable` for reordering; alternatively up/down buttons):
   - Document: margins, four fonts (select from the schema enum).
   - Paragraph list: add/delete/reorder; margins and tab stops (position + alignment) per paragraph.
   - Block per paragraph: type text/medium; text with placeholder chips (`{{GIVEN_NAME}}` … from `TextBlockProcessor`), fontSize, bold/italic, font, `ColorPicker`; medium via select from the media feature + width.
4. **Live validation**: check the schema from `GET /certificate_schema` on the client (e.g. `ajv`), show errors at the field instead of in the error PDF.
5. **Preview debounce**: deep watch (line ~111) with ~800 ms debounce, so that not every keystroke renders a PDF.
6. **Backend**: no change needed. Optional: close the gap in `certificate_schema.json` for `document.margins` (`type: object`, `properties`).

## Stage 2: graphical view (branch `feat/certificate-layout-canvas`)
Implemented on the same flowing-text model, without backend changes:
- `model/certificateCanvas.ts`: page geometry. The constants were measured on PDFs rendered by the backend (reading the text positions with pdf.js): 4 pt default spacing at top and bottom (a specified spacing replaces it), line height 1.5 x font size, baseline 1.055 x font size below the top edge of the line, tab stops ascending, a block sits on the next stop after the end of the previous one, then on the next multiple of 50. The tests in `test/features/certificate/certificateCanvas.spec.ts` pin these measured values.
- `widgets/CertificateLayoutCanvas.vue`: A4 page with background image and margin lines, sample data as in the backend preview, a click selects a block, vertical dragging changes the spacing at the top, horizontal dragging the tab stop, arrow keys, double-click edits the text.
- Modes in the form: Graphical (graphic and form together), Form, JSON.
- Measured comparison with the PDF rendering: the position of paragraphs and tab stops matches to about 1 pt.

Known limitations:
- The text width is estimated (no real font metrics). This only affects centered or right-aligned blocks and the position of following blocks, not the line height.
- Line wrapping of long texts and page breaks are not shown.
- The height of image blocks is estimated (width times 0.5), the real aspect ratio is unknown.
- Custom fonts (`.ttf`) are displayed with a similar web font.
- The background image is the backend's preview thumbnail, hence blurry.

Possible next stage: have the backend deliver block positions (exact widths and line breaks) and overlay the frames on the PDF preview.

## Effort (rough)
Stage 1: about 3–5 days including tests. Stage 2 / absolute positioning: considerably more, not recommended.

## Critical files
- `frontend/src/features/certificate/widgets/CertificateForm.vue`
- `frontend/src/features/certificate/widgets/NumberedTextarea.vue` (stays)
- `frontend/src/features/certificate/model/certificate.ts`
- `backend/src/main/resources/schema/certificate_schema.json`
- Renderer reference: `backend/.../application/certificate/CertificateServiceImpl.java`, `TextBlockProcessor.java`

## Verification
- Load existing fixtures (`backend/src/test/resources/certificate/test_layout_description_*.json`) → round trip parse/stringify without loss.
- Manual: change a layout in the editor, check the preview PDF, save, reload.
- Vitest for parse/serialize; existing backend tests stay green unchanged.

## Implementation status (branch `feat/certificate-layout-editor`)
Implemented (stage 1, all six points):
- `model/certificateLayout.ts`: types, parsing/serializing, helper functions (unit tests including round trip with the backend fixtures).
- `model/certificateLayoutValidation.ts`: validation against the backend schema with `ajv` (already present transitively in the lockfile, now a direct dependency). Schema errors appear in both modes, a JSON syntax error in JSON mode.
- `widgets/CertificateLayoutEditor.vue`: document settings, paragraphs, tab stops, text and image blocks, placeholders at the cursor position, color. Reordering via up/down buttons.
- `CertificateForm.vue`: Visual/JSON switch, preview with 800 ms debounce.
- `certificate_schema.json`: `document.margins` now has `type`/`properties` (numbers, matching the `Float` in the Java model).
- E2E test `e2e/certificate-layout-editor.spec.ts` (editor, PDF preview, schema error, saving, reopening).

Known limitations:
- The fixture `bbm-mittel-2024.json` uses the old flat format without `blocks` and does not pass the schema; the editor reports it as invalid.
- The layout remains flowing text. A graphical editor with direct manipulation is open as the next stage (see stage 2).
