# ADR 0010: JSON-Schema-Validierung des Urkunden-Layouts in Backend und Frontend

> **Deutsch** · [English](0010-certificate-json-schema-validation.md)

- **Status:** akzeptiert
- **Datum:** 2024-05-25
- **Kontext:** Commit `2b7f48b4` („add certificate json schema and classes …“); clientseitige Prüfung im Editor ab 2026-11-24

## Kontext

Das Urkunden-Layout ist ein frei bearbeitbarer JSON-Text
([ADR 0009](0009-certificate-layout-as-json-flowing-text-with-itext.de.md)). Ohne Prüfung zeigt
sich ein Fehler im Layout erst beim Rendern. Der Plan
`docs/plans/urkunden-layout-editor.md` nennt das als Problem: „Fehler zeigen sich erst
im Fehler-PDF.“

## Entscheidung

1. **Ein JSON-Schema (Draft 7) beschreibt das Layout.**
   `backend/src/main/resources/schema/certificate_schema.json` definiert `document`
   (Schriften, Ränder) und `paragraphs` mit `tabStops` und `blocks`, setzt
   `additionalProperties: false` und benennt die Pflichtfelder. Erlaubte Schriften
   stehen als Aufzählung im Schema (`definitions.font`).

2. **Das Backend prüft vor dem Rendern.** `JsonSchemaValidator` nutzt die Bibliothek
   `com.networknt:json-schema-validator`. Schlägt die Prüfung fehl, entsteht das PDF
   `LayoutDescriptionErrors.pdf` mit den Fehlertexten
   (`CertificateServiceImpl`).

3. **Das Backend liefert das Schema an das Frontend.** `GET /certificate_schema`
   (`ResultListController`).

4. **Das Frontend prüft mit demselben Schema.** `certificateLayoutValidation.ts`
   kompiliert das Schema mit `ajv` und liefert lesbare Fehlerlisten. Schema-Fehler
   erscheinen in allen Editor-Modi, ein JSON-Syntaxfehler im JSON-Modus
   ([ADR 0012](0012-certificate-editor-with-three-modes.de.md)). `ajv` wurde dafür zur direkten
   Abhängigkeit (vorher nur transitiv im Lockfile).

5. **Das Schema bleibt die einzige Quelle.** Eine Lücke bei `document.margins` wurde im
   Schema selbst geschlossen (Typ und Eigenschaften als Zahlen, passend zum
   `Float` im Java-Modell), nicht im Frontend.

## Konsequenzen

**Positiv**

- Ungültige Layouts werden beim Bearbeiten markiert, nicht erst im Fehler-PDF.
- Backend und Frontend prüfen gegen dieselbe Datei; neue Schriften werden an einer
  Stelle freigegeben (siehe [ADR 0011](0011-certificate-fonts.de.md)).

**Negativ / Kosten**

- Das Schema ist von Hand gepflegt; es muss zum Java-Modell
  (`ParagraphDefinition`, `TextBlock`, …) und zu den TypeScript-Typen in
  `certificateLayout.ts` passen. Ein automatischer Abgleich ist nicht belegt.
- Zwei Validierungsbibliotheken (Java und JavaScript) mit möglicherweise
  abweichender Auslegung von Draft 7. Die Frontend-Prüfung läuft mit `strict: false`.
- In `pnpm-workspace.yaml` besteht ein Override `ajv@^6` für ESLint; Renovate ignoriert
  ihn per Regel ([ADR 0018](0018-renovate-and-auto-merge.de.md)). Die direkte
  Abhängigkeit des Frontends ist `ajv` 8.

## Alternativen (nicht dokumentiert, naheliegend)

- **Nur im Backend prüfen:** Fehler blieben bis zur Vorschau unsichtbar.
- **Validierung per Java-Klassen und Jackson** statt Schema: ohne Wiederverwendung im
  Frontend.

## Verwandte Dokumente

- [`certificate_schema.json`](../../backend/src/main/resources/schema/certificate_schema.json)
- [`JsonSchemaValidator.java`](../../backend/src/main/java/de/jobst/resulter/application/certificate/JsonSchemaValidator.java)
- [`CertificateServiceImpl.java`](../../backend/src/main/java/de/jobst/resulter/application/certificate/CertificateServiceImpl.java)
- [`certificateLayoutValidation.ts`](../../frontend/src/features/certificate/model/certificateLayoutValidation.ts)
- [`urkunden-layout-editor.md`](../plans/urkunden-layout-editor.md)
