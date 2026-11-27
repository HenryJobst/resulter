# Changelog

> [Deutsch](CHANGELOG.de.md) · **English**

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versioning follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

---

## [4.8.15] - 2026-05-29

### Security
- **GHSA-ph9p-34f9-6g65** (high): raised `tmp` to v0.2.7 via an override in `pnpm-workspace.yaml`
  - Transitive: `nx` → `@nx/devkit` → `tmp@0.2.4`
  - Path traversal vulnerability caused by unsanitized `prefix`/`postfix` parameters; dev dependency only, no production risk

---

## [4.8.14] - 2026-05-29

### Fixed
- **BFF logout**: POST `/bff/logout` returned 403 because `PathPatternRequestMatcher` does not match correctly in Spring Security 7.x with Traefik `stripprefix` + `X-Forwarded-Prefix`
  - Changed `logoutRequestMatcher` and `ignoringRequestMatchers` in `BffSecurityConfiguration` to lambda matchers using `getServletPath()`
  - `BffController.getCsrfToken()` now calls `csrfToken.getToken()` to resolve the deferred token and actually set the `XSRF-TOKEN` cookie (Spring Security 6+ lazy evaluation)
  - `sessionApiSecurityFilterChain` returns 401 instead of 403 for unauthenticated requests; added `/error` as `permitAll()`

---

## [4.8.13] - 2026-05-24

### Dependencies

#### Frontend
- **sass** to v1.100.0 (#380)
- **TanStack Query Monorepo** updated to the current version (#383)

---

## [4.8.12] - 2026-05-24

### Fixed
- **CI**: added `--trust-lockfile` to all `pnpm install` calls in `build.yml` and `build-paketo.yml`
  - pnpm v11.3.0 enforces a 24h `minimumReleaseAge` policy, which blocked recently merged lock file entries (TanStack packages)
  - Committed lock files are trusted artifacts (CI-checked, code-reviewed); `--trust-lockfile` is the approach intended by the pnpm docs

---

## [4.8.11] - 2026-05-24

### Security
- **CVE-2026-8723** (CVSS 5.3, medium): raised `qs` to v6.15.2 via an override in `pnpm-workspace.yaml`
  - Transitive: `@nx/web` → `http-server` → `union` → `qs@6.14.2`
  - `qs.stringify` with `arrayFormat:'comma'` + `encodeValuesOnly:true` threw a synchronous `TypeError` on `null`/`undefined` elements (DoS)

---

## [4.8.10] - 2026-05-24

### Dependencies

#### Frontend
- **pnpm** 11.2.2 → 11.3.0 (#382)
- **NX Monorepo** 22.7.2 → 22.7.3 (`nx`, `@nx/eslint`, `@nx/playwright`, `@nx/vite`, `@nx/vue`, `@nx/web`) (#378)
- **TanStack Query Monorepo** updated to the current version (#379)
- **@tsconfig/node22** replaced by **@tsconfig/node24** (`^22.0.0` → `^24.0.0`) (#375)
- **npm-run-all2** updated to v9.0.1 (#377)
- **baseline-browser-mapping** updated to v2.10.32 (#376)

#### Backend
- **hibernate-enhance-maven-plugin** 6.6.50.Final → 6.6.51.Final (#381)

---

## [4.8.9] - 2026-05-24

### Fixed
- Ignored Go 1.26.2 CVEs in Paketo buildpack helper binaries

---

## [4.8.8] - 2026-05-23

### Fixed
- `pnpm-workspace.yaml` is copied before `pnpm install` in the Dockerfile
- Set `CI=true` for the Git commit to bypass the pnpm TTY check
