# ADR 0012: Urkunden-Editor mit drei Modi (Grafisch, Formular, JSON)

> **Deutsch** · [English](0012-certificate-editor-with-three-modes.md)

- **Status:** akzeptiert
- **Datum:** 2026-11-24
- **Kontext:** Commit `23b46d1c` („visual layout editor“) und `200477f2` („graphical layout view“); Plan in `docs/plans/urkunden-layout-editor.md`

## Kontext

Bis November 2026 wurde das Layout der Urkunde als JSON-Text in einer Textarea
(`NumberedTextarea` in `CertificateForm.vue`) von Hand geschrieben. Fehler zeigten sich
erst im Fehler-PDF. Der Plan verlangt ein bearbeitbares, WYSIWYG-nahes Layout, ohne
Backend und Speicherformat zu ändern ([ADR 0009](0009-certificate-layout-as-json-flowing-text-with-itext.de.md)).

## Entscheidung

1. **Das Speicherformat bleibt der JSON-String.** Alle Modi lesen und schreiben dasselbe
   Feld `layoutDescription`; im Backend gab es dafür keine Änderung außer einer
   Schema-Lücke bei `document.margins`.

2. **Drei Modi, umschaltbar per `SelectButton`** (`CertificateForm.vue`):
   - **Grafisch** (Standard): Seitenansicht mit Hintergrundbild und Randlinien
     (`CertificateLayoutCanvas.vue`) zusammen mit dem Formular. Klick wählt einen Block,
     senkrechtes Ziehen ändert den Abstand oben, waagerechtes Ziehen den Tabulator;
     Pfeiltasten, Doppelklick zum Texteditieren. Später kamen freies Verschieben,
     Hinzufügen und Entfernen von Blöcken, Bilder aus der Medienliste, Skalieren,
     Einrasten an Hilfslinien, Zoom, Mehrfachauswahl und Ränder dazu.
   - **Formular** (`CertificateLayoutEditor.vue`): Dokument-Einstellungen, Absätze,
     Tabulatoren, Text- und Bild-Blöcke, Platzhalter an der Cursor-Position, Farbe,
     Umsortieren per Auf/Ab-Buttons.
   - **JSON**: die bisherige Textarea als Expertenmodus.

3. **Fließtextmodell bleibt.** Die grafische Ansicht arbeitet auf dem Fließtextmodell;
   „Bewegen“ ändert Abstände und Tabulatoren, nicht Koordinaten. Der Plan verwirft einen
   freien Canvas mit Konva oder Fabric.

4. **Schema-Prüfung in allen Modi** ([ADR 0010](0010-certificate-json-schema-validation.de.md)),
   Syntaxfehler im JSON-Modus.

5. **Vorschau über das echte PDF.** Die bestehende PDF-Vorschau (`PUT /event/{id}/certificate`,
   `vue-pdf-embed`) bleibt die maßgebliche Darstellung und wird mit 800 ms Verzögerung
   (Debounce) neu geladen.

6. **Rückgängig und Wiederholen.** `useLayoutHistory` führt einen Verlauf über den
   JSON-String; Änderungen innerhalb von 600 ms gelten als ein Schritt. Strg+Z und Strg+Y
   wirken im grafischen und im Formular-Modus; Textfelder behalten ihr eigenes Undo.

7. **Zusammenspiel der Ansichten.** `useLayoutSync` hält die Auswahl zwischen
   Grafik und Formular synchron; `certificateMove.ts` enthält Verschieben und Einrasten.

## Konsequenzen

**Positiv**

- Fehler sind beim Bearbeiten sichtbar; Layouts lassen sich ohne JSON-Kenntnis gestalten.
- Das Format und das Backend blieben unverändert; vorhandene Layouts bleiben nutzbar.
- Round-Trip-Tests laden die Backend-Fixtures (`test_layout_description_*.json`).

**Negativ / Kosten**

- Mit knapp 40 KB ist `CertificateLayoutCanvas.vue` die größte Datei des Features.
- Die grafische Ansicht kann Zeilen- und Seitenumbruch langer Texte nicht abbilden
  (siehe [ADR 0013](0013-certificate-geometry-from-pdf-measurements.de.md)).
- Layouts im alten flachen Format ohne `blocks` meldet der Editor als ungültig.
- Drei Modi und zwei Eingabewege müssen konsistent gehalten werden.

## Alternativen (nicht dokumentiert, naheliegend)

- **Freier Drag-and-Drop-Canvas mit absoluten Koordinaten:** im Plan abgelehnt, weil
  dafür das Backend umgebaut werden müsste.
- **Nur Formular ohne grafische Ansicht** (Stufe 1 des Plans).
- **Nur die Backend-PDF-Vorschau** ohne eigene Darstellung im Browser.

## Verwandte Dokumente

- [`urkunden-layout-editor.md`](../plans/urkunden-layout-editor.md)
- [`CertificateForm.vue`](../../frontend/src/features/certificate/widgets/CertificateForm.vue)
- [`CertificateLayoutCanvas.vue`](../../frontend/src/features/certificate/widgets/CertificateLayoutCanvas.vue)
- [`CertificateLayoutEditor.vue`](../../frontend/src/features/certificate/widgets/CertificateLayoutEditor.vue)
- [`useLayoutHistory.ts`](../../frontend/src/features/certificate/model/useLayoutHistory.ts)
- [`certificate-layout-editor.spec.ts`](../../frontend/e2e/certificate-layout-editor.spec.ts)
