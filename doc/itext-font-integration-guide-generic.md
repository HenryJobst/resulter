# iText Font Integration Guide for Document PDFs

> [Deutsch](itext-font-integration-guide-generic.de.md) · **English**

## Goal

After completing these steps, you can add a new font family (for example `MyFont-Regular/Bold/Italic/BoldItalic.ttf`) to document PDF generation with iText in a reusable and project-agnostic way.

## Prerequisites

- iText 7/8/9 in your backend
- access to font files (`.ttf` or `.otf`)
- a central PDF rendering service
- optionally: JSON schema validation for document layout definitions

## Step 1: Add font files to resources

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

## Step 2: Extend your layout schema

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

## Step 3: Implement/reuse an iText font loader

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

## Step 4: Separate global and local font logic

- global document settings:
  `document.font`, `document.boldFont`, `document.italicFont`, `document.boldItalicFont`
- local text override:
  optional `block.font`

Order of application:

1. apply global style (bold/italic),
2. apply local font override if present,
3. fallback to simulated style (`simulateBold()`/`simulateItalic()`) if the matching variant is missing.

## Step 5: Example layout with a custom font

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

## Step 6: Validation and testing checklist

- schema validation accepts the new font names,
- PDF generation works with all style variants,
- Unicode characters render correctly,
- generated PDFs look identical on another machine,
- missing font files produce clear error messages.

Optional:

- golden-master PDF comparison,
- integration test via document export API endpoint.

## Step 7: Rollout checklist

- confirm font license allows redistribution,
- verify fonts are included in build artifacts,
- update docs for template authors/editors,
- monitor and log font-loading errors.

## Common failure modes

- `Font file not found`: layout filename does not exactly match the resource file.
- layout validation error: font missing in schema enum.
- poor bold/italic rendering: no dedicated bold/italic file, simulation fallback in use.
- broken special characters: encoding/embedding setup is incorrect.
