# ADR 0010: JSON schema validation of the certificate layout in backend and frontend

> [Deutsch](0010-certificate-json-schema-validation.de.md) · **English**

- **Status:** accepted
- **Date:** 2024-05-25
- **Context:** Commit `2b7f48b4` ("add certificate json schema and classes …"); client-side validation in the editor since 2026-11-24

## Context

The certificate layout is a freely editable JSON text
([ADR 0009](0009-certificate-layout-as-json-flowing-text-with-itext.md)). Without validation, an
error in the layout only shows up at render time. The plan
`docs/plans/urkunden-layout-editor.md` names this as a problem: "Errors only show up
in the error PDF."

## Decision

1. **A JSON schema (Draft 7) describes the layout.**
   `backend/src/main/resources/schema/certificate_schema.json` defines `document`
   (fonts, margins) and `paragraphs` with `tabStops` and `blocks`, sets
   `additionalProperties: false` and names the required fields. Allowed fonts
   are listed as an enumeration in the schema (`definitions.font`).

2. **The backend validates before rendering.** `JsonSchemaValidator` uses the library
   `com.networknt:json-schema-validator`. If validation fails, the PDF
   `LayoutDescriptionErrors.pdf` with the error messages is produced
   (`CertificateServiceImpl`).

3. **The backend serves the schema to the frontend.** `GET /certificate_schema`
   (`ResultListController`).

4. **The frontend validates with the same schema.** `certificateLayoutValidation.ts`
   compiles the schema with `ajv` and returns readable error lists. Schema errors
   appear in all editor modes, a JSON syntax error in JSON mode
   ([ADR 0012](0012-certificate-editor-with-three-modes.md)). `ajv` became a direct
   dependency for this (previously only transitive in the lockfile).

5. **The schema remains the single source.** A gap in `document.margins` was closed in the
   schema itself (type and properties as numbers, matching the
   `Float` in the Java model), not in the frontend.

## Consequences

**Positive**

- Invalid layouts are flagged while editing, not only in the error PDF.
- Backend and frontend validate against the same file; new fonts are enabled in one
  place (see [ADR 0011](0011-certificate-fonts.md)).

**Negative / costs**

- The schema is maintained by hand; it must match the Java model
  (`ParagraphDefinition`, `TextBlock`, …) and the TypeScript types in
  `certificateLayout.ts`. An automatic consistency check is not documented.
- Two validation libraries (Java and JavaScript) that may interpret
  Draft 7 differently. The frontend validation runs with `strict: false`.
- `pnpm-workspace.yaml` contains an override `ajv@^6` for ESLint; Renovate ignores
  it via a rule ([ADR 0018](0018-renovate-and-auto-merge.md)). The frontend's direct
  dependency is `ajv` 8.

## Alternatives (not documented, obvious candidates)

- **Validate only in the backend:** errors would stay invisible until the preview.
- **Validation via Java classes and Jackson** instead of a schema: no reuse in the
  frontend.

## Related documents

- [`certificate_schema.json`](../../backend/src/main/resources/schema/certificate_schema.json)
- [`JsonSchemaValidator.java`](../../backend/src/main/java/de/jobst/resulter/application/certificate/JsonSchemaValidator.java)
- [`CertificateServiceImpl.java`](../../backend/src/main/java/de/jobst/resulter/application/certificate/CertificateServiceImpl.java)
- [`certificateLayoutValidation.ts`](../../frontend/src/features/certificate/model/certificateLayoutValidation.ts)
- [`urkunden-layout-editor.md`](../plans/urkunden-layout-editor.md)
