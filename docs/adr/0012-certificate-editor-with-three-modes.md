# ADR 0012: Certificate editor with three modes (Graphic, Form, JSON)

> [Deutsch](0012-certificate-editor-with-three-modes.de.md) · **English**

- **Status:** accepted
- **Date:** 2026-11-24
- **Context:** Commits `23b46d1c` ("visual layout editor") and `200477f2` ("graphical layout view"); plan in `docs/plans/urkunden-layout-editor.md`

## Context

Until November 2026, the certificate layout was written by hand as JSON text in a textarea
(`NumberedTextarea` in `CertificateForm.vue`). Errors only showed up in the error PDF.
The plan calls for an editable, near-WYSIWYG layout without changing the
backend and the storage format ([ADR 0009](0009-certificate-layout-as-json-flowing-text-with-itext.md)).

## Decision

1. **The storage format stays the JSON string.** All modes read and write the same
   field `layoutDescription`; the backend needed no change for this except for a
   schema gap at `document.margins`.

2. **Three modes, switchable via `SelectButton`** (`CertificateForm.vue`):
   - **Graphic** (default): page view with background image and margin lines
     (`CertificateLayoutCanvas.vue`) together with the form. Clicking selects a block,
     vertical dragging changes the spacing above, horizontal dragging changes the tab stop;
     arrow keys, double-click to edit text. Later additions were free moving,
     adding and removing blocks, images from the media list, scaling,
     snapping to guides, zoom, multi-selection and margins.
   - **Form** (`CertificateLayoutEditor.vue`): document settings, paragraphs,
     tab stops, text and image blocks, placeholders at the cursor position, color,
     reordering via up/down buttons.
   - **JSON**: the previous textarea as an expert mode.

3. **The flowing-text model stays.** The graphical view works on the flowing-text model;
   "moving" changes spacing and tab stops, not coordinates. The plan rejects a
   free canvas with Konva or Fabric.

4. **Schema validation in all modes** ([ADR 0010](0010-certificate-json-schema-validation.md)),
   syntax errors in JSON mode.

5. **Preview through the real PDF.** The existing PDF preview (`PUT /event/{id}/certificate`,
   `vue-pdf-embed`) remains the authoritative display and is reloaded with an 800 ms delay
   (debounce).

6. **Undo and redo.** `useLayoutHistory` keeps a history over the JSON string;
   changes within 600 ms count as one step. Ctrl+Z and Ctrl+Y
   work in the graphical and form modes; text fields keep their own undo.

7. **Interaction of the views.** `useLayoutSync` keeps the selection synchronized between
   graphic and form; `certificateMove.ts` contains moving and snapping.

## Consequences

**Positive**

- Errors are visible while editing; layouts can be designed without knowing JSON.
- The format and the backend stayed unchanged; existing layouts remain usable.
- Round-trip tests load the backend fixtures (`test_layout_description_*.json`).

**Negative / costs**

- At almost 40 KB, `CertificateLayoutCanvas.vue` is the largest file of the feature.
- The graphical view cannot represent line and page breaks of long texts
  (see [ADR 0013](0013-certificate-geometry-from-pdf-measurements.md)).
- The editor reports layouts in the old flat format without `blocks` as invalid.
- Three modes and two input paths must be kept consistent.

## Alternatives (not documented, obvious candidates)

- **Free drag-and-drop canvas with absolute coordinates:** rejected in the plan because
  it would require rebuilding the backend.
- **Form only, without a graphical view** (stage 1 of the plan).
- **Only the backend PDF preview**, without its own display in the browser.

## Related documents

- [`urkunden-layout-editor.md`](../plans/urkunden-layout-editor.md)
- [`CertificateForm.vue`](../../frontend/src/features/certificate/widgets/CertificateForm.vue)
- [`CertificateLayoutCanvas.vue`](../../frontend/src/features/certificate/widgets/CertificateLayoutCanvas.vue)
- [`CertificateLayoutEditor.vue`](../../frontend/src/features/certificate/widgets/CertificateLayoutEditor.vue)
- [`useLayoutHistory.ts`](../../frontend/src/features/certificate/model/useLayoutHistory.ts)
- [`certificate-layout-editor.spec.ts`](../../frontend/e2e/certificate-layout-editor.spec.ts)
