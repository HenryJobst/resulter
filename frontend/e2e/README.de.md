# E2E-Tests mit Playwright

> **Deutsch** · [English](README.md)

End-to-End-Tests für die Resulter-Frontend-Anwendung mit **Datenbankisolation** für zuverlässige, parallele Testausführung.

## 📚 Dokumentation

- **[E2E_DATABASE_ISOLATION.md](./E2E_DATABASE_ISOLATION.de.md)** - Vollständige technische Anleitung zur Datenbankisolation (Architektur, Implementierung, Best Practices)
- **[TROUBLESHOOTING.md](./TROUBLESHOOTING.de.md)** - Ausführliche Anleitung zur Fehlerbehebung bei häufigen Problemen
- **Diese README** - Schnellstart und Überblick

---

## 🚀 Schnellstart

### 1. Voraussetzungen

**Umgebung einrichten:**
```bash
# e2e/.env.local anlegen
cat > e2e/.env.local << 'EOF'
HOSTNAME=localhost
FRONTEND_PROTOCOL=http
PORT=5173
BACKEND_PROTOCOL=http
BACKEND_PORT=8080
BACKEND_PROFILES=e2e-frontend-tests
VITE_MODE=development
USERNAME=your-test-username
PASSWORD=your-test-password
CREATEDATABASE_API_TOKEN=your-token-here
EOF
```

**Sicherstellen, dass Docker läuft** (für die Datenbankisolation erforderlich):
```bash
docker ps  # Should show running containers
```

### 2. Server starten

**Terminal 1 - Backend:**
```bash
cd backend
./start-e2e-frontend-tests.sh
# Wait for: "Started ResulterApplication"
```

**Terminal 2 - Frontend:**
```bash
pnpm dev
# Wait for: "VITE ready"
```

### 3. Tests ausführen

**Terminal 3 - E2E-Tests:**
```bash
# Alle Tests ausführen
pnpm test:e2e

# Bestimmte Testdatei ausführen
pnpm playwright test event-form.spec.ts

# Im Headed-Modus ausführen (Browser sichtbar)
pnpm playwright test event-form.spec.ts --headed

# Debug-Modus (schrittweise)
pnpm playwright test event-form.spec.ts --debug
```

---

## 📋 Überblick

### Testdateien

| Datei | Beschreibung | Datenbankisolation |
|------|-------------|-------------------|
| `auth-setup.ts` | BFF-Authentifizierungs-Setup | Keine |
| `main.spec.ts` | Hauptablauf der Anwendung | Keine |
| `create_db.spec.ts` | Demo zur Datenbankerstellung | Pro Test |
| `event.spec.ts` | Einfaches Event-CRUD | Pro Test |
| `event-form.spec.ts` | Umfassende Tests des Event-Formulars | Gemischt (pro Test + Suite-Ebene) |
| `cup.spec.ts` | Einfaches Cup-CRUD | Pro Test |
| `cup-form.spec.ts` | Umfassende Tests des Cup-Formulars | Gemischt (pro Test + Suite-Ebene) |

### Teststatistik

**Gesamt:** ~120 Tests in 3 Browsern (Chromium, Firefox, Edge)
**Bestanden:** ~115/120 (96%)

| Browser | Status | Tests | Hinweise |
|---------|--------|-------|-------|
| Chromium | ✅ | 40/40 | Alle Tests bestanden |
| Firefox | ⚠️ | 38/40 | 2 instabile Tests (Ressourcenkonkurrenz) |
| Edge | ✅ | 40/40 | Alle Tests bestanden (stabil, wenn separat ausgeführt) |
| Webkit | ⚠️ | Ausgeschlossen | Browserspezifische Probleme (siehe Bekannte Probleme) |

---

## 🔒 Authentifizierung (BFF-Muster)

E2E-Tests verwenden die OAuth2-Authentifizierung per Backend-for-Frontend (BFF):

1. `auth-setup.ts` startet den OAuth2-Ablauf → `/oauth2/authorization/keycloak`
2. Das Backend leitet zur Keycloak-Anmeldeseite weiter
3. Der Test trägt die Zugangsdaten ein und sendet sie ab
4. Keycloak leitet mit einem Autorisierungscode zurück
5. Das Backend tauscht den Code gegen Tokens → Session-Cookie (HTTP-only)
6. Das Backend leitet zum Frontend weiter
7. Playwright speichert die Session in `e2e/.auth/storageState.json`
8. Alle Tests verwenden die Session wieder (10 Minuten gültig)

**Wichtig:** Die Session wird automatisch erneuert, wenn sie jünger als 10 Minuten ist. `e2e/.auth/storageState.json` löschen, um eine erneute Authentifizierung zu erzwingen.

---

## 💾 Datenbankisolation

Jeder Test kann in einer eigenen, isolierten PostgreSQL-Datenbank laufen. Das verhindert Datenvermischung und ermöglicht sichere parallele Ausführung.

### Funktionsweise (Kurzfassung)

1. **Test fordert Datenbank an:** `createTestDatabase()` → Gibt eine UUID zurück
2. **Test setzt Cookie:** `X-DB-Identifier: <uuid>`
3. **Backend leitet Anfragen um:** Cookie → Isolierte Datenbank
4. **Automatische Bereinigung:** Datenbank wird nach 30 Sekunden Inaktivität gelöscht

**Alle Details siehe:** [E2E_DATABASE_ISOLATION.md](./E2E_DATABASE_ISOLATION.de.md)

### Drei Isolationsmuster

#### 🔹 Isolation pro Test
**Verwenden für:** Erstellen/Ändern, wenn ein frischer Zustand nötig ist

```typescript
import { createTestDatabase } from './helpers/database'

test.beforeEach(async ({ page }) => {
    const dbId = await createTestDatabase()

    await page.context().addCookies([{
        name: 'X-DB-Identifier',
        value: dbId,
        domain: 'localhost',
        path: '/',
        httpOnly: false,
        secure: false,
        sameSite: 'Lax',
    }])

    await page.goto('/en/event/new')
})

test('create event', async ({ page }) => {
    await page.fill('[name="name"]', 'Test Event')
    await page.click('button:has-text("Save")')

    // No cleanup needed!
})
```

#### 🔹 Gemeinsame Nutzung auf Suite-Ebene
**Verwenden für:** Bearbeiten/Aktualisieren derselben Entität

```typescript
let sharedDbId: string

test.beforeAll(async ({ browser }) => {
    sharedDbId = await createTestDatabase({ timeoutMs: 300000 }) // 5 min

    // Gemeinsame Testdaten einmalig anlegen
    const context = await browser.newContext({ storageState: 'e2e/.auth/storageState.json' })
    const page = await context.newPage()

    await page.context().addCookies([{
        name: 'X-DB-Identifier',
        value: sharedDbId,
        domain: 'localhost',
        path: '/',
        httpOnly: false,
        secure: false,
        sameSite: 'Lax',
    }])

    // Event anlegen
    await page.goto('/en/event/new')
    await page.fill('[name="name"]', 'Shared Event')
    await page.click('button:has-text("Save")')

    await context.close()
})

test.beforeEach(async ({ page }) => {
    // Alle Tests verwenden dieselbe Datenbank
    await page.context().addCookies([{
        name: 'X-DB-Identifier',
        value: sharedDbId,
        domain: 'localhost',
        path: '/',
        httpOnly: false,
        secure: false,
        sameSite: 'Lax',
    }])
})
```

#### 🔹 Keine Isolation
**Verwenden für:** Reine Lesetests (Validierung/UI)

```typescript
test('validate form', async ({ page }) => {
    await page.goto('/en/event/new')
    await page.click('button:has-text("Save")')

    await expect(page.locator('text=Name is required')).toBeVisible()
})
```

### Vorteile

✅ **Keine gegenseitige Beeinflussung der Tests** - Vollständige Datenisolation
✅ **Keine manuelle Bereinigung** - Automatisch nach 30 s gelöscht
✅ **Sicher bei paralleler Ausführung** - Jeder Test hat eine eigene Datenbank
✅ **Einfacherer Code** - Über 12 Bereinigungszeilen pro Test entfallen
✅ **Frischer Zustand** - Jedes Mal eine saubere Datenbank

---

## 🧪 E2E-Tests für EventForm

Umfassende Tests für die Komponente `EventForm.vue` (insgesamt 16 Tests):

### Testabdeckung

**Tests zum Erstellen von Events (8 Tests)** - Isolation pro Test
- Alle Formularfelder darstellen
- Mit allen Feldern / minimalen Feldern erstellen
- Zurücknavigieren ohne Speichern
- Interaktionen mit Datums-/Zeitauswahl
- Mehrfachauswahl von Organisationen
- Event-Status ändern

**Tests zum Bearbeiten von Events (4 Tests)** - Gemeinsame Nutzung auf Suite-Ebene
- Event-Namen bearbeiten
- Datum und Uhrzeit bearbeiten
- Organisation hinzufügen
- Urkunde hinzufügen

**Formularvalidierung (3 Tests)** - Keine Isolation
- Absenden des leeren Formulars
- Validierung des Datumsformats
- Formular leeren

**Ladezustände (1 Test)** - Keine Isolation
- Ladeanzeigen

---

## 🏆 E2E-Tests für CupForm

Umfassende Tests für die Komponente `CupForm.vue` (insgesamt 17 Tests):

### Testabdeckung

**Tests zum Erstellen von Cups (7 Tests)** - Isolation pro Test
- Alle Formularfelder darstellen
- Mit allen Feldern / minimalen Feldern erstellen
- Zurücknavigieren ohne Speichern
- Schaltflächen zum Erhöhen/Verringern des Jahres
- Mehrfachauswahl von Events
- Cup-Typ ändern

**Tests zum Bearbeiten von Cups (4 Tests)** - Gemeinsame Nutzung auf Suite-Ebene
- Cup-Namen bearbeiten
- Cup-Jahr bearbeiten
- Cup-Typ bearbeiten
- Bearbeitung ohne Speichern abbrechen

**Formularvalidierung (3 Tests)** - Keine Isolation
- Jahr mit aktuellem Jahr vorbelegt
- Gültigen Jahresbereich akzeptieren (1970-9999)
- Namensfeld ist Pflicht

**Ladezustände (2 Tests)** - Keine Isolation
- Ladezustand für Cup-Typen
- Ladezustand für Events

### Wichtige Hinweise

**Feld Cup-Typ:** Das Feld `type` ist laut Backend-Validierung **erforderlich**. Alle Tests, die Cups anlegen, müssen vor dem Speichern einen Typ auswählen:

```typescript
// Select cup type (required field)
await page.locator('#type').click()
await page.waitForSelector('[role="option"]', { state: 'visible' })
await page.getByRole('option').first().click()
```

**Firefox-Dropdown-Korrektur:** Tests, die mit Mehrfachauswahl-Dropdowns (Events) arbeiten, warten nach dem Drücken von Escape 300 ms, damit sich das Dropdown sicher schließt. Das ist vor allem für Firefox wichtig:

```typescript
// Close dropdown
await page.keyboard.press('Escape')
// Wait for dropdown to close (especially important in Firefox)
await page.waitForTimeout(300)
```

---

## 📖 Tests ausführen

### Grundlegende Befehle

```bash
# Alle Tests
pnpm test:e2e

# Bestimmte Datei
pnpm playwright test event-form.spec.ts
pnpm playwright test cup-form.spec.ts

# Bestimmter Test
pnpm playwright test event-form.spec.ts -g "should create event"
pnpm playwright test cup-form.spec.ts -g "should create cup"

# Bestimmter Browser
pnpm playwright test --project=chromium
pnpm playwright test --project=firefox
pnpm playwright test --project=msedge

# Nur Cup-Tests ausführen
pnpm playwright test cup
```

### Debuggen und Untersuchen

```bash
# Headed-Modus (Browser sichtbar)
pnpm playwright test event-form.spec.ts --headed
pnpm playwright test cup-form.spec.ts --headed

# Debug-Modus (schrittweise)
pnpm playwright test event-form.spec.ts --debug
pnpm playwright test cup-form.spec.ts --debug

# HTML-Bericht anzeigen
pnpm playwright show-report

# Trace für fehlgeschlagenen Test anzeigen
pnpm playwright show-trace test-results/*/trace.zip
```

---

## ✅ Best Practices

### Empfohlen ✅

- **Datenbankisolation verwenden** für Tests, die Daten erstellen oder ändern
- Semantische Selektoren verwenden: `getByRole`, `getByLabel`, `getByText`
- Mit Assertions warten: `await expect(locator).toBeVisible()`
- Das passende Muster wählen: pro Test für Erstellen, Suite-Ebene für Bearbeiten, keines für reine Lesetests
- Eindeutige Testdatennamen verwenden: `Test Event ${Date.now()}`

### Zu vermeiden ❌

- **Keine manuelle Bereinigung schreiben** - die Isolation übernimmt das
- Keine CSS-Selektoren verwenden, wenn semantische verfügbar sind
- Keine festen Wartezeiten verwenden: `page.waitForTimeout(5000)`
- Sich nicht auf die Ausführungsreihenfolge der Tests verlassen
- Nicht vergessen, bei Tests zum Erstellen/Ändern das Cookie zu setzen

---

## 🐛 Fehlerbehebung

### Schnellprüfungen

```bash
# 1. Servers running?
curl http://localhost:5173           # Frontend
curl http://localhost:8080/actuator/health  # Backend

# 2. Correct profile?
curl http://localhost:8080/actuator/env | grep activeProfiles
# Should show: "e2e-frontend-tests"

# 3. Docker running?
docker ps

# 4. Auth setup works?
pnpm playwright test auth-setup.ts --headed
```

### Häufige Probleme

| Problem | Schnelle Lösung |
|-------|-----------|
| **Datenbankerstellung schlägt fehl** | Prüfen, ob das Backend mit dem Profil `e2e-frontend-tests` läuft |
| **Tests laufen in Timeout** | Timeout erhöhen: `createTestDatabase({ timeoutMs: 300000 })` |
| **Falsche Datenbank wird verwendet** | Sicherstellen, dass das Cookie VOR `page.goto()` gesetzt wird |
| **Authentifizierung schlägt fehl** | `e2e/.auth/storageState.json` löschen und erneut versuchen |
| **Element nicht gefunden** | Mit `--headed` ausführen, um zu sehen, was passiert |

**Ausführliche Fehlerbehebung siehe:** [TROUBLESHOOTING.md](./TROUBLESHOOTING.de.md)

---

## 📊 Beispiel für eine Teststruktur

```typescript
import { test, expect } from '@playwright/test'
import { createTestDatabase } from './helpers/database'

test.describe('My Feature', () => {
    test.beforeEach(async ({ page }) => {
        // 1. Create isolated database
        const dbId = await createTestDatabase()

        // 2. Set routing cookie
        await page.context().addCookies([{
            name: 'X-DB-Identifier',
            value: dbId,
            domain: 'localhost',
            path: '/',
            httpOnly: false,
            secure: false,
            sameSite: 'Lax',
        }])

        // 3. Navigate to page
        await page.goto('/en/my-page')
    })

    test('should do something', async ({ page }) => {
        // 4. Interact with page
        await page.fill('[name="field"]', 'value')
        await page.click('button:has-text("Save")')

        // 5. Verify results
        await expect(page).toHaveURL(/\/success$/)

        // 6. No cleanup needed!
    })
})
```

---

## 🛠️ Debugging-Werkzeuge

### Im Testcode

```typescript
// Pause execution (opens Inspector)
await page.pause()

// Take screenshot
await page.screenshot({ path: 'debug.png' })

// Log console messages
page.on('console', msg => console.log('PAGE:', msg.text()))

// Log network requests
page.on('request', req => console.log('>>', req.method(), req.url()))
page.on('response', res => console.log('<<', res.status(), res.url()))

// Slow down execution
test.use({ slowMo: 500 }) // 500ms delay between actions
```

### Trace Viewer

Nach einem fehlgeschlagenen Test:
```bash
pnpm playwright show-report
# Click on failed test → Trace shows automatically
```

Funktionen:
- Jeden Aktionsschritt ansehen
- Screenshots zu jedem Schritt
- Zeitleiste der Netzwerkanfragen
- Konsolenausgaben
- Quellcode

---

## 🔧 Konfiguration

### Backend

**Datei:** `backend/src/main/resources/application-e2e-frontend-tests.properties`

```properties
# Server
server.port=8080

# Security
security.createdatabase.api-token=${CREATEDATABASE_API_TOKEN}

# OAuth2
spring.security.oauth2.resourceserver.jwt.issuer-uri=${API_OAUTH2_RESOURCE_SERVER_JWT_ISSUER_URI}
# ... (see file for complete config)
```

**Startskript:** `backend/start-e2e-frontend-tests.sh`
- Lädt Variablen aus `.env`
- Prüft die erforderlichen Variablen
- Setzt `SPRING_PROFILES_ACTIVE=e2e-frontend-tests`
- Startet das Backend

### Frontend

**Datei:** `e2e/.env.local`

```env
HOSTNAME=localhost
BACKEND_PROTOCOL=http
BACKEND_PORT=8080
BACKEND_PROFILES=e2e-frontend-tests
FRONTEND_PROTOCOL=http
PORT=5173
VITE_MODE=development
USERNAME=test-user
PASSWORD=test-password
CREATEDATABASE_API_TOKEN=your-token-here
```

---

## ⚙️ CI/CD-Integration

Die Tests sind für CI konfiguriert mit:
- Automatischem Serverstart
- Wiederholung bei Fehlschlag (2 Wiederholungen)
- Paralleler Ausführung (4 Worker)
- Erzeugung eines HTML-Berichts
- Trace bei Fehlschlag

**Beispiel für GitHub Actions:**
```yaml
env:
  USERNAME: ${{ secrets.TEST_USERNAME }}
  PASSWORD: ${{ secrets.TEST_PASSWORD }}
  CREATEDATABASE_API_TOKEN: ${{ secrets.CREATEDATABASE_API_TOKEN }}

steps:
  - name: Run E2E Tests
    run: pnpm test:e2e
```

---

## 🔍 Bekannte Probleme

### Instabile Firefox-Tests bei paralleler Ausführung

**Status:** Bekanntes Problem - nicht kritisch

**Problem:** 2-3 Tests laufen gelegentlich in einen Timeout, wenn alle Browser parallel laufen

**Symptome:**
- Tests bestehen, wenn sie einzeln oder nur mit Firefox laufen
- Timeouts treten bei `page.goto()` oder beim Rendern des Formulars auf
- Ressourcenkonkurrenz bei paralleler Ausführung

**Ursache:** Konkurrenz um Browserressourcen bei 4 Workern und 3 Browsern

**Workaround:**
```bash
# Run browsers sequentially
pnpm playwright test --workers=1

# Or run Firefox separately
pnpm playwright test --project=firefox
```

**Angewendete Korrektur:** 300 ms Wartezeit nach dem Schließen des Dropdowns für mehr Stabilität in Firefox

### Webkit-Browsertests

**Status:** Aus der Test-Suite ausgeschlossen

**Problem:** 15/40 Tests schlagen nur im Webkit-Browser fehl

**Symptome:**
- Die Datenbankisolation funktioniert (Datenbank wird erfolgreich erstellt)
- DOM-Elemente werden während der Ausführung abgelöst
- Unerwartete Navigation zum Wurzelpfad
- Fehler: "element was detached from the DOM"

**Ursache:** Webkit-spezifisches Browserverhalten (NICHT die Datenbankisolation)

**Aktuelles Vorgehen:** Tests laufen nur auf Chromium, Firefox und Edge

**Workaround:**
```bash
# Explicitly exclude webkit
pnpm playwright test --project=chromium --project=firefox --project=msedge
```

### Datenbankisolation - ✅ GELÖST

**Lösung:** `DynamicRoutingDataSource` für anfragebasiertes Routing implementiert

**Ergebnisse:**
- ✅ Chromium: 40/40 bestanden
- ✅ Firefox: 38-40/40 bestanden (2 instabile Tests bei parallelen Läufen)
- ✅ Edge: 40/40 bestanden
- ⚠️ Webkit: Ausgeschlossen (nicht zusammenhängende Browserprobleme)

---

## 📚 Weitere Ressourcen

### Dokumentation
- **[E2E_DATABASE_ISOLATION.md](./E2E_DATABASE_ISOLATION.de.md)** - Vollständige technische Anleitung (Architektur, Implementierung, Performance)
- **[TROUBLESHOOTING.md](./TROUBLESHOOTING.de.md)** - Ausführliche Fehlerbehebung für alle häufigen Probleme

### Externe Ressourcen
- [Playwright-Dokumentation](https://playwright.dev) - Offizielle Playwright-Dokumentation
- [PrimeVue-Dokumentation](https://primevue.org) - In den Tests verwendete UI-Komponenten
- [Testcontainers-Dokumentation](https://www.testcontainers.org/) - Datenbank-Container

---

## 📞 Hilfe erhalten

**Bevor du um Hilfe bittest:**

1. ✅ [TROUBLESHOOTING.md](./TROUBLESHOOTING.de.md) prüfen
2. ✅ Schnelldiagnose ausführen (siehe Abschnitt Fehlerbehebung)
3. ✅ [E2E_DATABASE_ISOLATION.md](./E2E_DATABASE_ISOLATION.de.md) lesen, falls die Datenbank betroffen ist

**Wenn du um Hilfe bittest, gib an:**
- Testdatei und konkreten Testnamen
- Fehlermeldung und Stacktrace
- Screenshots/Videos aus den Testergebnissen
- Backend- und Frontend-Logs
- Umgebungsinformationen (Betriebssystem, Node-Version, Docker-Version)

**HTML-Bericht erzeugen:**
```bash
pnpm playwright show-report
```

---

## 🚧 Wartung

### Neue Tests hinzufügen

1. Isolationsmuster wählen (pro Test, Suite-Ebene oder keines)
2. Der bestehenden Teststruktur folgen (siehe Beispiel für eine Teststruktur)
3. Semantische Selektoren verwenden (`getByRole`, `getByLabel`)
4. Passende Assertions ergänzen
5. Lokal im `--headed`-Modus testen
6. Diese README aktualisieren, wenn neue Testdateien hinzukommen

### Selektoren aktualisieren

Wenn sich PrimeVue-Komponenten ändern:
1. Test mit `--debug` ausführen
2. Mit dem Playwright Inspector neue Selektoren finden
3. Semantische Selektoren gegenüber CSS bevorzugen
4. `data-testid`-Attribute für mehr Stabilität in Betracht ziehen

---

*Zuletzt aktualisiert: 2025-12-28*
*Version: 2.1.0 - Cup-E2E-Tests hinzugefügt (18 Tests)*
