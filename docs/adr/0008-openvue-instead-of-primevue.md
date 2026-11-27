# ADR 0008: OpenVue instead of PrimeVue as UI library

> [Deutsch](0008-openvue-instead-of-primevue.de.md) · **English**

- **Status:** accepted
- **Date:** 2026-11-24
- **Context:** Commit `49576fc7` ("migrate from PrimeVue to OpenVue 1.1.1")

## Context

Since its start (2023-12-17), the frontend used PrimeVue as its component library with
the theme package `@primeuix/themes`. On 2026-11-24 a single commit switched to OpenVue 1.1.1.
The commit message only states the procedure and the verification. The reason comes from the
project owner: PrimeVue is no longer freely usable from version 5 onward.

## Decision

1. **OpenVue replaces PrimeVue.** Imports changed from `primevue/*` to `openvue/*`,
   the theme from `@primeuix/themes` to `@openvue/themes`. The switch was done with the
   tool `@openvue/migrate` 1.1.1 (55 files in the commit). Components and their
   usage (Button, Dialog, Select, ColorPicker, …) kept the same structure.

2. **Versions pinned exactly.** `openvue` and `@openvue/themes` are listed in
   `package.json` without a version range, at `1.1.1`.

3. **The identifiers `primevue` remain in a few places.** `usePrimeVue` is imported
   from `openvue/config`; files and texts such as `PrimevueMessages.ts` and
   `locales/primevue` keep their names.

Rationale for the switch (project owner): PrimeVue is no longer freely usable from version 5 onward.

## Consequences

**Positive**

- A mechanical migration without reworking the pages; type check, build, lint and
  unit tests passed afterwards, according to the commit message.

**Negative / costs**

- README and `.cursorrules` still mention PrimeVue.
- The exact version pin prevents automatic patch updates of the library;
  Renovate has to raise it ([ADR 0018](0018-renovate-and-auto-merge.md)).
- The comment in the nginx configuration about the CSP still refers to PrimeVue.

## Alternatives (not documented, obvious candidates)

- **Stay on PrimeVue 4:** no rework, but no updates to version 5, which is no longer
  freely usable.
- **Another library** (Vuetify, Element Plus, Nuxt UI): documented as not considered.

## Related documents

- [Frontend `package.json`](../../frontend/package.json)
- [`i18n.ts`](../../frontend/src/i18n.ts)
- [`PrimevueMessages.ts`](../../frontend/src/PrimevueMessages.ts)
- [`default.conf`](../../frontend/default.conf)
- [ADR 0007: Frontend stack and structure](0007-frontend-stack-and-structure.md)
