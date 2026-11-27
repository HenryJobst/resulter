# Leitfaden: Schriften in Dokument-PDFs mit iText einbinden

> **Deutsch** · [English](itext-font-integration-guide-generic.md)

## Ziel

Nach diesen Schritten kannst du eine neue Schriftfamilie (z. B. `MyFont-Regular/Bold/Italic/BoldItalic.ttf`) für den Dokument-PDF-Export mit iText sauber und übertragbar integrieren.

## Voraussetzungen

- iText 7/8/9 im Backend
- Zugriff auf Font-Dateien (`.ttf` oder `.otf`)
- zentraler PDF-Rendering-Service
- optional: JSON-Schema-Validierung für Layoutdefinitionen

## Schritt 1: Font-Dateien in Resources ablegen

```text
src/main/resources/fonts/
  MyFont-Regular.ttf
  MyFont-Bold.ttf
  MyFont-Italic.ttf
  MyFont-BoldItalic.ttf
```

Empfehlungen:

- möglichst komplette Familie (Regular/Bold/Italic/BoldItalic) nutzen,
- Dateinamen stabil halten, da sie direkt in Layout-JSONs referenziert werden.

## Schritt 2: Layout-Schema erweitern

Wenn Layout-JSON validiert wird, neue Fontnamen in die erlaubten `enum`-Werte aufnehmen.

```json
{
  "definitions": {
    "font": {
      "type": "string",
      "enum": ["Helvetica", "Times-Roman", "MyFont-Regular.ttf"]
    }
  },
  "properties": {
    "document": {
      "properties": {
        "boldFont": { "type": "string", "enum": ["Helvetica-Bold", "MyFont-Bold.ttf"] },
        "italicFont": { "type": "string", "enum": ["Helvetica-Oblique", "MyFont-Italic.ttf"] },
        "boldItalicFont": { "type": "string", "enum": ["Helvetica-BoldOblique", "MyFont-BoldItalic.ttf"] }
      }
    }
  }
}
```

## Schritt 3: iText Font-Loader implementieren/wiederverwenden

Der Loader sollte zwei Fälle behandeln:

- iText-Standardfonts (`Helvetica`, `Times-Roman`, ...)
- projektspezifische Fonts aus `resources/fonts`

```java
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

import java.io.IOException;
import java.io.InputStream;

public static FontProgram loadFont(String fontNameOrPath) {
    InputStream fontStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("fonts/" + fontNameOrPath);

    if (fontStream == null) {
        throw new RuntimeException("Font file not found: " + fontNameOrPath);
    }

    try (fontStream) {
        return FontProgramFactory.createFont(fontStream.readAllBytes(), true);
    } catch (IOException e) {
        throw new RuntimeException("Error loading font: " + fontNameOrPath, e);
    }
}

public static PdfFont getPdfFont(String fontNameOrPath) {
    if (fontNameOrPath == null) {
        return null;
    }
    try {
        if (StandardFonts.isStandardFont(fontNameOrPath)) {
            return PdfFontFactory.createFont(fontNameOrPath);
        }
        FontProgram program = loadFont(fontNameOrPath);
        return PdfFontFactory.createFont(
                program,
                PdfEncodings.IDENTITY_H,
                PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
        );
    } catch (IOException e) {
        throw new RuntimeException("Error creating PDF font: " + fontNameOrPath, e);
    }
}
```

Warum `IDENTITY_H` und `FORCE_EMBEDDED`:

- bessere Unicode-Unterstützung,
- reproduzierbares Rendering auf unterschiedlichen Systemen.

## Schritt 4: Globale und lokale Font-Logik trennen

- globale Einstellungen:
  `document.font`, `document.boldFont`, `document.italicFont`, `document.boldItalicFont`
- lokaler Override je Block:
  optional `block.font`

Empfohlene Reihenfolge:

1. globalen Stil (bold/italic) anwenden,
2. lokalen Font-Override anwenden,
3. bei fehlender Variante auf Simulation (`simulateBold()`/`simulateItalic()`) zurückfallen.

## Schritt 5: Layout-Beispiel mit neuer Schrift

```json
{
  "document": {
    "font": "MyFont-Regular.ttf",
    "boldFont": "MyFont-Bold.ttf",
    "italicFont": "MyFont-Italic.ttf",
    "boldItalicFont": "MyFont-BoldItalic.ttf"
  },
  "paragraphs": [
    {
      "blocks": [
        { "block": { "text": "{{GIVEN_NAME}} {{FAMILY_NAME}}", "bold": true, "fontSize": 28 } }
      ]
    },
    {
      "blocks": [
        { "block": { "text": "Signatur", "font": "MyFont-Regular.ttf", "italic": true, "fontSize": 18 } }
      ]
    }
  ]
}
```

## Schritt 6: Test- und Abnahme-Checkliste

- Schema-Validierung akzeptiert neue Fontnamen,
- PDF-Generierung funktioniert für alle Varianten,
- Sonderzeichen/Unicode werden korrekt gerendert,
- PDF sieht auf einem zweiten System identisch aus,
- fehlende Font-Dateien erzeugen nachvollziehbare Fehlermeldungen.

Optional:

- Golden-Master-PDF-Vergleich,
- Integrationstest über den Dokument-Export-Endpunkt.

## Schritt 7: Rollout-Checkliste

- Font-Lizenz für Redistribution geprüft,
- Fonts sind im Build-Artefakt enthalten,
- Doku für Template-Autoren aktualisiert,
- Logging/Monitoring für Font-Fehler aktiv.

## Typische Fehlerbilder

- `Font file not found`: Dateiname im Layout passt nicht exakt zur Datei.
- Schema-Fehler: Font nicht in `enum` freigeschaltet.
- unschönes Bold/Italic: echte Varianten fehlen, Simulation aktiv.
- kaputte Sonderzeichen: Encoding/Embedding nicht korrekt gesetzt.
