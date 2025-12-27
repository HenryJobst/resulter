# E2E Tests Troubleshooting Guide

Detaillierte Lösungen für häufige Probleme bei E2E-Tests.

## 🔍 Schnelldiagnose

```bash
# 1. Prüfe ob Server laufen
curl http://localhost:5173           # Frontend
curl http://localhost:8080/actuator/health  # Backend

# 2. Prüfe Keycloak OAuth2 Endpoint
curl -L http://localhost:8080/oauth2/authorization/keycloak

# 3. Teste Auth-Setup mit Debugging
pnpm test:e2e:auth:headed

# 4. Sehe komplette Logs
pnpm test:e2e:auth --debug
```

## ❌ "Timeout 10000ms exceeded" bei Auth-Setup

### Problem
```
TimeoutError: page.waitForURL: Timeout 10000ms exceeded.
waiting for navigation to "**/auth/**" until "load"
```

### Mögliche Ursachen & Lösungen

#### 1. Keycloak ist nicht erreichbar

**Prüfen:**
```bash
# Manuell OAuth2 Flow testen
curl -L http://localhost:8080/oauth2/authorization/keycloak
```

**Erwartete Antwort:** Redirect zu Keycloak Login-Seite

**Wenn nicht:** Backend läuft nicht oder OAuth2 ist nicht konfiguriert

**Lösung:**
```bash
# Backend logs prüfen
cd ../backend
./mvnw spring-boot:run

# In Logs nach Fehlern suchen:
# - "OAuth2ClientRegistrationRepository"
# - "Keycloak"
# - "redirect-uri"
```

#### 2. Keycloak URL stimmt nicht

**Das Script versucht jetzt automatisch mehrere URL-Muster:**
- `**/realms/**/protocol/openid-connect/auth**`
- `**/auth/realms/**/protocol/openid-connect/auth**`
- `**/openid-connect/auth**`

**Wenn alle fehlschlagen:** Sehe in den Logs welche URL tatsächlich verwendet wird:

```bash
pnpm test:e2e:auth:headed
# Sehe "Current URL after OAuth2 redirect: ..."
```

**Lösung:** URL-Pattern in `auth-setup.ts` anpassen

#### 3. Backend Profile falsch

**Prüfen in `e2e/.env.local`:**
```env
BACKEND_PROFILES=dev  # NICHT "testcontainers" für lokale Tests!
```

**Warum?** `testcontainers` startet isolierte Container, nicht das lokale Keycloak

**Lösung:**
```bash
# e2e/.env.local anpassen
BACKEND_PROFILES=dev

# Backend neu starten
cd ../backend
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

#### 4. Session bereits vorhanden

**Das Script überspringt Auth wenn Session < 10 Min alt**

**Lösung:**
```bash
# Session löschen und neu authentifizieren
rm e2e/.auth/storageState.json
pnpm test:e2e:auth
```

## ❌ "Element not found" Fehler

### Problem
```
Error: locator.fill: Target page, context or browser has been closed
```

### Lösung

**1. Selektoren prüfen:**
```bash
# Test im headed mode laufen lassen
pnpm test:e2e:headed

# Playwright Inspector nutzen
pnpm test:e2e:debug

# In Inspector: Hover über Elemente und sehe Selektoren
```

**2. Warte auf Element:**
```typescript
// FALSCH
await page.getByLabel('Name').fill('Test')

// RICHTIG
await page.getByLabel('Name').waitFor()
await page.getByLabel('Name').fill('Test')
```

## ❌ Backend/Frontend nicht erreichbar

### Problem
```
✅ Frontend server is running on http://localhost:5173
❌ Backend server is not running on http://localhost:8080
```

### Lösungen

#### Backend läuft nicht

```bash
# Prüfen ob Port belegt ist
lsof -i :8080

# Backend starten
cd ../backend
./mvnw spring-boot:run

# Warten bis:
# "Started ResulterApplication in X.XXX seconds"
```

#### Frontend läuft nicht

```bash
# Prüfen ob Port belegt ist
lsof -i :5173

# Frontend starten
pnpm dev

# Warten bis:
# "VITE vX.X.X ready in XXms"
```

#### Port bereits belegt

```bash
# Port-Besitzer finden
lsof -i :5173
lsof -i :8080

# Prozess killen
kill -9 <PID>

# Neu starten
pnpm dev  # Frontend
cd ../backend && ./mvnw spring-boot:run  # Backend
```

## ❌ "e2e/.env.local not found"

### Problem
```
❌ e2e/.env.local not found
```

### Lösung

**Erstelle `e2e/.env.local`:**

```bash
cat > e2e/.env.local << 'EOF'
HOSTNAME=localhost
FRONTEND_PROTOCOL=http
PORT=5173
BACKEND_PROTOCOL=http
BACKEND_PORT=8080
BACKEND_PROFILES=dev
VITE_MODE=development
USERNAME=dein-username
PASSWORD=dein-password
EOF
```

**Wichtig:**
- `USERNAME` und `PASSWORD` müssen in Keycloak existieren
- Realm: `resulter`
- Client: `resulter-backend` (für BFF)

## ❌ Authentication fails - Wrong Credentials

### Problem
```
Error: locator.fill: Target closed
```
oder Keycloak zeigt "Invalid username or password"

### Lösung

**1. Credentials in Keycloak prüfen:**

```bash
# Keycloak Admin Console öffnen
open https://keycloak.jobst24.de/admin

# Navigiere zu:
# Realm: resulter
# Users → Suche nach deinem USERNAME
# Prüfe:
# - User existiert
# - User ist "Enabled"
# - Email ist verifiziert (falls required)
```

**2. Credentials in .env.local prüfen:**

```bash
# Zeige aktuelle Credentials (VORSICHT: Passwort sichtbar!)
cat e2e/.env.local | grep USERNAME
cat e2e/.env.local | grep PASSWORD

# Teste Login manuell im Browser:
# 1. Öffne http://localhost:8080/oauth2/authorization/keycloak
# 2. Gebe USERNAME und PASSWORD ein
# 3. Sollte zu http://localhost:5173 redirecten
```

**3. User Rollen prüfen:**

Einige Tests benötigen ADMIN-Rolle:

```bash
# In Keycloak Admin Console:
# Realm: resulter
# Users → Dein User → Role Mappings
# Füge hinzu: "ADMIN" oder "admin"
```

## ❌ Tests laufen aber schlagen fehl

### Problem
Tests starten, aber funktionale Assertions schlagen fehl

### Debug-Strategien

#### 1. Headed Mode

```bash
# Sehe was im Browser passiert
pnpm test:e2e:headed

# Oder spezifischer Test
pnpm playwright test event-form.spec.ts --headed -g "should create event"
```

#### 2. Debug Mode (Step-through)

```bash
# Playwright Inspector öffnet sich
pnpm test:e2e:debug

# Oder spezifischer Test
pnpm playwright test event-form.spec.ts --debug -g "should create event"

# Im Inspector:
# - Step through: F10
# - Resume: F8
# - Screenshot: Camera Icon
```

#### 3. Screenshots & Videos

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

# Fehler-Screenshots in:
ls test-results/*/screenshots/

# Fehler-Videos in:
ls test-results/*/videos/
```

#### 4. Network Logs

**In Test einfügen:**
```typescript
page.on('request', request =>
    console.log('>>', request.method(), request.url())
)
page.on('response', response =>
    console.log('<<', response.status(), response.url())
)
```

#### 5. Console Logs

**In Test einfügen:**
```typescript
page.on('console', msg => console.log('PAGE LOG:', msg.text()))
```

## ❌ "Session expired" während Tests

### Problem
Mitten im Test: 401 Unauthorized oder Redirect zu Login

### Ursache
Session-Cookie abgelaufen (Standard: 30 Min)

### Lösung

**1. Session-Timeout erhöhen (Backend):**

```yaml
# application.yml
server:
  servlet:
    session:
      timeout: 60m  # Erhöhe von 30m auf 60m
```

**2. Vor jedem Test-Run neu authentifizieren:**

```bash
# Alte Session löschen
rm e2e/.auth/storageState.json

# Tests laufen lassen (Auth-Setup läuft automatisch)
pnpm test:e2e
```

**3. Schnellere Tests schreiben:**
- Weniger `page.waitForTimeout()`
- Mehr `expect().toBeVisible()` statt fixer Waits

## ❌ Tests funktionieren lokal, aber nicht in CI

### Problem
Tests auf lokalem Rechner ✅, in GitHub Actions ❌

### Lösungen

#### 1. Environment Variables

**Prüfe `.github/workflows/*.yml`:**
```yaml
env:
  USERNAME: ${{ secrets.TEST_USERNAME }}
  PASSWORD: ${{ secrets.TEST_PASSWORD }}
```

**Stelle sicher:**
- Secrets sind in GitHub Repository Settings definiert
- Namen stimmen exakt überein (case-sensitive!)

#### 2. Server-Startup in CI

**playwright.config.ts hat `webServer` für CI:**
```typescript
webServer: process.env.CI ? [...] : undefined
```

**Das sollte funktionieren, aber:**
- Erhöhe Timeout falls Server langsam startet
- Prüfe CI Logs für Server-Startup-Fehler

#### 3. Headless vs Headed

CI läuft immer headless. Teste lokal auch headless:

```bash
pnpm test:e2e  # Headless wie in CI
```

## 🛠️ Erweiterte Debugging-Tools

### Playwright Trace Viewer

**Nach Test-Failure:**
```bash
# HTML Report öffnen
pnpm test:e2e:report

# Dann auf failed test klicken
# Trace wird automatisch angezeigt

# Oder manuell:
pnpm playwright show-trace test-results/*/trace.zip
```

**Im Trace Viewer:**
- Sehe jeden Action Schritt
- Sehe Screenshots für jeden Step
- Sehe Network Requests
- Sehe Console Logs
- Sehe Source Code

### Browser Developer Tools

**In headed mode:**
1. Test mit `--headed` starten
2. Rechtsklick → Inspect
3. Developer Tools öffnen sich
4. Nutze wie normale Web-Development

### Pause im Test

**In Test einfügen:**
```typescript
await page.pause()  // Playwright Inspector öffnet sich
```

**Dann:**
- Inspiziere Seite
- Führe Commands manuell aus
- Resume wenn bereit

## 📞 Hilfe holen

Wenn nichts funktioniert:

1. **Logs sammeln:**
   ```bash
   # Backend logs
   cd ../backend && ./mvnw spring-boot:run > backend.log 2>&1

   # Frontend logs
   pnpm dev > frontend.log 2>&1

   # E2E logs
   pnpm test:e2e:debug > e2e.log 2>&1
   ```

2. **Report erstellen:**
   ```bash
   pnpm test:e2e:report
   # Report wird automatisch geöffnet
   ```

3. **Kontext dokumentieren:**
   - Welcher Test schlägt fehl?
   - Error Message
   - Screenshots/Videos
   - Logs
   - Environment (OS, Node version, etc.)

4. **Issue erstellen:**
   - GitHub Issue mit allen Infos
   - Oder Team-Chat mit Logs
