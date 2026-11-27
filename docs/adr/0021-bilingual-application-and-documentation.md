# ADR 0021: Bilingual application and documentation (English and German)

> [Deutsch](0021-bilingual-application-and-documentation.de.md) · **English**

- **Status:** accepted
- **Date:** 2026-11-27
- **Context:** The application has been bilingual since commit `ca9c5dfb` ("Dynamische Sprachumschaltung", 2023-12-17). The documentation scheme was set when the user manual and the ADRs were created.

## Context

Resulter is run in German and English. The frontend already supports both languages. The
documentation grew unevenly: the README held a German and an English part in one file,
`docs/plans`, `doc/prometheus-token.md` and `CHANGELOG.md` are German only, and the iText
font guide states that it is bilingual within one file. The new user manual and the ADRs
needed a rule for languages and file names.

## Decision

1. **The application is bilingual: English and German.** The frontend uses `vue-i18n` with the
   message files `frontend/src/locales/en.json` and `de.json`. `SUPPORT_LOCALES` in
   `frontend/src/i18n.ts` lists exactly these two languages. German is the default, English the
   fallback. The language is the first part of every URL (`/:locale/...`), and a switch in the
   header changes it. The texts of the component library have their own files in
   `frontend/src/locales/primevue`. Imprint and privacy text come per language from the
   environment variables `VITE_IMPRESS_TEXT_DE`/`_EN` and `VITE_PRIVACY_TEXT_DE`/`_EN`. A new
   interface text must be added to both message files.

2. **Documentation exists in both languages.** This applies to the README, the user manual
   (`docs/manual`) and the ADRs (`docs/adr`). **English is the primary version.** It keeps the
   plain file name (`README.md`, `0001-...md`). **The German version has the same name with the
   language code before the extension** (`README.de.md`, `0001-....de.md`), in the same directory.

3. **The versions link to each other.** Every document has a language switch near the top (line 3 in the ADRs and the manual, below the title in the README):
   `> [Deutsch](name.de.md) · **English**` in the English file and
   `> **Deutsch** · [English](name.md)` in the German file. Links between documents point to the
   version of the same language.

4. **The README is split.** `README.md` (English) and `README.de.md` (German) replace the single
   bilingual file. `bump-version.sh` updates the version badge in both.

5. **Screenshots are kept per language**, because the interface texts are part of the image.
   The English images are in `images/`, the German ones in `images/de/`. Image names are English.

6. **A change to a document changes both versions** in the same commit. If the versions
   differ, the English text is the reference.

## Consequences

**Positive**

- Users and contributors can read the project in either language. English is the common
  language for outside contributors, German for the clubs that use the application.
- Plain file names for the English files keep existing links to `README.md` working.
- The language code in the file name makes missing translations visible in a directory listing.

**Negative / costs**

- Every documentation change has to be made twice. The two versions can drift apart.
- No automated check compares the versions. A script that checks that every English document has a
  German sibling and that the links match was used once when the files were created; it is not
  part of the build. The message files `en.json` and `de.json` are not compared by a test either
  (the existing tests in `frontend/test/i18n.spec.ts` cover the helper functions only).
- Screenshots have to be taken twice, once per language.
- Some files do not follow the scheme yet (see below).

**Open**

- `CHANGELOG.md`, `docs/plans/*`, `doc/prometheus-token.md` and `doc/itext-font-integration-guide-generic.md`
  are German only or bilingual within one file. They are not covered by this decision yet and
  should be moved to the scheme.
- The backend returns no localized texts. Texts in the certificate layout are content, not
  interface text, and are not translated.

## Alternatives (not documented, obvious candidates)

- **German only** (the language of the project owner and the clubs).
- **English only** for documentation, as many open source projects do.
- **Both languages in one file**, as the README did until now. This keeps the versions together but
  makes long documents hard to read and to link.
- **A directory per language** (`docs/en/...`, `docs/de/...`) instead of a file suffix.

## Related documents

- [`i18n.ts`](../../frontend/src/i18n.ts)
- [`en.json`](../../frontend/src/locales/en.json)
- [`de.json`](../../frontend/src/locales/de.json)
- [`router/index.ts`](../../frontend/src/router/index.ts)
- [`bump-version.sh`](../../bump-version.sh)
- [README](../../README.md) · [README (Deutsch)](../../README.de.md)
- [User manual](../manual/README.md) · [Benutzerhandbuch](../manual/README.de.md)
