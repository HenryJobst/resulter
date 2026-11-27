# 10 Referenz zum Urkunden-Editor

> **Deutsch** · [English](10-certificate-reference.md)

Nachschlagewerk für Platzhalter, Schriften, Maße, Tastenkürzel und Grenzen.

## Platzhalter

Platzhalter stehen in doppelten geschweiften Klammern in einem Text. Sie werden beim Erzeugen der
Urkunde ersetzt. Jeder Platzhalter darf mehrfach und in jedem Textblock vorkommen.

| Platzhalter | Wird ersetzt durch | Beispiel in der Seitenansicht |
|---|---|---|
| `{{GIVEN_NAME}}` | Vorname des Teilnehmers | Max |
| `{{FAMILY_NAME}}` | Nachname | Mustermann |
| `{{EVENT_NAME}}` | Name des Wettkampfs | Beispiel-OL |
| `{{RESULT_POSITION}}` | Platz | 1 |
| `{{CATEGORY}}` | Kurzname der Kategorie | H35- |
| `{{RESULT_TIME}}` | Laufzeit; `mm:ss`, ab einer Stunde `HH:mm:ss` | 32:14 |
| `{{ORGANISATION}}` | Name des Vereins | Kaulsdorfer OLV Berlin |

Hat der Teilnehmer keinen Verein oder keine Zeit, bleibt der Platzhalter unverändert im Text
stehen. Die Quelle ist `TextBlockProcessor` im Backend.

## Schriften

Die Schriftnamen im Layout sind die der PDF-Bibliothek.

| Familie | Normal | Fett | Kursiv | Fett kursiv |
|---|---|---|---|---|
| Helvetica | `Helvetica` | `Helvetica-Bold` | `Helvetica-Oblique` | `Helvetica-BoldOblique` |
| Times | `Times-Roman` | `Times-Bold` | `Times-Italic` | `Times-BoldItalic` |
| Courier | `Courier` | `Courier-Bold` | `Courier-Oblique` | `Courier-BoldOblique` |
| Amaranth | `Amaranth-Regular.ttf` | `Amaranth-Bold.ttf` | `Amaranth-Italic.ttf` | `Amaranth-BoldItalic.ttf` |
| Montserrat | `Montserrat-Regular.ttf` | `Montserrat-Bold.ttf` | `Montserrat-Italic.ttf` | `Montserrat-BoldItalic.ttf` |
| Satisfy | `Satisfy-Regular.ttf` | keine | keine | keine |
| Tangerine | `Tangerine-Regular.ttf` | keine | keine | keine |
| Symbol | `Symbol` | keine | keine | keine |
| Zapf Dingbats | `ZapfDingbats` | keine | keine | keine |

- Ohne Angabe verwendet das PDF Helvetica.
- Die Wahl einer Schrift für einen Block geht vor den Dokumentschriften. Hat der Block keine,
  gilt für Fett, Kursiv oder beides die passende Dokumentschrift, sonst die Dokumentschrift
  **Schrift**.
- Gibt es für einen Stil keine Schrift, **täuscht** der PDF-Druck Fett (Umrandung) und Kursiv
  (Schrägstellung um etwa 12°) vor. Die Breite der Buchstaben ändert sich dabei nicht.
- Weitere Schriften lassen sich einbinden, siehe
  [`doc/itext-font-integration-guide-generic.md`](../../doc/itext-font-integration-guide-generic.de.md).
  Die Lizenzen der mitgelieferten Schriften (Amaranth, Montserrat und Tangerine unter der SIL Open
  Font License 1.1, Satisfy unter Apache 2.0) stehen in
  [`frontend/src/features/certificate/assets/fonts`](../../frontend/src/features/certificate/assets/fonts/README.de.md).

## Maße und Standardwerte

| Größe | Wert |
|---|---|
| Seite | A4, 595 × 842 pt |
| Seitenränder ohne `document` | 36 pt rundum |
| Seitenränder mit `document`, ohne Angabe | oben 30, unten 30, links 20, rechts 20 pt |
| Abstand oben und unten eines Absatzes | 4 pt (ein eigener Wert ersetzt ihn) |
| Standard-Tabulator ohne eigene Angabe | 261,5 pt, zentriert |
| Raster hinter dem letzten Tabulator | Vielfache von 50 pt |
| Schriftgröße ohne Angabe | 12 pt |
| Bildbreite ohne Angabe | 100 pt |
| Zeilenhöhe | Faktor mal Schriftgröße, je nach Schrift (siehe unten) |

Die **Zeilenhöhe** hängt von der Schrift ab. Sie wurde an PDFs gemessen, die das Backend erzeugt
hat:

| Schrift | Zeilenhöhe | Grundlinie |
|---|---|---|
| Helvetica (Standard) | 1,499 | 1,056 |
| Times | 1,458 | 1,009 |
| Courier | 1,273 | 0,92 |
| Amaranth | 1,964 | 1,426 |
| Montserrat | 2,108 | 1,382 |
| Satisfy | 1,921 | 1,183 |
| Tangerine | 1,45 (geschätzt) | 0,949 |

Beide Werte sind Faktoren der Schriftgröße. Die Grundlinie ist der Abstand von der Oberkante
der Zeile zur Schrift-Grundlinie. Ein Bild steht mit seinem unteren Rand auf dieser Linie.

## Tastenkürzel im grafischen Editor

| Taste | Wirkung |
|---|---|
| Klick | Block auswählen |
| Umschalt+Klick | Block zur Auswahl hinzufügen oder entfernen |
| Rahmen auf leerer Seite aufziehen | Berührte Blöcke auswählen (mit Umschalt: hinzufügen) |
| Ziehen | Block verschieben (senkrecht oder waagerecht, je nach Richtung) |
| Alt beim Ziehen | Alles darunter rückt mit |
| Doppelklick auf Text | Text bearbeiten |
| Doppelklick auf Bild | Anderes Bild wählen |
| Eingabe | Ausgewählten Text bearbeiten, beim Bearbeiten übernehmen |
| Esc | Bearbeitung abbrechen |
| Pfeiltasten | Block um 1 pt verschieben |
| Umschalt+Pfeiltasten | Block um 10 pt verschieben |
| Alt+Pfeiltasten | Wie Pfeiltasten, aber alles darunter rückt mit |
| Entf, Rückschritt | Ausgewählte Blöcke löschen |
| Strg+Z (Cmd+Z) | Rückgängig |
| Strg+Y (Cmd+Y), Umschalt+Strg+Z | Wiederholen |

## Grenzen der Seitenansicht

Die Seitenansicht im grafischen Editor rechnet das Layout so nach, wie das Backend es setzt. Die
Maße stammen aus Messungen an echten PDFs von 13 Urkunden. Es bleiben Abweichungen:

- **Senkrecht:** bis etwa 1 pt.
- **Linke Kante** eines Blocks: bis etwa 3 pt.
- **Breite** eines Textes: bis etwa 5 pt. Bei fetten Amaranth- und Montserrat-Zeilen liegt die
  Abweichung bei rund 2 %, die Ursache ist nicht geklärt.
- **Tangerine:** Die Zeilenhöhe ist nur geschätzt, weil sie in den gemessenen Urkunden nur in einer
  letzten Zeile vorkam.
- **Zeilenumbruch** langer Texte und **Seitenumbruch** werden nicht dargestellt.
- Die Bilder erscheinen mit ihrem echten Seitenverhältnis. Die Höhe eines Bildes im PDF ergibt sich
  aus Breite mal Verhältnis.
- Die Seitenansicht wurde bisher im Browser Chromium geprüft. Firefox und Safari sind nicht getestet.

Maßgeblich bleibt die [PDF-Vorschau](06-certificates.de.md#die-vorschau). Die Entscheidung dahinter steht in
[ADR 0013](../adr/0013-certificate-geometry-from-pdf-measurements.de.md).
