# Leitfaden zur Fehlerbehebung bei E2E-Tests

> **Deutsch** · [English](TROUBLESHOOTING.md)

Ausführliche Lösungen für häufige Probleme der E2E-Tests mit Datenbank-Isolation.

## 🔍 Schnelldiagnose

```bash
# 1. Prüfen, ob die Server laufen
curl http://localhost:5173           # Frontend
curl http://localhost:8080/actuator/health  # Backend

# 2. Keycloak-OAuth2-Endpunkt prüfen
curl -L http://localhost:8080/oauth2/authorization/keycloak

# 3. Auth-Setup mit Debugging testen
pnpm playwright test auth-setup.ts --headed

# 4. Vollständige Logs ansehen
pnpm playwright test auth-setup.ts --debug

# 5. Backend-Profil prüfen
curl http://localhost:8080/actuator/env | grep -i activeProfiles
# Sollte anzeigen: "e2e-frontend-tests"
```

## 📋 Index häufiger Probleme

### Probleme bei der Authentifizierung
- ["Timeout 10000ms exceeded" beim Auth-Setup](#-timeout-10000ms-exceeded-beim-auth-setup)
- [Falsche Zugangsdaten / Ungültiger Benutzername oder ungültiges Passwort](#-falsche-zugangsdaten--authentifizierung-schlägt-fehl)
- [Sitzung während der Tests abgelaufen](#-sitzung-während-der-tests-abgelaufen)

### Probleme bei der Datenbank-Isolation
- [Datenbankerstellung schlägt fehl](#-datenbankerstellung-schlägt-fehl)
- [Falsche Datenbank verwendet (Daten anderer Tests sichtbar)](#-falsche-datenbank-verwendet)
- [Datenbank nicht aufgeräumt (Speicherprobleme)](#-datenbank-nicht-aufgeräumt)
- [Liquibase-Migration schlägt fehl](#-liquibase-migration-schlägt-fehl)

### Probleme mit Server und Konfiguration
- [Backend/Frontend nicht erreichbar](#-backendfrontend-nicht-erreichbar)
- [e2e/.env.local nicht gefunden](#-e2eenvlocal-nicht-gefunden)
- [Probleme mit dem API-Token (403/401)](#-probleme-mit-dem-api-token)

### Probleme bei der Testausführung
- [Tests laufen, schlagen aber fehl](#-tests-laufen-schlagen-aber-fehl)
- [Tests laufen lokal, schlagen aber in der CI fehl](#-tests-laufen-lokal-schlagen-aber-in-der-ci-fehl)
- [Performance-Probleme / Tests langsam](#-performance-probleme--tests-langsam)
- [Fehler wegen nicht gefundener Elemente](#-fehler-wegen-nicht-gefundener-elemente)

---

## 🔒 Probleme bei der Authentifizierung

### ❌ "Timeout 10000ms exceeded" beim Auth-Setup

**Fehler:**
```
TimeoutError: page.waitForURL: Timeout 10000ms exceeded.
waiting for navigation to "**/auth/**" until "load"
```

**Mögliche Ursachen und Lösungen:**

#### 1. Keycloak ist nicht erreichbar

**Prüfen:**
```bash
# OAuth2-Ablauf manuell testen
curl -L http://localhost:8080/oauth2/authorization/keycloak
```

**Erwartete Antwort:** Weiterleitung zur Keycloak-Anmeldeseite

**Falls nicht:** Das Backend läuft nicht oder OAuth2 ist nicht konfiguriert

**Lösung:**
```bash
# Backend-Logs prüfen
cd ../backend
./mvnw spring-boot:run

# In den Logs nach Fehlern suchen:
# - "OAuth2ClientRegistrationRepository"
# - "Keycloak"
# - "redirect-uri"
```

#### 2. Keycloak-URL passt nicht

**Das Skript probiert automatisch mehrere URL-Muster aus:**
- `**/realms/**/protocol/openid-connect/auth**`
- `**/auth/realms/**/protocol/openid-connect/auth**`
- `**/openid-connect/auth**`

**Wenn alle fehlschlagen:** In den Logs prüfen, welche URL tatsächlich verwendet wurde:

```bash
pnpm playwright test auth-setup.ts --headed
# Nach "Current URL after OAuth2 redirect: ..." suchen
```

**Lösung:** URL-Muster in `auth-setup.ts` anpassen

#### 3. Falsches Backend-Profil

**In `e2e/.env.local` prüfen:**
```env
BACKEND_PROFILES=dev  # NICHT "e2e-frontend-tests" für lokale Auth-Tests!
```

**Warum?** `e2e-frontend-tests` startet isolierte Container, nicht das lokale Keycloak

**Lösung:**
```bash
# e2e/.env.local anpassen
BACKEND_PROFILES=dev

# Backend neu starten
cd ../backend
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

#### 4. Sitzung existiert bereits

**Das Skript überspringt die Authentifizierung, wenn die Sitzung jünger als 10 Minuten ist**

**Lösung:**
```bash
# Sitzung löschen und neu authentifizieren
rm e2e/.auth/storageState.json
pnpm playwright test auth-setup.ts
```

---

### ❌ Falsche Zugangsdaten / Authentifizierung schlägt fehl

**Fehler:**
```
Error: locator.fill: Target closed
```
oder Keycloak zeigt "Invalid username or password"

**Lösung:**

**1. Zugangsdaten in Keycloak prüfen:**

```bash
# Keycloak-Admin-Konsole öffnen
open https://keycloak.jobst-server.de/admin

# Navigieren zu:
# Realm: resulter
# Users → Nach deinem USERNAME suchen
# Prüfen:
# - Benutzer existiert
# - Benutzer ist "Enabled"
# - E-Mail ist verifiziert (falls erforderlich)
```

**2. Zugangsdaten in .env.local prüfen:**

```bash
# Aktuelle Zugangsdaten anzeigen (ACHTUNG: Passwort ist sichtbar!)
cat e2e/.env.local | grep USERNAME
cat e2e/.env.local | grep PASSWORD

# Anmeldung manuell im Browser testen:
# 1. http://localhost:8080/oauth2/authorization/keycloak öffnen
# 2. USERNAME und PASSWORD eingeben
# 3. Sollte zu http://localhost:5173 weiterleiten
```

**3. Benutzerrollen prüfen:**

Manche Tests benötigen die Rolle ADMIN:

```bash
# In der Keycloak-Admin-Konsole:
# Realm: resulter
# Users → Dein Benutzer → Role Mappings
# Hinzufügen: "ADMIN" oder "admin"
```

---

### ❌ Sitzung während der Tests abgelaufen

**Fehler:** 401 Unauthorized oder Weiterleitung zur Anmeldung während eines Tests

**Ursache:** Das Sitzungs-Cookie ist abgelaufen (Standard: 30 Minuten)

**Lösungen:**

**1. Sitzungs-Timeout erhöhen (Backend):**

```yaml
# application.yml
server:
  servlet:
    session:
      timeout: 60m  # Von 30m auf 60m erhöhen
```

**2. Vor jedem Testlauf neu authentifizieren:**

```bash
# Alte Sitzung löschen
rm e2e/.auth/storageState.json

# Tests ausführen (auth-setup läuft automatisch)
pnpm test:e2e
```

**3. Schnellere Tests schreiben:**
- `page.waitForTimeout()` reduzieren
- `expect().toBeVisible()` statt fester Wartezeiten verwenden

---

## 💾 Probleme bei der Datenbank-Isolation

### ❌ Datenbankerstellung schlägt fehl

**Fehler:**
```
Failed to create test database: Connection refused
```
oder
```
Failed to create test database: 500 Internal Server Error
```

**Ursachen und Lösungen:**

#### 1. Backend läuft nicht

**Prüfen:**
```bash
curl http://localhost:8080/actuator/health
```

**Lösung:**
```bash
cd backend
./start-e2e-frontend-tests.sh
```

#### 2. Falsches Backend-Profil

**Aktives Profil prüfen:**
```bash
curl http://localhost:8080/actuator/env | grep -i activeProfiles
# Sollte anzeigen: "e2e-frontend-tests"
```

**Lösung:**
```bash
# Mit dem richtigen Profil neu starten
cd backend
export SPRING_PROFILES_ACTIVE=e2e-frontend-tests
./mvnw spring-boot:run
```

#### 3. Docker läuft nicht

**Fehler:** `Could not start PostgreSQL container`

**Prüfen:**
```bash
docker ps
# Sollte laufende Container anzeigen
```

**Lösung:**
```bash
# Docker Desktop starten
open -a Docker

# Warten, bis Docker gestartet ist
docker ps
```

#### 4. API-Token fehlt

**Fehler:** `403 Forbidden` oder `401 Unauthorized`

**Prüfen:**
```bash
# .env-Datei (Projektwurzel)
grep CREATEDATABASE_API_TOKEN .env

# e2e/.env.local
grep CREATEDATABASE_API_TOKEN e2e/.env.local
```

**Lösung:**
```bash
# Token generieren
openssl rand -base64 32

# Zu .env und e2e/.env.local hinzufügen
CREATEDATABASE_API_TOKEN=<generated-token>

# Backend neu starten
cd backend
./start-e2e-frontend-tests.sh
```

---

### ❌ Falsche Datenbank verwendet

**Fehler:** Der Test sieht Daten anderer Tests oder der Standarddatenbank

**Ursache:** Das Cookie ist nicht oder falsch gesetzt

**Lösung:**

**1. Prüfen, ob das Cookie VOR der Navigation gesetzt wird:**

```typescript
// ✅ RICHTIG: Zuerst das Cookie setzen
await page.context().addCookies([{
    name: 'X-DB-Identifier',
    value: dbId,
    domain: 'localhost', // Must be 'localhost', not '127.0.0.1'
    path: '/',
    httpOnly: false,
    secure: false,
    sameSite: 'Lax',
}])

await page.goto('/event') // Cookie sent automatically

// ❌ FALSCH: Zuerst navigieren
await page.goto('/event') // Uses default database!
await page.context().addCookies([...]) // Too late
```

**2. Debug-Logging hinzufügen:**

```typescript
test('debug cookie', async ({ page }) => {
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

    // Verify cookie was set
    const cookies = await page.context().cookies()
    console.log('Cookies:', cookies.filter(c => c.name === 'X-DB-Identifier'))

    await page.goto('/event')
})
```

**3. Backend-Logs prüfen:**

```bash
# Die Backend-Logs sollten zeigen:
# DataSourceInterceptor - X-DB-Identifier header: <uuid>
# DataSourceInterceptor - Routing to database: <uuid>
# DataSourceManager - DataSource found and set for: <uuid>

# Falls nicht, wird das Cookie nicht korrekt gesendet oder gelesen
```

---

### ❌ Datenbank nicht aufgeräumt

**Fehler:** Zu viele Datenbanken, Speicherprobleme, Leistungseinbußen

**Ursache:** Der Cleanup-Scheduler läuft nicht oder Datenbanken sind noch aktiv

**Lösungen:**

**1. Prüfen, ob der Cleanup-Scheduler läuft:**

```bash
# Die Backend-Logs sollten alle 10 Sekunden zeigen:
# DataSourceCleanupScheduler - Checking for inactive databases...
# DataSourceCleanupScheduler - Cleaning up inactive database: <uuid>
# DataSourceManager - Database dropped: test-db-<uuid>
```

**2. Prüfen, ob das Profil aktiv ist:**

```bash
curl http://localhost:8080/actuator/env | grep "e2e-frontend-tests"
# Sollte zurückgeben: "e2e-frontend-tests"
```

**3. Prüfen, ob der Scheduler aktiviert ist:**

```bash
curl http://localhost:8080/actuator/scheduledtasks
# Sollte anzeigen: DataSourceCleanupScheduler.cleanupInactiveDatabases
```

**4. Auf das automatische Aufräumen warten:**

Das Aufräumen geschieht automatisch:
- Der letzte Datenbankzugriff wird erfasst
- 30 Sekunden warten
- Der Scheduler läuft alle 10 Sekunden
- Erkennt Inaktivität von mehr als 30 Sekunden
- Schließt die Verbindungen und löscht die Datenbank

**5. Manuelles Aufräumen (falls nötig):**

```bash
# Backend neu starten, um alle Testdatenbanken zu bereinigen
cd backend
./start-e2e-frontend-tests.sh
```

---

### ❌ Liquibase-Migration schlägt fehl

**Fehler:**
```
Failed to run Liquibase migrations
```
oder
```
Database migration failed for test-db-<uuid>
```

**Ursachen und Lösungen:**

**1. Liquibase-Changelog prüfen:**

```bash
# Master-Changelog ansehen
cat backend/src/main/resources/db/changelog/db.changelog-master.yaml

# Migrationen auf der Standarddatenbank testen
cd backend
./mvnw liquibase:update -Dspring.profiles.active=e2e-frontend-tests
```

**2. Auf Migrationskonflikte prüfen:**

```bash
cd backend
./mvnw liquibase:status -Dspring.profiles.active=e2e-frontend-tests
```

**3. Liquibase-Sperren aufheben:**

```sql
-- Falls Migrationen mit gesperrtem Status hängen
-- Mit der Datenbank verbinden und ausführen:
UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE;
```

**4. Migrations-Logs prüfen:**

```bash
# Die Backend-Logs sollten zeigen:
# DataSourceManager - Running Liquibase migrations for: test-db-<uuid>
# Liquibase - Successfully acquired change log lock
# Liquibase - Reading from PUBLIC.DATABASECHANGELOG
# Liquibase - Successfully released change log lock
```

---

### ❌ Probleme mit dem API-Token

**Fehler:**
```
403 Forbidden
```
oder
```
401 Unauthorized
```
beim Aufruf von `/createDatabase`

**Ursachen und Lösungen:**

**1. Token nicht konfiguriert:**

**Prüfen:**
```bash
# Backend .env
grep CREATEDATABASE_API_TOKEN .env

# Frontend e2e/.env.local
grep CREATEDATABASE_API_TOKEN e2e/.env.local
```

**Lösung:**
```bash
# Neues Token generieren
openssl rand -base64 32

# In .env setzen (Projektwurzel)
CREATEDATABASE_API_TOKEN=<generated-token>

# In e2e/.env.local setzen (dasselbe Token)
CREATEDATABASE_API_TOKEN=<generated-token>

# Backend neu starten
cd backend
./start-e2e-frontend-tests.sh
```

**2. Token stimmt nicht überein:**

**Prüfen, ob die Tokens übereinstimmen:**
```bash
# Backend-Token
grep CREATEDATABASE_API_TOKEN .env

# Frontend-Token
grep CREATEDATABASE_API_TOKEN frontend/e2e/.env.local

# Sie müssen identisch sein!
```

**3. Token manuell testen:**

```bash
# Token aus .env lesen
TOKEN=$(grep CREATEDATABASE_API_TOKEN .env | cut -d '=' -f2)

# Endpunkt testen
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/createDatabase

# Sollte zurückgeben:
# {"success":true,"data":{"identifier":"<uuid>"}}
```

---

## 🖥️ Probleme mit Server und Konfiguration

### ❌ Backend/Frontend nicht erreichbar

**Fehler:**
```
✅ Frontend server is running on http://localhost:5173
❌ Backend server is not running on http://localhost:8080
```

**Lösungen:**

#### Backend läuft nicht

```bash
# Prüfen, ob der Port belegt ist
lsof -i :8080

# Backend starten
cd backend
./start-e2e-frontend-tests.sh

# Warten auf:
# "Started ResulterApplication in X.XXX seconds"
```

#### Frontend läuft nicht

```bash
# Prüfen, ob der Port belegt ist
lsof -i :5173

# Frontend starten
pnpm dev

# Warten auf:
# "VITE vX.X.X ready in XXms"
```

#### Port bereits belegt

```bash
# Prozess finden, der den Port verwendet
lsof -i :5173
lsof -i :8080

# Prozess beenden
kill -9 <PID>

# Server neu starten
pnpm dev  # Frontend
cd backend && ./start-e2e-frontend-tests.sh  # Backend
```

---

### ❌ e2e/.env.local nicht gefunden

**Fehler:**
```
❌ e2e/.env.local not found
```

**Lösung:**

`e2e/.env.local` anlegen:

```bash
cat > e2e/.env.local << 'EOF'
# Backend Configuration
HOSTNAME=localhost
BACKEND_PROTOCOL=http
BACKEND_PORT=8080
BACKEND_PROFILES=e2e-frontend-tests

# Frontend Configuration
FRONTEND_PROTOCOL=http
PORT=5173
VITE_MODE=development

# Test Credentials
USERNAME=your-username
PASSWORD=your-password

# API Tokens
CREATEDATABASE_API_TOKEN=your-token-here
EOF
```

**Wichtig:**
- `USERNAME` und `PASSWORD` müssen in Keycloak existieren
- Realm: `resulter`
- Client: `resulter-backend` (für BFF)
- Das Token muss mit der `.env`-Datei des Backends übereinstimmen

---

## 🐛 Probleme bei der Testausführung

### ❌ Fehler wegen nicht gefundener Elemente

**Fehler:**
```
Error: locator.fill: Target page, context or browser has been closed
```
oder
```
Error: locator.click: Element not found
```

**Lösungen:**

**1. Selektoren prüfen:**

```bash
# Test im Headed-Modus ausführen
pnpm playwright test event-form.spec.ts --headed

# Playwright Inspector verwenden
pnpm playwright test event-form.spec.ts --debug

# Im Inspector: Mit der Maus über Elemente fahren, um Selektoren zu sehen
```

**2. Auf Elemente warten:**

```typescript
// ❌ SCHLECHT: Element ist möglicherweise noch nicht bereit
await page.getByLabel('Name').fill('Test')

// ✅ GUT: Auf das Element warten
await page.getByLabel('Name').waitFor()
await page.getByLabel('Name').fill('Test')

// ✅ BESSER: expect verwenden
await expect(page.getByLabel('Name')).toBeVisible()
await page.getByLabel('Name').fill('Test')
```

**3. Timing der Elemente prüfen:**

```typescript
// Add debug logging
page.on('load', () => console.log('Page loaded'))
page.on('domcontentloaded', () => console.log('DOM ready'))

await page.goto('/event')
console.log('Navigation complete')

await page.screenshot({ path: 'debug.png' })
```

---

### ❌ Tests laufen, schlagen aber fehl

**Fehler:** Die Tests starten, aber fachliche Assertions schlagen fehl

**Debug-Strategien:**

#### 1. Headed-Modus

```bash
# Im Browser sehen, was passiert
pnpm playwright test event-form.spec.ts --headed

# Oder einen bestimmten Test
pnpm playwright test event-form.spec.ts --headed -g "should create event"
```

#### 2. Debug-Modus (schrittweise)

```bash
# Der Playwright Inspector öffnet sich
pnpm playwright test event-form.spec.ts --debug

# Oder einen bestimmten Test
pnpm playwright test event-form.spec.ts --debug -g "should create event"

# Im Inspector:
# - Schrittweise ausführen: F10
# - Fortsetzen: F8
# - Screenshot: Kamera-Symbol
```

#### 3. Screenshots und Videos

**In playwright.config.ts:**
```typescript
use: {
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
}
```

**Dann:**
```bash
pnpm test:e2e

# Screenshots von Fehlern in:
ls test-results/*/screenshots/

# Videos von Fehlern in:
ls test-results/*/videos/
```

#### 4. Netzwerk-Logs

**Im Test:**
```typescript
page.on('request', request =>
    console.log('>>', request.method(), request.url())
)
page.on('response', response =>
    console.log('<<', response.status(), response.url())
)
```

#### 5. Konsolen-Logs

**Im Test:**
```typescript
page.on('console', msg => console.log('PAGE LOG:', msg.text()))
```

---

### ❌ Tests laufen lokal, schlagen aber in der CI fehl

**Fehler:** Die Tests laufen auf dem lokalen Rechner durch ✅, schlagen aber in GitHub Actions fehl ❌

**Lösungen:**

#### 1. Umgebungsvariablen

**`.github/workflows/*.yml` prüfen:**
```yaml
env:
  USERNAME: ${{ secrets.TEST_USERNAME }}
  PASSWORD: ${{ secrets.TEST_PASSWORD }}
  CREATEDATABASE_API_TOKEN: ${{ secrets.CREATEDATABASE_API_TOKEN }}
```

**Sicherstellen:**
- Die Secrets sind in den GitHub-Repository-Einstellungen definiert
- Die Namen stimmen exakt überein (Groß- und Kleinschreibung beachten!)
- Alle erforderlichen Variablen sind gesetzt

#### 2. Serverstart in der CI

**playwright.config.ts hat einen `webServer` für die CI:**
```typescript
webServer: process.env.CI ? [...] : undefined
```

**Sollte funktionieren, aber:**
- Timeout erhöhen, wenn der Server langsam startet
- CI-Logs auf Fehler beim Serverstart prüfen
- Prüfen, ob Docker in der CI verfügbar ist

#### 3. Headless vs. Headed

Die CI läuft immer headless. Lokal ebenfalls headless testen:

```bash
pnpm test:e2e  # Headless wie in der CI
```

#### 4. Timing-Probleme

Die CI kann langsamer sein als der lokale Rechner:

```typescript
// Increase timeouts for CI
test.setTimeout(process.env.CI ? 60000 : 30000)
```

---

### ❌ Performance-Probleme / Tests langsam

**Fehler:** Die Tests brauchen mehr als 10 Minuten, die Datenbankerstellung ist langsam

**Ursachen und Lösungen:**

#### 1. Docker optimieren

```bash
# Docker-Ressourcen erhöhen
# Docker Desktop → Settings → Resources
# - CPUs: 4+
# - Memory: 8GB+
# - Swap: 2GB+
```

#### 2. Gemeinsame Nutzung auf Suite-Ebene verwenden

```typescript
// ❌ SCHLECHT: 7 Datenbankerstellungen (35 Sekunden Overhead)
test.beforeEach(async ({ page }) => {
    const dbId = await createTestDatabase()
})

// ✅ GUT: 1 Datenbankerstellung (5 Sekunden Overhead)
test.beforeAll(async ({ browser }) => {
    sharedDbId = await createTestDatabase()
})
```

#### 3. Komplexität der Migration reduzieren

```xml
<!-- Skip heavy seed data in test databases -->
<changeSet id="seed-data" context="!test">
    <sqlFile path="seed-data.sql"/>
</changeSet>
```

#### 4. Parallele Ausführung aktivieren

```typescript
// playwright.config.ts
export default defineConfig({
    workers: 4, // Safe with database isolation
    fullyParallel: true,
})
```

---

## 🛠️ Erweiterte Debugging-Werkzeuge

### Playwright Trace Viewer

**Nach einem Testfehler:**
```bash
# HTML-Report öffnen
pnpm playwright show-report

# Auf den fehlgeschlagenen Test klicken
# Der Trace wird automatisch angezeigt

# Oder manuell:
pnpm playwright show-trace test-results/*/trace.zip
```

**Im Trace Viewer:**
- Jeden einzelnen Aktionsschritt sehen
- Screenshots zu jedem Schritt sehen
- Netzwerkanfragen sehen
- Konsolen-Logs sehen
- Quellcode sehen

### Entwicklerwerkzeuge des Browsers

**Im Headed-Modus:**
1. Test mit `--headed` starten
2. Rechtsklick → Untersuchen
3. Die Entwicklerwerkzeuge öffnen sich
4. Wie in der normalen Webentwicklung verwenden

### Pause im Test

**Im Test:**
```typescript
await page.pause()  // Playwright Inspector opens
```

**Dann:**
- Seite untersuchen
- Befehle manuell ausführen
- Fortsetzen, wenn du bereit bist

---

## 📞 Hilfe erhalten

Wenn nichts funktioniert:

**1. Logs sammeln:**
```bash
# Backend-Logs
cd backend && ./mvnw spring-boot:run > backend.log 2>&1

# Frontend-Logs
pnpm dev > frontend.log 2>&1

# E2E-Logs
pnpm test:e2e > e2e.log 2>&1
```

**2. Report erstellen:**
```bash
pnpm playwright show-report
# Der Report öffnet sich automatisch
```

**3. Kontext dokumentieren:**
- Welcher Test schlägt fehl?
- Fehlermeldung
- Screenshots/Videos
- Logs
- Umgebung (Betriebssystem, Node-Version, Docker-Version)

**4. Issue erstellen:**
- GitHub-Issue mit allen Informationen
- Oder Team-Chat mit Logs

---

## 📚 Weitere Ressourcen

- [Dokumentation zur E2E-Datenbank-Isolation](./E2E_DATABASE_ISOLATION.de.md) - Vollständiger technischer Leitfaden
- [README.md](./README.de.md) - Überblick über die E2E-Tests und Schnellstart
- [Playwright-Dokumentation](https://playwright.dev/docs/debug)
- [Docker-Dokumentation](https://docs.docker.com/)

---

*Zuletzt aktualisiert: 2025-12-28*
*Version: 2.0.0*
