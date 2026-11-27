# ADR 0007: Frontend stack and feature-oriented structure

> [Deutsch](0007-frontend-stack-and-structure.de.md) · **English**

- **Status:** accepted
- **Date:** 2023-12-17
- **Context:** Start of the frontend subproject ("Vue.js Frontend Subprojekt hinzugefügt", 2023-12-17); TanStack Query was added on 2023-12-27

## Context

Resulter is a web application with a REST backend. The frontend was created on 2023-12-17
as a separate subproject `frontend/`. The repository contains no written rationale for
the technology choice; the evidence is the dependencies in
`frontend/package.json` and the folder structure.

## Decision

1. **Vue 3 with TypeScript, built with Vite.** Entry point `src/main.ts`, path alias `@`
   for `src`. The package manager is pnpm; Nx (`nx.json`) invokes the type check (`vue-tsc`) and
   the build.

2. **Client state and server data kept separate.** Pinia (with `pinia-plugin-persistedstate`) holds
   client state such as login and settings; TanStack Query (`@tanstack/vue-query`)
   holds server data and its caching. HTTP calls go through `axios`.

3. **Multilingual support with vue-i18n.** Texts are in `src/locales/de.json` and
   `en.json` (default language `de`, fallback `en`); UI component messages are provided by
   `PrimevueMessages.ts`. `i18n.spec.ts` checks the translations.

4. **UI components and styling.** Components come from OpenVue
   ([ADR 0008](0008-openvue-instead-of-primevue.md)); Tailwind CSS 4 is integrated via PostCSS
   (`@tailwindcss/postcss`). In addition, the project contains `uno.config.ts` with
   UnoCSS configuration, and `@unocss/eslint-plugin` is installed.
   Whether UnoCSS is actively used or is a leftover: not checked.

5. **Feature folders instead of technical folders.** Each domain area has a folder
   under `src/features/<name>` (e.g. `event`, `cup`, `person`, `organisation`,
   `certificate`, `analysis`, `auth`, `media`) with `model/`, `services/`, `pages/`,
   `widgets/` and, where needed, `store/` or `composables/`. Each feature brings
   its own routes (`<name>-routing.ts`, pages loaded via `import()`) and a
   `<Name>Index.vue`. Shared parts are in `src/components` and
   `features/common` or `features/generic`.

6. **Communication via REST.** The frontend calls the backend's REST API; authentication
   follows [ADR 0004](0004-bff-with-keycloak-and-session-cookie.md).

7. **Delivery as a static site.** The frontend image contains nginx with
   `default.conf` (single-page fallback, CSP and other security headers).

## Consequences

**Positive**

- Code that belongs together (model, service, pages) lives together; pages are
  loaded on demand, so not every function is in the start bundle.
- Clear separation between client state (Pinia) and server state (TanStack Query).

**Negative / costs**

- The content security policy allows `style-src 'unsafe-inline'` because the
  UI library inserts styles at runtime (comment in `default.conf`).
- Two CSS tools in the project (Tailwind and UnoCSS configuration).
- README and `.cursorrules` still mention PrimeVue and Keycloak in the frontend and are
  therefore out of date with respect to the code.

## Alternatives (not documented, obvious candidates)

- **Other frameworks** (React, Angular): documented as not considered.
- **Everything in Pinia** instead of TanStack Query: simpler, but without cache management.

## Related documents

- [Frontend `package.json`](../../frontend/package.json)
- [`main.ts`](../../frontend/src/main.ts)
- [`i18n.ts`](../../frontend/src/i18n.ts)
- [`certificate-routing.ts`](../../frontend/src/features/certificate/certificate-routing.ts)
- [`default.conf`](../../frontend/default.conf)
- [`vite.config.ts`](../../frontend/vite.config.ts)
- [README, section Technologies](../../README.md)
