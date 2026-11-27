# Testing Guide

> **Deutsch** · [English](TESTING.md)

Umfassende Anleitung zum Ausführen der Tests in der Resulter-Frontend-Anwendung.

## Quick Start

```bash
# Run all tests (unit + e2e)
pnpm test:all

# Run only unit tests
pnpm test:unit

# Run only E2E tests
pnpm test:e2e
```

## Unit-Tests (Vitest)

Unit-Tests sind mit Vitest und Vue Test Utils geschrieben. Sie testen einzelne Komponenten, Stores und Services isoliert.

### Grundlegende Befehle

```bash
# Run unit tests once
pnpm test:unit

# Run unit tests in watch mode (re-runs on file changes)
pnpm test:unit:watch

# Run unit tests with UI dashboard
pnpm test:unit:ui

# Run unit tests with coverage report
pnpm test:unit:coverage
```

### Bestimmte Tests ausführen

```bash
# Run tests in a specific file
pnpm test:unit:file src/features/auth/store/auth.store.spec.ts

# Run tests matching a pattern (via Vitest UI)
pnpm test:unit:ui
# Then use the UI to filter tests by name
```

### Coverage

Coverage-Reports werden im Verzeichnis `coverage/` erzeugt und zeigen:
- Statement Coverage
- Branch Coverage
- Function Coverage
- Line Coverage

Öffne `coverage/index.html` in einem Browser, um den detaillierten Report anzusehen.

## E2E-Tests (Playwright)

E2E-Tests nutzen Playwright, um die Anwendung in einem echten Browser zu testen. Sie testen vollständige Benutzerabläufe.

### Voraussetzungen

⚠️ **WICHTIG: Vor dem Ausführen der E2E-Tests musst du beide Server manuell starten!**

1. **Frontend-Server starten:**
   ```bash
   pnpm dev
   ```
   Warte, bis `VITE vX.X.X ready in XXXms` erscheint.

2. **Backend-Server starten:**
   ```bash
   cd ../backend
   ./mvnw spring-boot:run
   ```
   Warte, bis `Started ResulterApplication in X.XXX seconds` erscheint.

3. **Umgebung konfiguriert** in `e2e/.env.local`:
   ```env
   HOSTNAME=localhost
   FRONTEND_PROTOCOL=http
   PORT=5173
   BACKEND_PROTOCOL=http
   BACKEND_PORT=8080
   BACKEND_PROFILES=dev
   VITE_MODE=development
   USERNAME=<test-username>
   PASSWORD=<test-password>
   ```

### Grundlegende Befehle

```bash
# Run all E2E tests (headless mode)
pnpm test:e2e

# Run E2E tests with UI mode (interactive)
pnpm test:e2e:ui

# Run E2E tests in headed mode (see browser)
pnpm test:e2e:headed

# Run E2E tests in debug mode (step through tests)
pnpm test:e2e:debug
```

### Browserspezifische Tests

```bash
# Run tests on Chromium only
pnpm test:e2e:chromium

# Run tests on Firefox only
pnpm test:e2e:firefox

# Run tests on WebKit (Safari) only
pnpm test:e2e:webkit
```

### Authentifizierungstests

Die Authentifizierung läuft über das BFF-Muster (Backend-for-Frontend). Session-Cookies werden gespeichert und wiederverwendet.

```bash
# Run only authentication setup (establishes session)
pnpm test:e2e:auth

# Run auth setup in headed mode (see login process)
pnpm test:e2e:auth:headed

# Debug authentication flow
pnpm test:e2e:auth:debug
```

**Hinweis:** Die Auth-Session wird 10 Minuten in `e2e/.auth/storageState.json` zwischengespeichert. Lösche diese Datei, um eine erneute Authentifizierung zu erzwingen.

### Bestimmte Test-Suites

```bash
# Run main page tests
pnpm test:e2e:main

# Run event CRUD tests
pnpm test:e2e:event

# Run event form tests (comprehensive)
pnpm test:e2e:event-form
```

### Reports und Traces

```bash
# View HTML test report
pnpm test:e2e:report

# View trace for failed tests (debugging)
pnpm test:e2e:trace
```

### Bestimmte Tests ausführen

```bash
# Run specific test file
pnpm playwright test event-form.spec.ts

# Run specific test by name (grep)
pnpm playwright test -g "should create event"

# Run specific test in headed mode
pnpm playwright test event-form.spec.ts --headed -g "should create event"

# Run specific test in debug mode
pnpm playwright test event-form.spec.ts --debug -g "should create event"
```

## Alte Cypress-Tests

Cypress-Tests sind weiterhin verfügbar, für neue Tests wird aber Playwright bevorzugt.

```bash
# Run Cypress E2E tests
pnpm test:cypress

# Open Cypress in dev mode
pnpm test:cypress:dev
```

## Teststruktur

### Unit-Tests

```
frontend/test/
├── features/
│   ├── auth/
│   │   ├── store/
│   │   │   └── auth.store.spec.ts          # Auth store tests
│   │   └── services/
│   │       └── api.spec.ts                 # API interceptor tests
│   ├── common/
│   │   └── services/
│   │       └── apiResponseFunctions.spec.ts # API response helpers
│   ├── event/
│   │   └── services/
│   │       └── event.service.spec.ts       # Event service tests
│   └── ...
├── utils/
│   └── HandleError.spec.ts                 # Error handling tests
└── unit.setup.ts                           # Vitest global setup
```

### E2E-Tests

```
frontend/e2e/
├── auth-setup.ts              # BFF authentication setup
├── main.spec.ts               # Main page tests
├── event.spec.ts              # Event CRUD tests
├── event-form.spec.ts         # Event form tests
├── create_db.spec.ts          # Database tests
├── .auth/
│   └── storageState.json      # Saved session cookies
└── .env.local                 # Environment config
```

## Best Practices

### Unit-Tests

✅ **DO:**
- Pro Datei eine Komponente bzw. Funktion testen
- Externe Abhängigkeiten mocken
- Aussagekräftige Testnamen verwenden
- Randfälle und Fehlerbedingungen testen
- Tests schnell und isoliert halten

❌ **DON'T:**
- Implementierungsdetails testen
- Fragile Tests schreiben, die beim Refactoring brechen
- Echte API-Aufrufe verwenden
- Zustand zwischen Tests teilen

### E2E-Tests

✅ **DO:**
- Vollständige Benutzerabläufe testen
- Semantische Selektoren verwenden (getByRole, getByLabel)
- Testdaten nach den Tests aufräumen
- Eindeutige Namen für Testdaten verwenden
- Mit expect-Assertions auf Elemente warten

❌ **DON'T:**
- Fest codierte Wartezeiten verwenden
- Sich auf die Ausführungsreihenfolge der Tests verlassen
- Testdaten in der Datenbank zurücklassen
- CSS-Selektoren verwenden, wenn semantische Selektoren verfügbar sind

## Debugging

### Unit-Tests

```bash
# Open Vitest UI for interactive debugging
pnpm test:unit:ui

# Run specific test file in watch mode
pnpm test:unit:watch path/to/test.spec.ts

# Add debugger statement in test
test('my test', () => {
    debugger  // Execution will pause here
    expect(result).toBe(expected)
})
```

### E2E-Tests

```bash
# Debug mode (step through tests)
pnpm test:e2e:debug

# Headed mode (see browser)
pnpm test:e2e:headed

# Add pause in test
await page.pause()  // Opens Playwright Inspector

# Take screenshot
await page.screenshot({ path: 'debug.png' })

# Console logs
page.on('console', msg => console.log(msg.text()))

# Network requests
page.on('request', request => console.log('>>', request.url()))
page.on('response', response => console.log('<<', response.url()))
```

## CI/CD

### GitHub Actions

Die Tests laufen automatisch bei Push bzw. PR:

```yaml
# Unit tests
- name: Run unit tests
  run: pnpm test:unit

# E2E tests
- name: Run E2E tests
  run: pnpm test:e2e
  env:
    USERNAME: ${{ secrets.TEST_USERNAME }}
    PASSWORD: ${{ secrets.TEST_PASSWORD }}
```

### Lokaler Pre-Commit

Unit-Tests laufen vor dem Commit automatisch über lint-staged.

## Fehlerbehebung

### Unit-Tests

**Problem:** Tests schlagen mit "Cannot find module" fehl
- **Lösung:** Führe `pnpm install` aus, um die Abhängigkeiten zu installieren

**Problem:** Tests laufen in ein Timeout
- **Lösung:** Erhöhe das Timeout im Test oder prüfe auf Probleme mit asynchronem Code

**Problem:** Mock funktioniert nicht
- **Lösung:** Stelle sicher, dass vi.mock() am Anfang der Datei vor den Imports steht

### E2E-Tests

**Problem:** Authentifizierung schlägt fehl
- **Lösung:**
  - Lösche `e2e/.auth/storageState.json`
  - Prüfe, ob das Backend läuft
  - Prüfe die Zugangsdaten in `e2e/.env.local`
  - Führe `pnpm test:e2e:auth:headed` aus, um den Login-Vorgang zu sehen

**Problem:** "Element not found"
- **Lösung:**
  - Prüfe, ob sich der Selektor in der UI geändert hat
  - Stelle sicher, dass das Element vor der Interaktion sichtbar ist
  - Prüfe auf Ladezustände

**Problem:** "Timeout waiting for page"
- **Lösung:**
  - Prüfe, ob das Backend läuft
  - Erhöhe das Timeout in playwright.config.ts
  - Prüfe die Netzwerkanfragen im Headed-Modus

**Problem:** "Session expired"
- **Lösung:**
  - Lösche `e2e/.auth/storageState.json`, um eine erneute Authentifizierung zu erzwingen
  - Die Session ist 10 Minuten gültig

## Performance

### Unit-Tests

- Schnell: ca. 5-10 Sekunden für 639 Tests
- Laufen standardmäßig parallel
- Den Modus --watch während der Entwicklung nutzen

### E2E-Tests

- Langsamer: ca. 30-60 Sekunden je nach Tests
- Laufen in der CI nacheinander
- Den Headed-Modus nur zum Debuggen nutzen
- Authentifizierung wird 10 Minuten zwischengespeichert

## Ressourcen

- [Vitest Documentation](https://vitest.dev)
- [Vue Test Utils](https://test-utils.vuejs.org)
- [Playwright Documentation](https://playwright.dev)
- [Testing Library](https://testing-library.com)

## Fragen?

Bei Fragen zum Testen schau in:
1. Diese Anleitung
2. Beispiele für Testdateien in `test/` und `e2e/`
3. Die Komponentendokumentation
4. Die Teamdokumentation
