# ADR 0007: Frontend-Stack und feature-orientierte Struktur

> **Deutsch** · [English](0007-frontend-stack-and-structure.md)

- **Status:** akzeptiert
- **Datum:** 2023-12-17
- **Kontext:** Start des Frontend-Unterprojekts („Vue.js Frontend Subprojekt hinzugefügt“, 2023-12-17); TanStack Query kam am 2023-12-27 hinzu

## Kontext

Resulter ist eine Webanwendung mit REST-Backend. Das Frontend wurde am 2023-12-17
als eigenes Unterprojekt `frontend/` angelegt. Eine schriftliche Begründung der
Technologiewahl steht im Repository nicht; belegt sind die Abhängigkeiten in
`frontend/package.json` und die Ordnerstruktur.

## Entscheidung

1. **Vue 3 mit TypeScript, gebaut mit Vite.** Einstieg `src/main.ts`, Pfadalias `@`
   auf `src`. Der Paketmanager ist pnpm; Nx (`nx.json`) ruft Typprüfung (`vue-tsc`) und
   Build.

2. **Zustand und Serverdaten getrennt.** Pinia (mit `pinia-plugin-persistedstate`) hält
   Client-Zustand wie Anmeldung und Einstellungen; TanStack Query (`@tanstack/vue-query`)
   hält Serverdaten und deren Caching. HTTP-Aufrufe laufen über `axios`.

3. **Mehrsprachigkeit mit vue-i18n.** Texte stehen in `src/locales/de.json` und
   `en.json` (Standardsprache `de`, Rückfall `en`); UI-Komponenten-Meldungen liefert
   `PrimevueMessages.ts`. `i18n.spec.ts` prüft die Übersetzungen.

4. **UI-Komponenten und Styling.** Komponenten kommen aus OpenVue
   ([ADR 0008](0008-openvue-instead-of-primevue.de.md)); Tailwind CSS 4 wird über PostCSS
   eingebunden (`@tailwindcss/postcss`). Zusätzlich liegt `uno.config.ts` mit
   UnoCSS-Konfiguration im Projekt, und `@unocss/eslint-plugin` ist installiert.
   Ob UnoCSS aktiv genutzt wird oder ein Überbleibsel ist: nicht geprüft.

5. **Feature-Ordner statt technischer Ordner.** Jedes Fachgebiet hat einen Ordner
   unter `src/features/<name>` (z. B. `event`, `cup`, `person`, `organisation`,
   `certificate`, `analysis`, `auth`, `media`) mit `model/`, `services/`, `pages/`,
   `widgets/` und gegebenenfalls `store/` oder `composables/`. Jedes Feature bringt
   seine Routen mit (`<name>-routing.ts`, Seiten per `import()` geladen) und eine
   `<Name>Index.vue`. Gemeinsame Teile liegen in `src/components` und
   `features/common` bzw. `features/generic`.

6. **Kommunikation über REST.** Das Frontend ruft die REST-API des Backends auf; die
   Authentifizierung folgt [ADR 0004](0004-bff-with-keycloak-and-session-cookie.de.md).

7. **Auslieferung als statische Seite.** Das Frontend-Image enthält nginx mit
   `default.conf` (Single-Page-Fallback, CSP und weitere Sicherheits-Header).

## Konsequenzen

**Positiv**

- Zusammengehörender Code (Modell, Service, Seiten) liegt beieinander; Seiten werden
  nachgeladen, sodass nicht jede Funktion im Start-Bundle steckt.
- Klare Trennung zwischen Client-Zustand (Pinia) und Serverzustand (TanStack Query).

**Negativ / Kosten**

- Die Content-Security-Policy erlaubt `style-src 'unsafe-inline'`, weil die
  UI-Bibliothek zur Laufzeit Styles einfügt (Kommentar in `default.conf`).
- Zwei CSS-Werkzeuge im Projekt (Tailwind und UnoCSS-Konfiguration).
- README und `.cursorrules` nennen noch PrimeVue und Keycloak im Frontend und sind
  damit gegenüber dem Code veraltet.

## Alternativen (nicht dokumentiert, naheliegend)

- **Andere Frameworks** (React, Angular): nicht erwogen dokumentiert.
- **Alles in Pinia** statt TanStack Query: einfacher, aber ohne Cache-Verwaltung.

## Verwandte Dokumente

- [`package.json` des Frontends](../../frontend/package.json)
- [`main.ts`](../../frontend/src/main.ts)
- [`i18n.ts`](../../frontend/src/i18n.ts)
- [`certificate-routing.ts`](../../frontend/src/features/certificate/certificate-routing.ts)
- [`default.conf`](../../frontend/default.conf)
- [`vite.config.ts`](../../frontend/vite.config.ts)
- [README, Abschnitt Technologies](../../README.md)
