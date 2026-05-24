# Changelog

Alle wesentlichen Änderungen an diesem Projekt werden in dieser Datei dokumentiert.

Das Format basiert auf [Keep a Changelog](https://keepachangelog.com/de/1.1.0/).
Die Versionierung folgt [Semantic Versioning](https://semver.org/lang/de/).

## [Unreleased]

### Dependencies (ausstehend)
- `sass` auf v1.100.0 (#380, Rebase durch Renovate ausstehend)

---

## [4.8.12] - 2026-05-24

### Fixed
- **CI**: `--trust-lockfile` zu allen `pnpm install`-Aufrufen in `build.yml` und `build-paketo.yml` hinzugefügt
  - pnpm v11.3.0 erzwingt eine 24h-`minimumReleaseAge`-Policy, die kürzlich gemergte Lock-File-Einträge (TanStack-Pakete) blockierte
  - Committed Lock-Files sind vertrauenswürdige Artefakte (CI-geprüft, Code-reviewed) — `--trust-lockfile` ist der laut pnpm-Docs vorgesehene Weg

---

## [4.8.11] - 2026-05-24

### Security
- **CVE-2026-8723** (CVSS 5.3, mittel): `qs` auf v6.15.2 angehoben via Override in `pnpm-workspace.yaml`
  - Transitiv: `@nx/web` → `http-server` → `union` → `qs@6.14.2`
  - `qs.stringify` mit `arrayFormat:'comma'` + `encodeValuesOnly:true` auf `null`/`undefined`-Elementen warf synchronen `TypeError` (DoS)

---

## [4.8.10] - 2026-05-24

### Dependencies

#### Frontend
- **pnpm** 11.2.2 → 11.3.0 (#382)
- **NX Monorepo** 22.7.2 → 22.7.3 (`nx`, `@nx/eslint`, `@nx/playwright`, `@nx/vite`, `@nx/vue`, `@nx/web`) (#378)
- **TanStack Query Monorepo** auf aktuelle Version aktualisiert (#379)
- **@tsconfig/node22** durch **@tsconfig/node24** ersetzt (`^22.0.0` → `^24.0.0`) (#375)
- **npm-run-all2** auf v9.0.1 aktualisiert (#377)
- **baseline-browser-mapping** auf v2.10.32 aktualisiert (#376)

#### Backend
- **hibernate-enhance-maven-plugin** 6.6.50.Final → 6.6.51.Final (#381)

---

## [4.8.9] - 2026-05-24

### Fixed
- Go 1.26.2 CVEs in Paketo-Buildpack-Hilfsbinärdateien ignoriert

---

## [4.8.8] - 2026-05-23

### Fixed
- `pnpm-workspace.yaml` wird vor `pnpm install` in der Dockerfile kopiert
- `CI=true` für Git-Commit gesetzt, um den pnpm-TTY-Check zu umgehen
