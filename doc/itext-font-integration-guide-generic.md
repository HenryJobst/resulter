# iText Font Integration Guide for Document PDFs

This document is bilingual:
- English section first
- German section below

---

## English

### Goal

After completing these steps, you can add a new font family (for example `MyFont-Regular/Bold/Italic/BoldItalic.ttf`) to document PDF generation with iText in a reusable and project-agnostic way.

### Prerequisites

- iText 7/8/9 in your backend
- access to font files (`.ttf` or `.otf`)
- a central PDF rendering service
- optionally: JSON schema validation for document layout definitions

### Step 1: Add font files to resources

```text
src/main/resources/fonts/
  MyFont-Regular.ttf
  MyFont-Bold.ttf
  MyFont-Italic.ttf
  MyFont-BoldItalic.ttf
```

Recommendations:

- keep all style variants in one family if available,
- use stable filenames because layout configs will reference them directly.

### Step 2: Extend your layout schema

If your layout JSON is validated, add the new fonts to allowed enums.

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

### Step 3: Implement/reuse an iText font loader

Handle two cases:

- iText standard fonts (`Helvetica`, `Times-Roman`, ...)
- custom fonts loaded from `resources/fonts`

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

Why `IDENTITY_H` and `FORCE_EMBEDDED`:

- better Unicode support,
- deterministic rendering across environments.

### Step 4: Separate global and local font logic

- global document settings:
  `document.font`, `document.boldFont`, `document.italicFont`, `document.boldItalicFont`
- local text override:
  optional `block.font`

Order of application:

1. apply global style (bold/italic),
2. apply local font override if present,
3. fallback to simulated style (`simulateBold()`/`simulateItalic()`) if the matching variant is missing.

### Step 5: Example layout with a custom font

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
        { "block": { "text": "Signature", "font": "MyFont-Regular.ttf", "italic": true, "fontSize": 18 } }
      ]
    }
  ]
}
```

### Step 6: Validation and testing checklist

- schema validation accepts the new font names,
- PDF generation works with all style variants,
- Unicode characters render correctly,
- generated PDFs look identical on another machine,
- missing font files produce clear error messages.

Optional:

- golden-master PDF comparison,
- integration test via document export API endpoint.

### Step 7: Rollout checklist

- confirm font license allows redistribution,
- verify fonts are included in build artifacts,
- update docs for template authors/editors,
- monitor and log font-loading errors.

### Common failure modes

- `Font file not found`: layout filename does not exactly match the resource file.
- layout validation error: font missing in schema enum.
- poor bold/italic rendering: no dedicated bold/italic file, simulation fallback in use.
- broken special characters: encoding/embedding setup is incorrect.

---

## Deutsch

### Ziel

Nach diesen Schritten kannst du eine neue Schriftfamilie (z. B. `MyFont-Regular/Bold/Italic/BoldItalic.ttf`) fuer den Dokument-PDF-Export mit iText sauber und uebertragbar integrieren.

### Voraussetzungen

- iText 7/8/9 im Backend
- Zugriff auf Font-Dateien (`.ttf` oder `.otf`)
- zentraler PDF-Rendering-Service
- optional: JSON-Schema-Validierung fuer Layoutdefinitionen

### Schritt 1: Font-Dateien in Resources ablegen

```text
src/main/resources/fonts/
  MyFont-Regular.ttf
  MyFont-Bold.ttf
  MyFont-Italic.ttf
  MyFont-BoldItalic.ttf
```

Empfehlungen:

- moeglichst komplette Familie (Regular/Bold/Italic/BoldItalic) nutzen,
- Dateinamen stabil halten, da sie direkt in Layout-JSONs referenziert werden.

### Schritt 2: Layout-Schema erweitern

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

### Schritt 3: iText Font-Loader implementieren/wiederverwenden

Der Loader sollte zwei Faelle behandeln:

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

- bessere Unicode-Unterstuetzung,
- reproduzierbares Rendering auf unterschiedlichen Systemen.

### Schritt 4: Globale und lokale Font-Logik trennen

- globale Einstellungen:
  `document.font`, `document.boldFont`, `document.italicFont`, `document.boldItalicFont`
- lokaler Override je Block:
  optional `block.font`

Empfohlene Reihenfolge:

1. globalen Stil (bold/italic) anwenden,
2. lokalen Font-Override anwenden,
3. bei fehlender Variante auf Simulation (`simulateBold()`/`simulateItalic()`) zurueckfallen.

### Schritt 5: Layout-Beispiel mit neuer Schrift

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

### Schritt 6: Test- und Abnahme-Checkliste

- Schema-Validierung akzeptiert neue Fontnamen,
- PDF-Generierung funktioniert fuer alle Varianten,
- Sonderzeichen/Unicode werden korrekt gerendert,
- PDF sieht auf einem zweiten System identisch aus,
- fehlende Font-Dateien erzeugen nachvollziehbare Fehlermeldungen.

Optional:

- Golden-Master-PDF-Vergleich,
- Integrationstest ueber den Dokument-Export-Endpunkt.

### Schritt 7: Rollout-Checkliste

- Font-Lizenz fuer Redistribution geprueft,
- Fonts sind im Build-Artefakt enthalten,
- Doku fuer Template-Autoren aktualisiert,
- Logging/Monitoring fuer Font-Fehler aktiv.

### Typische Fehlerbilder

- `Font file not found`: Dateiname im Layout passt nicht exakt zur Datei.
- Schema-Fehler: Font nicht in `enum` freigeschaltet.
- unschoenes Bold/Italic: echte Varianten fehlen, Simulation aktiv.
- kaputte Sonderzeichen: Encoding/Embedding nicht korrekt gesetzt.
