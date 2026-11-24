# Plan: Visueller Editor für Urkunden-Layouts

## Context
`layoutDescription` ist ein JSON-String, den man heute in `CertificateForm.vue` (Zeilen 214–219, `NumberedTextarea`) von Hand editiert. Fehler zeigen sich erst im Fehler-PDF. Ziel: ein WYSIWYG-nahes Bearbeiten, ohne Backend und Speicherformat zu ändern.

## Machbarkeit
Ja, machbar. Wichtig: Das Modell ist **Fließtext** (Absätze, Tab-Stops, Margins, Blöcke), nicht absolut positioniert. Ein freier Drag-and-Drop-Canvas (Konva/Fabric) passt nicht zum Renderer (iText `Paragraph` + `Tab`). Er würde eine Backend-Umstellung auf x/y-Koordinaten erzwingen (gross, bricht bestehende Layouts).

## Empfohlener Ansatz: Strukturierter Formular-Editor + Live-PDF-Vorschau
Das PDF vom Backend ist die einzige echte WYSIWYG-Wahrheit; die bestehende Vorschau (`PUT /event/{id}/certificate`, `VuePdfEmbed`) bleibt daneben stehen und lädt automatisch (debounced) neu.

1. **Neue Komponente** `frontend/src/features/certificate/widgets/CertificateLayoutEditor.vue`, `v-model` = der JSON-String (parse/stringify). Ungültiges JSON → Fallback auf Textmodus.
2. **Umschalter** in `CertificateForm.vue`: Tab "Visuell" / "JSON" (`NumberedTextarea` bleibt als Expertenmodus, beide schreiben dasselbe `layoutDescription`).
3. **Editor-Inhalt** (nur OpenVue, keine neue Dep ausser ggf. `vuedraggable` für Umsortieren; alternativ Auf/Ab-Buttons):
   - Dokument: Margins, vier Fonts (Select aus Schema-Enum).
   - Absatzliste: hinzufügen/löschen/umsortieren; Margins und Tab-Stops (Position + Alignment) je Absatz.
   - Block je Absatz: Typ Text/Medium; Text mit Platzhalter-Chips (`{{GIVEN_NAME}}` … aus `TextBlockProcessor`), fontSize, bold/italic, Font, `ColorPicker`; Medium per Select aus Media-Feature + Breite.
4. **Live-Validierung**: Schema aus `GET /certificate_schema` clientseitig prüfen (z. B. `ajv`), Fehler am Feld anzeigen statt im Fehler-PDF.
5. **Vorschau-Debounce**: Deep-Watch (Zeile ~111) mit ~800 ms Debounce, damit nicht jeder Tastendruck ein PDF rendert.
6. **Backend**: keine Änderung nötig. Optional: Lücke in `certificate_schema.json` für `document.margins` (`type: object`, `properties`) schliessen.

## Optionale Stufe 2 (nur bei Bedarf)
Klick ins PDF-Bild springt zum passenden Block. Aufwand hoch (Mapping PDF-Position ↔ Block), erst nach Stufe 1 bewerten.

## Aufwand (grob)
Stufe 1: ca. 3–5 Tage inkl. Tests. Stufe 2 / absolute Positionierung: deutlich mehr, nicht empfohlen.

## Kritische Dateien
- `frontend/src/features/certificate/widgets/CertificateForm.vue`
- `frontend/src/features/certificate/widgets/NumberedTextarea.vue` (bleibt)
- `frontend/src/features/certificate/model/certificate.ts`
- `backend/src/main/resources/schema/certificate_schema.json`
- Referenz Renderer: `backend/.../application/certificate/CertificateServiceImpl.java`, `TextBlockProcessor.java`

## Verifikation
- Bestehende Fixtures (`backend/src/test/resources/certificate/test_layout_description_*.json`) laden → Round-Trip parse/stringify ohne Verlust.
- Manuell: Layout im Editor ändern, Vorschau-PDF prüfen, speichern, neu laden.
- Vitest für Parse/Serialize; bestehende Backend-Tests unverändert grün.

## Umsetzungsstand (Branch `feat/certificate-layout-editor`)
Umgesetzt (Stufe 1, Punkte 1, 2, 3 und 5):
- `model/certificateLayout.ts`: Typen, Parsen/Serialisieren, Hilfsfunktionen (Unit-Tests inkl. Round-Trip mit den Backend-Fixtures).
- `widgets/CertificateLayoutEditor.vue`: Dokument-Einstellungen, Absätze, Tabulatoren, Text- und Bild-Blöcke, Platzhalter-Knöpfe, Farbe. Umsortieren per Auf/Ab-Buttons, keine neue Abhängigkeit.
- `CertificateForm.vue`: Umschalter Visuell/JSON, Vorschau mit 800 ms Debounce.

Offen:
- Punkt 4: clientseitige Schema-Validierung (z. B. mit ajv, wäre eine neue Abhängigkeit). Fehler im JSON-Modus erscheinen weiter erst im Fehler-PDF.
- Punkt 6: Schema-Lücke bei `document.margins` (`type`/`properties` fehlen) schließen.
- Das Fixture `bbm-mittel-2024.json` nutzt das alte flache Format ohne `blocks` und besteht das aktuelle Schema nicht. Der Editor meldet es als ungültig.
- Einfügen von Platzhaltern am Textende statt an der Cursor-Position.
