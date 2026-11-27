# ADR 0013: Geometrie der grafischen Urkundenansicht aus PDF-Messungen

> **Deutsch** · [English](0013-certificate-geometry-from-pdf-measurements.md)

- **Status:** akzeptiert
- **Datum:** 2026-11-24
- **Kontext:** Commit `200477f2` („graphical layout view and margin fix“), Nachbesserung in `56f60966` (2026-11-25, „graphical view matches the PDF“)

## Kontext

Die grafische Ansicht ([ADR 0012](0012-certificate-editor-with-three-modes.de.md)) soll zeigen,
wo iText die Blöcke setzt. Die Position ergibt sich aus Absatzabständen,
Zeilenhöhe, Tabulatoren und Schrift ([ADR 0009](0009-certificate-layout-as-json-flowing-text-with-itext.de.md));
das Frontend kann iText nicht ausführen und hat keinen Zugriff auf die Backend-Logik.

## Entscheidung

1. **Das Frontend bildet die Satzregeln nach.** `model/certificateCanvas.ts` rechnet
   Layout-JSON in Rahmen (`CanvasBox`) mit Position in Punkt von der oberen linken
   Seitenecke um. Es berechnet sie rein aus dem Layout, ohne Backend-Aufruf.

2. **Die Konstanten wurden an echten PDFs gemessen.** Gemessen wurden PDFs, die das
   Backend rendert (Textpositionen ausgelesen mit pdf.js):
   - Absatzabstand oben und unten 4 pt; ein angegebener Abstand ersetzt ihn
     (er wird nicht addiert).
   - Zeilenhöhe und Grundlinie hängen von der Schrift ab und sind pro Schrift gefittet
     (Faktor mal Schriftgröße), z. B. Helvetica 1,499 und 1,056, Amaranth 1,964 und 1,426.
     Die Werte wurden an den PDFs von 13 echten Urkunden angepasst (Abweichung
     laut Kommentar unter 0,1 pt, laut Commit-Nachricht höchstens 1 pt). Tangerine ist
     eine Schätzung.
   - Tabulatoren werden aufsteigend benutzt; ein Block steht auf dem nächsten Stopp hinter
     dem Ende des vorigen, danach auf dem nächsten Vielfachen von 50. Ohne Tabulatoren gilt
     der iText-Standard: ein zentrierter Stopp bei 261,5.
   - Standardschriftgröße 12; Bildhöhe grob Breite mal 0,5.

3. **Die Messwerte sind durch Tests festgehalten.**
   `test/features/certificate/certificateCanvas.spec.ts` pinnt sie; ändert sich das
   Backend-Rendering, schlägt der Test an oder die Werte müssen neu gemessen werden.

4. **Textbreite im Browser gemessen.** Die ursprüngliche Schätzung der Textbreite wurde
   am 2026-11-25 durch Messung im Browser mit der echten Schrift ersetzt
   ([ADR 0011](0011-certificate-fonts.de.md)). Echte Schriftmetriken fehlen weiter für
   nicht eingebundene Schriften.

5. **Beispieldaten wie in der Backend-Vorschau.** Platzhalter werden durch dieselben
   Dummy-Werte ersetzt (`Max Mustermann`, `Beispiel-OL`, …).

6. **Die PDF-Vorschau bleibt die Wahrheit.** Das Dokument sagt ausdrücklich: Die Ansicht
   ist eine Näherung, die Vorschau im PDF entscheidet.

## Konsequenzen

**Positiv**

- Die grafische Bearbeitung ist ohne Roundtrip zum Backend möglich und trifft laut
  Messung Absätze und Tabulatoren auf etwa 1 pt genau.
- Die Annahmen sind als Konstanten und Tests nachvollziehbar.

**Negativ / Kosten**

- Die Werte sind eine Kopie des iText-Verhaltens; eine Änderung der iText-Version,
  der Schriften oder des Renderers kann sie ungenau machen (neue Messung nötig).
- Zeilen- und Seitenumbruch langer Texte sind nicht abgebildet; die Höhe von Bildern ist
  geschätzt, weil das echte Seitenverhältnis fehlt.
- Eigene `.ttf`-Schriften werden mit ähnlicher Web-Schrift dargestellt.
- Das Hintergrundbild ist die Miniatur des Backends und daher unscharf.

## Alternativen (nicht dokumentiert, naheliegend)

- **Das Backend liefert Blockpositionen** (genaue Breiten und Umbrüche) und das Frontend
  legt Rahmen über die PDF-Vorschau. Der Plan nennt das als mögliche nächste Stufe.
- **Nur die PDF-Vorschau ohne eigene Geometrie.**

## Verwandte Dokumente

- [`certificateCanvas.ts`](../../frontend/src/features/certificate/model/certificateCanvas.ts)
- [`certificateCanvas.spec.ts`](../../frontend/test/features/certificate/certificateCanvas.spec.ts)
- [`urkunden-layout-editor.md`](../plans/urkunden-layout-editor.md)
- [`CertificateServiceImpl.java`](../../backend/src/main/java/de/jobst/resulter/application/certificate/CertificateServiceImpl.java)
