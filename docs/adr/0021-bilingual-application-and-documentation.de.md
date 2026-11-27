# ADR 0021: Zweisprachige Anwendung und Dokumentation (Englisch und Deutsch)

> **Deutsch** · [English](0021-bilingual-application-and-documentation.md)

- **Status:** akzeptiert
- **Datum:** 2026-11-27
- **Kontext:** Die Anwendung ist seit dem Commit `ca9c5dfb` („Dynamische Sprachumschaltung“, 2023-12-17) zweisprachig. Das Schema für die Dokumentation wurde beim Anlegen von Benutzerhandbuch und ADR festgelegt.

## Kontext

Resulter wird auf Deutsch und Englisch geführt. Das Frontend unterstützt beide Sprachen schon.
Die Dokumentation ist ungleichmäßig gewachsen: Die README enthielt einen deutschen und einen
englischen Teil in einer Datei, `docs/plans`, `doc/prometheus-token.md` und `CHANGELOG.md` sind
nur deutsch, und der iText-Schriftleitfaden nennt sich zweisprachig innerhalb einer Datei. Für
das neue Benutzerhandbuch und die ADR fehlte eine Regel für Sprachen und Dateinamen.

## Entscheidung

1. **Die Anwendung ist zweisprachig: Englisch und Deutsch.** Das Frontend nutzt `vue-i18n` mit
   den Textdateien `frontend/src/locales/en.json` und `de.json`. `SUPPORT_LOCALES` in
   `frontend/src/i18n.ts` nennt genau diese beiden Sprachen. Deutsch ist die Standardsprache,
   Englisch die Ersatzsprache. Die Sprache ist der erste Teil jeder URL (`/:locale/...`), und ein
   Schalter in der Kopfzeile wechselt sie. Die Texte der Komponentenbibliothek haben eigene
   Dateien unter `frontend/src/locales/primevue`. Impressum und Datenschutztext kommen je Sprache
   aus den Umgebungsvariablen `VITE_IMPRESS_TEXT_DE`/`_EN` und `VITE_PRIVACY_TEXT_DE`/`_EN`.
   Ein neuer Oberflächentext muss in beide Textdateien eingetragen werden.

2. **Die Dokumentation gibt es in beiden Sprachen.** Das gilt für die README, das
   Benutzerhandbuch (`docs/manual`) und die ADR (`docs/adr`). **Englisch ist die primäre
   Fassung.** Sie behält den einfachen Dateinamen (`README.md`, `0001-...md`). **Die deutsche
   Fassung hat denselben Namen mit dem Sprachkürzel vor der Endung** (`README.de.md`,
   `0001-....de.md`), im selben Verzeichnis.

3. **Die Fassungen verlinken sich gegenseitig.** Jedes Dokument hat oben einen Sprachwechsel (Zeile 3 in ADR und Handbuch, unter dem Titel in der README):
   `> [Deutsch](name.de.md) · **English**` in der englischen und
   `> **Deutsch** · [English](name.md)` in der deutschen Datei. Verweise zwischen Dokumenten
   zeigen auf die Fassung derselben Sprache.

4. **Die README ist aufgeteilt.** `README.md` (Englisch) und `README.de.md` (Deutsch) ersetzen
   die eine zweisprachige Datei. `bump-version.sh` aktualisiert das Versions-Badge in beiden.

5. **Screenshots gibt es je Sprache**, weil die Oberflächentexte Teil des Bildes sind. Die
   englischen Bilder liegen in `images/`, die deutschen in `images/de/`. Die Bildnamen sind
   englisch.

6. **Eine Änderung an einem Dokument ändert beide Fassungen** im selben Commit. Weichen die
   Fassungen ab, gilt der englische Text.

## Konsequenzen

**Positiv**

- Nutzer und Mitwirkende können das Projekt in beiden Sprachen lesen. Englisch ist die gemeinsame
  Sprache für externe Mitwirkende, Deutsch für die Vereine, die die Anwendung nutzen.
- Die einfachen Dateinamen der englischen Dateien lassen bestehende Verweise auf `README.md`
  weiter funktionieren.
- Das Sprachkürzel im Dateinamen macht fehlende Übersetzungen in der Dateiliste sichtbar.

**Negativ / Kosten**

- Jede Änderung an der Dokumentation muss zweimal erfolgen. Die beiden Fassungen können
  auseinanderlaufen.
- Kein automatischer Test vergleicht die Fassungen. Ein Skript, das prüft, dass jedes englische
  Dokument ein deutsches Gegenstück hat und die Verweise stimmen, wurde einmal beim Anlegen der
  Dateien benutzt und ist nicht Teil des Builds. Auch die Textdateien `en.json` und `de.json`
  vergleicht kein Test (die vorhandenen Tests in `frontend/test/i18n.spec.ts` decken nur die
  Hilfsfunktionen ab).
- Screenshots müssen zweimal aufgenommen werden, einmal je Sprache.
- Einige Dateien folgen dem Schema noch nicht (siehe unten).

**Offen**

- `CHANGELOG.md`, `docs/plans/*`, `doc/prometheus-token.md` und
  `doc/itext-font-integration-guide-generic.md` sind nur deutsch oder in einer Datei
  zweisprachig. Sie sind von dieser Entscheidung noch nicht erfasst und sollten auf das Schema
  umgestellt werden.
- Das Backend liefert keine lokalisierten Texte. Texte im Urkunden-Layout sind Inhalt, keine
  Oberflächentexte, und werden nicht übersetzt.

## Alternativen (nicht dokumentiert, naheliegend)

- **Nur Deutsch** (die Sprache des Projektinhabers und der Vereine).
- **Nur Englisch** für die Dokumentation, wie bei vielen Open-Source-Projekten.
- **Beide Sprachen in einer Datei**, wie bisher in der README. Das hält die Fassungen zusammen,
  macht lange Dokumente aber schwer lesbar und schwer verlinkbar.
- **Ein Verzeichnis je Sprache** (`docs/en/...`, `docs/de/...`) statt eines Dateisuffixes.

## Verwandte Dokumente

- [`i18n.ts`](../../frontend/src/i18n.ts)
- [`en.json`](../../frontend/src/locales/en.json)
- [`de.json`](../../frontend/src/locales/de.json)
- [`router/index.ts`](../../frontend/src/router/index.ts)
- [`bump-version.sh`](../../bump-version.sh)
- [README (Deutsch)](../../README.de.md) · [README](../../README.md)
- [Benutzerhandbuch](../manual/README.de.md) · [User manual](../manual/README.md)
