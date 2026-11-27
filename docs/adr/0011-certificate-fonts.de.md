# ADR 0011: Eingebundene Urkunden-Schriften, Lizenzen und Laden im Editor

> **Deutsch** · [English](0011-certificate-fonts.md)

- **Status:** akzeptiert
- **Datum:** 2024-04-04
- **Kontext:** Commits `ced63824` und `e9b93051` (Schriften im Backend, 2024-04-04), Montserrat am 2025-10-06, Schriften im Frontend am 2026-11-25 (`56f60966`)

## Kontext

Urkunden sollen mehr als die PDF-Standardschriften nutzen können. Die Schrift wird im
Layout-JSON angegeben ([ADR 0009](0009-certificate-layout-as-json-flowing-text-with-itext.de.md)).
Seit der grafischen Ansicht ([ADR 0012](0012-certificate-editor-with-three-modes.de.md)) braucht
auch das Frontend dieselben Schriften, damit die Vorschau die echten Zeichen zeigt.

## Entscheidung

1. **Schriften liegen als TrueType-Dateien im Backend.**
   `backend/src/main/resources/fonts` enthält Amaranth (Regular, Bold, Italic,
   BoldItalic), Montserrat (vier Schnitte), Satisfy und Tangerine (je Regular).
   `CertificateServiceImpl.loadFont` lädt sie über den Classpath (`fonts/<Dateiname>`);
   die PDF-Standardschriften (Courier, Helvetica, Times-Roman, Symbol, ZapfDingbats)
   sind ohne Datei verfügbar.

2. **Freigabe über das JSON-Schema.** Nur Schriftnamen aus `definitions.font` im
   `certificate_schema.json` sind gültig ([ADR 0010](0010-certificate-json-schema-validation.de.md)).
   Eine neue Schrift braucht die Datei im Backend, einen Schema-Eintrag und
   Einträge in `FONTS`, `BOLD_FONTS` und `ITALIC_FONTS` im Frontend
   (`certificateLayout.ts`). Der Ablauf steht in
   `doc/itext-font-integration-guide-generic.md` (zweisprachig).

3. **Lizenzen.** Laut `assets/fonts/README.md` stehen Amaranth, Montserrat und
   Tangerine unter der SIL Open Font License 1.1, Satisfy unter der Apache License 2.0.
   Die Lizenztexte liegen im Frontend neben den Schriftkopien
   (`OFL-amaranth.txt`, `OFL-montserrat.txt`, `OFL-tangerine.txt`,
   `LICENSE-satisfy-apache-2.0.txt`); die Angaben stammen aus dem Google-Fonts-Repository.

4. **Kopien für das Frontend.** Das Frontend enthält Kopien derselben Dateien unter
   `src/features/certificate/assets/fonts`, eingebunden über `certificateFonts.css`
   (`@font-face`, `font-display: swap`).

5. **Laden nur auf den Urkundenseiten.** `certificateFonts.css` wird ausschließlich von
   `CertificateLayoutCanvas.vue` importiert. Die Seiten der Urkunden sind eigene
   Routen, die per `import()` nachgeladen werden; die Schriftdateien holt der Browser erst,
   wenn ein Text sie verwendet. Auf den übrigen Seiten werden sie nicht geladen.

## Konsequenzen

**Positiv**

- Die grafische Ansicht zeigt dieselben Zeichen wie das PDF; die Textbreite wird im
  Browser mit der echten Schrift gemessen.
- Schriften belasten die übrigen Seiten und das Start-Bundle nicht.

**Negativ / Kosten**

- Die Schriften liegen doppelt im Repository (Backend und Frontend) und müssen
  synchron bleiben; die Kopie ist nicht automatisiert.
- Im Backend-Ordner `fonts` liegen keine Lizenztexte; sie stehen nur im Frontend.
  Ob die Auslieferung des Backends die Lizenzbedingungen erfüllt (Weitergabe der
  Lizenztexte), ist nicht dokumentiert.
- Eigene Schriften außerhalb dieser Liste werden im Editor nur ähnlich dargestellt;
  in `certificateCanvas.ts` fehlt für sie eine Zuordnung.

## Alternativen (nicht dokumentiert, naheliegend)

- **Schriften nur vom Backend ausliefern** und im Frontend per Endpunkt laden:
  keine Duplikate, dafür ein weiterer Aufruf.
- **Nur Standardschriften nutzen.**

## Verwandte Dokumente

- [`backend/src/main/resources/fonts`](../../backend/src/main/resources/fonts)
- [`certificate_schema.json`](../../backend/src/main/resources/schema/certificate_schema.json)
- [`certificateFonts.css`](../../frontend/src/features/certificate/assets/certificateFonts.css)
- [`assets/fonts/README.md`](../../frontend/src/features/certificate/assets/fonts/README.de.md)
- [`certificateLayout.ts`](../../frontend/src/features/certificate/model/certificateLayout.ts)
- [`itext-font-integration-guide-generic.md`](../../doc/itext-font-integration-guide-generic.de.md)
