# E2E-Datenbankisolierung - Vollständige technische Dokumentation

> **Deutsch** · [English](E2E_DATABASE_ISOLATION.md)

## Inhaltsverzeichnis

1. [Überblick](#überblick)
2. [Architektur](#architektur)
3. [Funktionsweise](#funktionsweise)
4. [Komponenten](#komponenten)
5. [Implementierungsleitfaden](#implementierungsleitfaden)
6. [Nutzungsmuster](#nutzungsmuster)
7. [Konfiguration](#konfiguration)
8. [Technische Details](#technische-details)
9. [Best Practices](#best-practices)
10. [Fehlerbehebung](#fehlerbehebung)
11. [Überlegungen zur Performance](#überlegungen-zur-performance)
12. [Entwicklerleitfaden](#entwicklerleitfaden)

---

## Überblick

### Was ist E2E-Datenbankisolierung?

Die E2E-Datenbankisolierung ist eine Testinfrastruktur, die jedem Playwright-E2E-Test eine eigene, isolierte PostgreSQL-Datenbank bereitstellt. Das verhindert Wechselwirkungen zwischen Tests, ermöglicht eine sichere parallele Testausführung und macht manuelle Aufräumlogik überflüssig.

### Wichtigste Vorteile

✅ **Vollständige Testisolierung** - Tests können die Daten anderer Tests nicht verfälschen
✅ **Kein manuelles Aufräumen** - Datenbanken werden nach 30 Sekunden Inaktivität automatisch gelöscht
✅ **Sicher bei paralleler Ausführung** - Jeder Test hat seine eigene Datenbank
✅ **Einfacherer Testcode** - Mehr als 12 Aufräumzeilen pro Test entfallen
✅ **Frischer Zustand** - Jeder Test startet mit einer sauberen, migrierten Datenbank
✅ **Flexible Granularität** - Wahl zwischen Isolierung pro Test, pro Suite oder keiner Isolierung

### Das Problem, das gelöst wird

**Vor der Datenbankisolierung:**
```typescript
test('create event', async ({ page }) => {
    const uniqueName = `Event ${Date.now()}`

    // Event anlegen
    await page.fill('[name="name"]', uniqueName)
    await page.click('button:has-text("Save")')

    // Manuelles Aufräumen (12+ Zeilen)
    await page.goto('/event')
    const row = page.locator(`tr:has-text("${uniqueName}")`)
    await row.locator('button[aria-label="Delete"]').click()
    await page.locator('button:has-text("Confirm")').click()
    await expect(row).not.toBeVisible()

    // Problem: Schlägt der Test vor dem Aufräumen fehl, bleiben die Daten bestehen
    // Problem: Tests beeinflussen sich gegenseitig
    // Problem: Parallele Ausführung verursacht Race Conditions
})
```

**Nach der Datenbankisolierung:**
```typescript
test('create event', async ({ page }) => {
    // Datenbank wird automatisch in beforeEach angelegt

    // Event anlegen
    await page.fill('[name="name"]', 'Test Event')
    await page.click('button:has-text("Save")')

    // Das war's! Kein Aufräumen nötig.
    // Die Datenbank wird nach 30 Sekunden automatisch gelöscht.
})
```

---

## Architektur

### Systemüberblick

```
┌─────────────────────────────────────────────────────────────┐
│                      Playwright Test                         │
│                                                              │
│  1. createTestDatabase() → Returns UUID                     │
│  2. Set X-DB-Identifier cookie with UUID                    │
│  3. Navigate to page → Cookie sent to backend               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Backend (Spring Boot)                     │
│                                                              │
│  DataSourceInterceptor (preHandle)                          │
│    ↓ Reads X-DB-Identifier from cookie                      │
│    ↓ Calls DataSourceManager.getOrCreateDataSource(uuid)    │
│    ↓ Stores DataSource in ThreadLocal                       │
│                                                              │
│  DynamicRoutingDataSource (determineTargetDataSource)       │
│    ↓ Reads DataSource from ThreadLocal                      │
│    ↓ Routes all DB operations to isolated database          │
│                                                              │
│  DataSourceCleanupScheduler (every 10 seconds)              │
│    ↓ Checks DataSource last access time                     │
│    ↓ Closes and removes DataSources inactive >30s           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    PostgreSQL (Testcontainers)               │
│                                                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐│
│  │ default testdb  │  │ test-db-uuid-1  │  │ test-db-...  ││
│  │ (main schema)   │  │ (isolated)      │  │ (isolated)   ││
│  └─────────────────┘  └─────────────────┘  └──────────────┘│
│                                                              │
│  Each database has:                                          │
│  - Full Liquibase migrations applied                        │
│  - Isolated schema and data                                 │
│  - Automatic cleanup after 30s                              │
└─────────────────────────────────────────────────────────────┘
```

### Datenfluss

1. **Phase Testvorbereitung:**
   ```
   Test → createTestDatabase() → POST /createDatabase
        ← UUID (e.g., "f8a1e027-...")
   ```

2. **Phase Anfrage:**
   ```
   Browser → HTTP Request with Cookie: X-DB-Identifier=f8a1e027...
           → DataSourceInterceptor reads cookie
           → DataSourceManager creates/retrieves DataSource
           → ThreadLocal stores DataSource
   ```

3. **Phase Datenbankoperation:**
   ```
   Spring Data JDBC → DynamicRoutingDataSource.determineTargetDataSource()
                    → Reads DataSource from ThreadLocal
                    → Routes query to test-db-f8a1e027...
   ```

4. **Phase Aufräumen:**
   ```
   DataSourceCleanupScheduler (every 10s)
   → Check all DataSources
   → If lastAccessTime > 30s ago
   → Close DataSource + Drop database
   ```

---

## Funktionsweise

### Cookie-basiertes Routing

**Warum Cookies statt Header?**

Header würden den OAuth2/Keycloak-Authentifizierungsablauf stören:
- Keycloak-Weiterleitungen würden benutzerdefinierte Header verlieren
- Mehrere Weiterleitungen im OAuth2-Ablauf
- Cookies bleiben über Weiterleitungen hinweg erhalten
- Cookies werden nur an `localhost:8080` (Backend) gesendet, nicht an Keycloak

**Cookie-Konfiguration:**
```typescript
await page.context().addCookies([{
    name: 'X-DB-Identifier',
    value: dbIdentifier,
    domain: 'localhost',
    path: '/',
    httpOnly: false,  // Für JavaScript zugänglich
    secure: false,    // HTTP (Entwicklungsumgebung)
    sameSite: 'Lax',  // Wird bei Navigation mitgesendet
}])
```

### Lebenszyklus der Datenbank

```
1. CREATE DATABASE
   ├─ POST /createDatabase with API token
   ├─ DataSourceManager creates PostgreSQLContainer database
   ├─ Generate UUID identifier (e.g., test-db-f8a1e027...)
   ├─ Create DataSource with JDBC URL to new database
   ├─ Run Liquibase migrations (full schema + seed data)
   └─ Return UUID to test

2. USE DATABASE (per request)
   ├─ HTTP request with X-DB-Identifier cookie
   ├─ DataSourceInterceptor.preHandle()
   ├─ Read cookie value (UUID)
   ├─ DataSourceManager.getOrCreateDataSource(uuid)
   ├─ Store DataSource in DataSourceContextHolder (ThreadLocal)
   ├─ Update lastAccessTime
   └─ DynamicRoutingDataSource routes to isolated DB

3. CLEANUP DATABASE (automatic)
   ├─ DataSourceCleanupScheduler runs every 10 seconds
   ├─ Iterate all tracked DataSources
   ├─ Check: (now - lastAccessTime) > 30 seconds
   ├─ If yes:
   │   ├─ Close DataSource (close all connections)
   │   ├─ Drop database: DROP DATABASE IF EXISTS test-db-{uuid}
   │   └─ Remove from tracking map
   └─ Garbage collection frees resources
```

---

## Komponenten

### Backend-Komponenten

#### 1. TestContainersConfig.java
```java
@Configuration
@Profile("e2e-frontend-tests")
public class TestContainersConfig {
    private static final PostgreSQLContainer<?> postgresContainer = ...;

    static {
        postgresContainer.start(); // Einmal starten, für alle Tests wiederverwenden
    }

    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("defaultDataSource") DataSource defaultDataSource) {
        return new DynamicRoutingDataSource(defaultDataSource);
    }
}
```

**Zweck:** Startet einen einzelnen PostgreSQL-Container und konfiguriert das dynamische Routing

**Wichtige Merkmale:**
- Ein einzelner Container für alle Tests (Performance)
- Jeder Test erhält innerhalb des Containers seine eigene Datenbank
- DynamicRoutingDataSource als primäre DataSource

#### 2. DynamicRoutingDataSource.java
```java
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected DataSource determineTargetDataSource() {
        DataSource contextDataSource = DataSourceContextHolder.getDataSource();

        if (contextDataSource != null) {
            return contextDataSource; // Isolierte Datenbank verwenden
        }

        return defaultDataSource; // Rückfall auf die Standard-Datenbank
    }
}
```

**Zweck:** Leitet Datenbankoperationen an die richtige isolierte Datenbank weiter

**Funktionsweise:**
- Spring ruft `determineTargetDataSource()` für jede Datenbankoperation auf
- Liest die DataSource aus dem ThreadLocal (vom Interceptor gesetzt)
- Gibt die isolierte DataSource zurück, falls vorhanden, sonst die Standard-DataSource

#### 3. DataSourceInterceptor.java
```java
@Component
@Profile("e2e-frontend-tests")
public class DataSourceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        String dbIdentifier = extractDbIdentifier(request);

        if (dbIdentifier != null) {
            DataSource dataSource = dataSourceManager.getOrCreateDataSource(dbIdentifier);
            DataSourceContextHolder.setDataSource(dataSource);
        }

        return true;
    }

    @Override
    public void afterCompletion(...) {
        DataSourceContextHolder.clear(); // ThreadLocal aufräumen
    }
}
```

**Zweck:** Fängt HTTP-Anfragen ab und richtet das Datenbank-Routing ein

**Wichtige Abläufe:**
1. Liest `X-DB-Identifier` aus Cookie oder Header
2. Holt oder erzeugt die DataSource für diesen Identifier
3. Speichert sie im ThreadLocal für die aktuelle Anfrage
4. Räumt den ThreadLocal auf, nachdem die Anfrage abgeschlossen ist

#### 4. DataSourceManager.java
```java
@Component
@Profile("e2e-frontend-tests")
public class DataSourceManager {

    private final ConcurrentHashMap<String, DataSourceWrapper> dataSources = new ConcurrentHashMap<>();

    public DataSource getOrCreateDataSource(String dbIdentifier) {
        return dataSources.computeIfAbsent(dbIdentifier, this::createDataSource)
                          .updateAccessTime()
                          .getDataSource();
    }

    private DataSourceWrapper createDataSource(String dbIdentifier) {
        String dbName = "test-db-" + dbIdentifier;

        // Datenbank im PostgreSQL-Container anlegen
        createDatabaseInContainer(dbName);

        // DataSource anlegen, die auf die neue Datenbank zeigt
        DataSource dataSource = DataSourceBuilder.create()
            .url(postgresContainer.getJdbcUrl().replace("/testdb", "/" + dbName))
            .username(postgresContainer.getUsername())
            .password(postgresContainer.getPassword())
            .build();

        // Liquibase-Migrationen ausführen
        runLiquibaseMigrations(dataSource);

        return new DataSourceWrapper(dataSource);
    }
}
```

**Zweck:** Verwaltet den Lebenszyklus der isolierten DataSources

**Wichtige Merkmale:**
- Threadsichere Erzeugung von DataSources (ConcurrentHashMap)
- Lazy Erzeugung (nur bei Bedarf)
- Erfasst die letzte Zugriffszeit für das Aufräumen
- Führt Liquibase-Migrationen automatisch aus

#### 5. DataSourceCleanupScheduler.java
```java
@Component
@Profile("e2e-frontend-tests")
public class DataSourceCleanupScheduler {

    @Scheduled(fixedDelay = 10000) // Alle 10 Sekunden
    public void cleanupInactiveDatabases() {
        long now = System.currentTimeMillis();
        long inactivityThreshold = 30000; // 30 Sekunden

        dataSources.entrySet().removeIf(entry -> {
            DataSourceWrapper wrapper = entry.getValue();
            boolean inactive = (now - wrapper.getLastAccessTime()) > inactivityThreshold;

            if (inactive) {
                closeAndDropDatabase(entry.getKey(), wrapper);
            }

            return inactive;
        });
    }
}
```

**Zweck:** Automatisches Aufräumen ungenutzter Datenbanken

**Verhalten:**
- Läuft alle 10 Sekunden
- Prüft alle DataSources auf Inaktivität über 30 Sekunden
- Schließt Verbindungen und löscht die Datenbank
- Entfernt den Eintrag aus der Tracking-Map

#### 6. DatabaseController.java
```java
@RestController
@Profile("e2e-frontend-tests")
public class DatabaseController {

    @PostMapping("/createDatabase")
    @PreAuthorize("hasRole('CREATE_DATABASE')")
    public ResponseEntity<Map<String, String>> createDatabase() {
        String uuid = UUID.randomUUID().toString();

        // Datenbank vorab anlegen
        dataSourceManager.getOrCreateDataSource(uuid);

        return ResponseEntity.ok(Map.of("identifier", uuid));
    }
}
```

**Zweck:** API-Endpunkt, über den Tests neue Datenbanken anfordern

**Sicherheit:**
- Erfordert `ROLE_CREATE_DATABASE` (aus dem API-Token)
- Nur im Profil `e2e-frontend-tests` aktiv

#### 7. CreateDatabaseApiTokenFilter.java
```java
@Component
@Profile("e2e-frontend-tests")
public class CreateDatabaseApiTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        if (request.getRequestURI().equals("/createDatabase")) {
            String token = extractToken(request);

            if (validateToken(token)) {
                Authentication auth = new UsernamePasswordAuthenticationToken(
                    "create-database-user",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CREATE_DATABASE"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

**Zweck:** API-Token-Authentifizierung für den Endpunkt /createDatabase

**Funktionsweise:**
- Prüft den Authorization-Header: `Bearer <token>`
- Alternativ: Header `X-CREATE-DATABASE-TOKEN`
- Validiert gegen das konfigurierte Token
- Setzt die Authentifizierung mit `ROLE_CREATE_DATABASE`

### Frontend-Komponenten

#### 1. helpers/database.ts

```typescript
export async function createTestDatabase(config?: DatabaseConfig): Promise<string> {
    const backendUrl = `${protocol}://${hostname}:${port}/createDatabase`
    const token = process.env.CREATEDATABASE_API_TOKEN || 'test-database-token'

    const response = await fetch(backendUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(requestBody),
        signal: AbortSignal.timeout(config?.timeoutMs || 120000),
    })

    const data = await response.json()
    return data.data.identifier // UUID
}

export function useDatabaseIsolation(dbIdentifier: string) {
    return {
        cookies: [{
            name: 'X-DB-Identifier',
            value: dbIdentifier,
            domain: 'localhost',
            path: '/',
            httpOnly: false,
            secure: false,
            sameSite: 'Lax' as const,
        }],
    }
}
```

**Zweck:** Hilfsfunktionen zum Anlegen und Routen von Testdatenbanken

**Merkmale:**
- Legt die Datenbank über einen API-Aufruf an
- Gibt den UUID-Identifier zurück
- Liefert eine Playwright-kompatible Cookie-Konfiguration
- Konfigurierbarer Timeout (Standard 2 Minuten wegen des Container-Starts)

---

## Implementierungsleitfaden

### Schritt-für-Schritt-Implementierung

#### 1. Backend-Einrichtung

**Profil aktivieren:**
```bash
cd backend
export SPRING_PROFILES_ACTIVE=e2e-frontend-tests
./mvnw spring-boot:run
```

**Oder das Hilfsskript verwenden:**
```bash
./start-e2e-frontend-tests.sh
```

**Erforderliche Umgebungsvariablen:**
```bash
# .env-Datei im Projektstamm
CREATEDATABASE_API_TOKEN=your-secret-token-here
RESULTER_MEDIA_FILE_PATH=/tmp/resulter-media
RESULTER_MEDIA_FILE_PATH_THUMBNAILS=/tmp/resulter-media/thumbnails
API_OAUTH2_RESOURCE_SERVER_JWT_ISSUER_URI=https://keycloak.example.com/realms/resulter
API_OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI=https://keycloak.example.com/realms/resulter/protocol/openid-connect/certs
# ... (vollständige Liste siehe .env.example)
```

#### 2. Frontend-Einrichtung

**Umgebungsdatei anlegen:**
```bash
# frontend/e2e/.env.local
BACKEND_PROFILES=e2e-frontend-tests
CREATEDATABASE_API_TOKEN=your-secret-token-here
```

**Hilfsfunktionen importieren:**
```typescript
import { createTestDatabase, useDatabaseIsolation } from './helpers/database'
```

#### 3. Tests mit Datenbankisolierung schreiben

**Muster 1: Isolierung pro Test**
```typescript
import { test, expect } from '@playwright/test'
import { createTestDatabase } from './helpers/database'

test.describe('Event Creation', () => {
    test.beforeEach(async ({ page }) => {
        // Frische Datenbank für diesen Test anlegen
        const dbId = await createTestDatabase()

        // Cookie setzen, um auf die isolierte Datenbank zu routen
        await page.context().addCookies([{
            name: 'X-DB-Identifier',
            value: dbId,
            domain: 'localhost',
            path: '/',
            httpOnly: false,
            secure: false,
            sameSite: 'Lax',
        }])

        // Zur Seite navigieren - Cookie wird automatisch gesendet
        await page.goto('/en/event/new')
    })

    test('should create event with all fields', async ({ page }) => {
        await page.fill('[name="name"]', 'Test Event')
        await page.fill('[name="date"]', '2027-12-31')
        await page.click('button:has-text("Save")')

        // Prüfen, dass das Event angelegt wurde
        await expect(page).toHaveURL(/\/event$/)
        await expect(page.locator('text=Test Event')).toBeVisible()

        // KEIN AUFRÄUMEN NÖTIG - Datenbank wird nach 30 s automatisch gelöscht
    })
})
```

**Muster 2: Gemeinsame Datenbank pro Suite**
```typescript
test.describe('Event Editing', () => {
    let sharedDbId: string
    let eventName: string

    test.beforeAll(async ({ browser }) => {
        // Datenbank einmal für die gesamte Suite anlegen
        sharedDbId = await createTestDatabase({ timeoutMs: 300000 }) // 5 Min. Timeout
        eventName = `Shared Event ${Date.now()}`

        // Testdaten EINMAL anlegen
        const context = await browser.newContext({
            storageState: 'e2e/.auth/storageState.json'
        })
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
        await page.fill('[name="name"]', eventName)
        await page.click('button:has-text("Save")')

        await context.close()
    })

    test.beforeEach(async ({ page }) => {
        // Jeder Test verwendet dieselbe Datenbank
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

    test('should edit event name', async ({ page }) => {
        await page.goto('/en/event')
        await page.locator(`tr:has-text("${eventName}")`).locator('button[aria-label="Edit"]').click()
        await page.fill('[name="name"]', 'Updated Name')
        await page.click('button:has-text("Save")')

        await expect(page.locator('text=Updated Name')).toBeVisible()
    })

    test('should edit event date', async ({ page }) => {
        // Verwendet dieselbe Datenbank, sieht "Updated Name" aus dem vorherigen Test
        // Das ist bei Bearbeitungstests beabsichtigt
    })
})
```

**Muster 3: Keine Isolierung (nur lesend)**
```typescript
test.describe('Form Validation', () => {
    // Kein Datenbank-Setup - verwendet die Standarddatenbank

    test('should validate required fields', async ({ page }) => {
        await page.goto('/en/event/new')
        await page.click('button:has-text("Save")')

        // Validierungsfehler prüfen
        await expect(page.locator('text=Name is required')).toBeVisible()
    })
})
```

---

## Nutzungsmuster

### Wann welches Muster verwenden

| Muster | Anwendungsfall | Beispieltests | Lebensdauer der Datenbank |
|---------|----------|---------------|-------------------|
| **Isolierung pro Test** | Anlege-/Änderungsoperationen, die einen frischen Zustand benötigen | Event anlegen, Ergebnisse importieren, Benutzerregistrierung | 1 Datenbank pro Test (~30 s) |
| **Gemeinsame Datenbank pro Suite** | Bearbeitungs-/Aktualisierungsoperationen auf derselben Entität | Event bearbeiten, Urkunde hinzufügen, Einstellungen aktualisieren | 1 Datenbank pro Suite (~5 Min.) |
| **Keine Isolierung** | Rein lesende Validierung, UI-Tests | Formularvalidierung, Ladezustände, Navigation | Verwendet die Standarddatenbank |

### Entscheidungsbaum für das Muster

```
Legt dein Test neue Daten an?
├─ Ja → Braucht jeder Test eigene, isolierte Daten?
│         ├─ Ja → Isolierung pro Test
│         └─ Nein → Gemeinsame Datenbank pro Suite
└─ Nein → Ändert er bestehende Daten?
          ├─ Ja → Gemeinsame Datenbank pro Suite
          └─ Nein → Keine Isolierung
```

### Codebeispiele je Muster

#### Beispiel Isolierung pro Test
```typescript
// ✅ GUT: Jeder Test erhält eine frische Datenbank
test.describe('Event Creation', () => {
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
    })

    test('create event with name only', async ({ page }) => { ... })
    test('create event with all fields', async ({ page }) => { ... })
    test('create event with organisations', async ({ page }) => { ... })
})
```

#### Beispiel gemeinsame Datenbank pro Suite
```typescript
// ✅ GUT: Alle Tests teilen sich eine Datenbank und ein Event
test.describe('Event Editing', () => {
    test.describe.configure({ mode: 'serial' }) // Tests nacheinander ausführen

    let sharedDbId: string

    test.beforeAll(async ({ browser }) => {
        sharedDbId = await createTestDatabase({ timeoutMs: 300000 })
        // Gemeinsames Test-Event anlegen
    })

    test.beforeEach(async ({ page }) => {
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

    test('edit name', async ({ page }) => { ... })
    test('edit date', async ({ page }) => { ... })
    test('add certificate', async ({ page }) => { ... })
})
```

#### Beispiel ohne Isolierung
```typescript
// ✅ GUT: Rein lesende Tests brauchen keine Isolierung
test.describe('Form Validation', () => {
    // Kein beforeEach - keine Datenbankisolierung

    test('validate required fields', async ({ page }) => {
        await page.goto('/en/event/new')
        await page.click('button:has-text("Save")')
        await expect(page.locator('text=Name is required')).toBeVisible()
    })
})
```

---

## Konfiguration

### Backend-Konfiguration

**Datei: `backend/src/main/resources/application-e2e-frontend-tests.properties`**

```properties
# Server-Konfiguration
server.port=8080
cors.allowed-origins=http://localhost:5173,http://localhost:8080

# Spring Docker Compose deaktivieren (wir verwenden Testcontainers direkt)
spring.docker.compose.enabled=false
spring.docker.compose.lifecycle-management=none

# Sicherheit: API-Token zum Anlegen von Datenbanken
security.createdatabase.api-token=${CREATEDATABASE_API_TOKEN}

# Pfade für Mediendateien (über Umgebungsvariablen erforderlich)
resulter.media-file-path=${RESULTER_MEDIA_FILE_PATH}
resulter.media-file-path-thumbnails=${RESULTER_MEDIA_FILE_PATH_THUMBNAILS}

# OAuth2 Resource Server (über Umgebungsvariablen erforderlich)
spring.security.oauth2.resourceserver.jwt.issuer-uri=${API_OAUTH2_RESOURCE_SERVER_JWT_ISSUER_URI}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${API_OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI}

# OAuth2 Client BFF (über Umgebungsvariablen erforderlich)
spring.security.oauth2.client.registration.keycloak.client-id=${BFF_OAUTH2_CLIENT_ID}
spring.security.oauth2.client.registration.keycloak.client-secret=${BFF_OAUTH2_CLIENT_SECRET}
spring.security.oauth2.client.provider.keycloak.authorization-uri=${KEYCLOAK_AUTHORIZATION_URI}
spring.security.oauth2.client.provider.keycloak.token-uri=${KEYCLOAK_TOKEN_URI}
spring.security.oauth2.client.provider.keycloak.user-info-uri=${KEYCLOAK_USER_INFO_URI}
spring.security.oauth2.client.provider.keycloak.jwk-set-uri=${KEYCLOAK_JWK_SET_URI}

# Logging-Konfiguration
logging.level.root=WARN
logging.level.de.jobst.resulter.springapp.config.DataSourceInterceptor=DEBUG
logging.level.de.jobst.resulter.springapp.config.DataSourceManager=DEBUG
```

### Frontend-Konfiguration

**Datei: `frontend/e2e/.env.local`**

```env
# Backend-Konfiguration
HOSTNAME=localhost
BACKEND_PROTOCOL=http
BACKEND_PORT=8080
BACKEND_PROFILES=e2e-frontend-tests

# Frontend-Konfiguration
FRONTEND_PROTOCOL=http
PORT=5173
VITE_MODE=development

# Testzugangsdaten
USERNAME=test-user
PASSWORD=test-password

# API-Tokens
CREATEDATABASE_API_TOKEN=your-secret-token-here
```

### Umgebungsvariablen

**Erforderliche Variablen (müssen in der `.env`-Datei im Projektstamm gesetzt sein):**

```bash
# OAuth2 Resource Server
API_OAUTH2_RESOURCE_SERVER_JWT_ISSUER_URI=https://keycloak.example.com/realms/resulter
API_OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI=https://keycloak.example.com/realms/resulter/protocol/openid-connect/certs

# BFF-OAuth2-Client
BFF_OAUTH2_CLIENT_ID=resulter-bff
BFF_OAUTH2_CLIENT_SECRET=your-oauth2-secret

# Keycloak-Provider-URIs
KEYCLOAK_AUTHORIZATION_URI=https://keycloak.example.com/realms/resulter/protocol/openid-connect/auth
KEYCLOAK_TOKEN_URI=https://keycloak.example.com/realms/resulter/protocol/openid-connect/token
KEYCLOAK_USER_INFO_URI=https://keycloak.example.com/realms/resulter/protocol/openid-connect/userinfo
KEYCLOAK_JWK_SET_URI=https://keycloak.example.com/realms/resulter/protocol/openid-connect/certs

# Pfade für Mediendateien
RESULTER_MEDIA_FILE_PATH=/tmp/resulter-media
RESULTER_MEDIA_FILE_PATH_THUMBNAILS=/tmp/resulter-media/thumbnails

# API-Tokens (erzeugen mit: openssl rand -base64 32)
CREATEDATABASE_API_TOKEN=your-create-database-token
PROMETHEUS_API_TOKEN=your-prometheus-token
```

---

## Technische Details

### Threading-Modell

**ThreadLocal-Speicher:**
```java
public class DataSourceContextHolder {
    private static final ThreadLocal<DataSource> contextHolder = new ThreadLocal<>();

    public static void setDataSource(DataSource dataSource) {
        contextHolder.set(dataSource);
    }

    public static DataSource getDataSource() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove(); // Memory Leaks verhindern
    }
}
```

**Warum ThreadLocal?**
- Jede HTTP-Anfrage läuft in einem eigenen Thread
- ThreadLocal stellt einen anfragebezogenen Speicher bereit
- Keine Beeinflussung zwischen Anfragen
- Automatische Isolierung zwischen parallelen Anfragen
- Muss nach der Anfrage geleert werden, um Leaks zu verhindern

**Ablauf einer Anfrage:**
```
Thread-1 (Test A)                    Thread-2 (Test B)
│                                    │
├─ Request: X-DB-Identifier=uuid-a  ├─ Request: X-DB-Identifier=uuid-b
├─ Interceptor sets ThreadLocal     ├─ Interceptor sets ThreadLocal
│  └─ contextHolder.set(dsA)        │  └─ contextHolder.set(dsB)
├─ Query executed with dsA          ├─ Query executed with dsB
├─ After completion: clear()        ├─ After completion: clear()
└─ ThreadLocal empty                └─ ThreadLocal empty
```

### Connection Pooling

**Jede isolierte DataSource hat ihren eigenen HikariCP-Pool:**

```java
DataSource dataSource = DataSourceBuilder.create()
    .url(jdbcUrl)
    .username(username)
    .password(password)
    .type(HikariDataSource.class)
    .build();

// Standardkonfiguration von HikariCP
// - maximumPoolSize: 10
// - minimumIdle: 10
// - connectionTimeout: 30000ms
// - idleTimeout: 600000ms (10 Minuten)
```

**Ressourcenverbrauch:**
- Standard-Datenbank: 1 Connection Pool (10 Verbindungen)
- Pro Test-Datenbank: 1 Connection Pool (10 Verbindungen)
- Bei 5 parallelen Tests: insgesamt ca. 60 Verbindungen

**Lebenszyklus der Verbindungen:**
```
Create DB → Create DataSource → HikariCP Pool Created
         → Test runs → Connections used
         → Test ends → Connections idle
         → 30s timeout → Cleanup triggered
         → Pool shutdown → Database dropped
         → Connections closed
```

### Liquibase-Integration

**Automatische Migration beim Anlegen der Datenbank:**

```java
private void runLiquibaseMigrations(DataSource dataSource) {
    try {
        Database database = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));

        Liquibase liquibase = new Liquibase(
            "db/changelog/db.changelog-master.yaml",
            new ClassLoaderResourceAccessor(),
            database
        );

        liquibase.update(new Contexts(), new LabelExpression());
    } catch (Exception e) {
        throw new RuntimeException("Failed to run Liquibase migrations", e);
    }
}
```

**Was migriert wird:**
- Alle Schemadefinitionen (Tabellen, Indizes, Constraints)
- Alle Seed-Daten (Länder, Organisationen, initiale Cups)
- Alle Funktionen und Stored Procedures

**Performance der Migration:**
- Erstes Anlegen einer Datenbank: ca. 2-5 Sekunden (Liquibase-Migrationen)
- Weitere Datenbanken: ca. 2-5 Sekunden (dieselben Migrationen)
- Migrationen laufen nacheinander (Lock-Mechanismus von Liquibase)

### Mechanismus zum Aufräumen der Datenbank

**Konfiguration des Cleanup-Schedulers:**

```java
@Scheduled(fixedDelay = 10000, initialDelay = 10000)
public void cleanupInactiveDatabases() {
    long now = System.currentTimeMillis();
    long inactivityThreshold = 30000; // 30 Sekunden

    dataSources.entrySet().removeIf(entry -> {
        String identifier = entry.getKey();
        DataSourceWrapper wrapper = entry.getValue();

        boolean inactive = (now - wrapper.getLastAccessTime()) > inactivityThreshold;

        if (inactive) {
            logger.info("Cleaning up inactive database: {}", identifier);
            closeAndDropDatabase(identifier, wrapper);
        }

        return inactive;
    });
}

private void closeAndDropDatabase(String identifier, DataSourceWrapper wrapper) {
    try {
        // 1. Alle Verbindungen im Pool schließen
        if (wrapper.getDataSource() instanceof HikariDataSource hikari) {
            hikari.close();
        }

        // 2. Datenbank über Admin-Verbindung löschen
        try (Connection conn = postgresContainer.createConnection("")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP DATABASE IF EXISTS \"test-db-" + identifier + "\"");
            }
        }

        logger.info("Database dropped: test-db-{}", identifier);
    } catch (SQLException e) {
        logger.error("Failed to cleanup database: " + identifier, e);
    }
}
```

**Zeitablauf beim Aufräumen:**
```
Test completes → Last DB access recorded
              → Wait 30 seconds
              → Cleanup scheduler runs (every 10s)
              → Detects inactivity >30s
              → Closes connections
              → Drops database
              → Frees memory
```

---

## Best Practices

### Testorganisation

**✅ SO GEHT'S: Nach Isolierungsstrategie gruppieren**
```typescript
// Suite 1: Anlege-Tests (Isolierung pro Test)
test.describe('Event Creation', () => {
    test.beforeEach(async ({ page }) => {
        const dbId = await createTestDatabase()
        // ...
    })

    test('test 1', ...)
    test('test 2', ...)
})

// Suite 2: Bearbeitungs-Tests (gemeinsame Datenbank pro Suite)
test.describe('Event Editing', () => {
    test.describe.configure({ mode: 'serial' })

    let sharedDbId: string
    test.beforeAll(async ({ browser }) => {
        sharedDbId = await createTestDatabase({ timeoutMs: 300000 })
        // ...
    })

    test('test 1', ...)
    test('test 2', ...)
})
```

**❌ SO NICHT: Isolierungsstrategien in einer Suite mischen**
```typescript
// SCHLECHT: Uneinheitliche Isolierung
test.describe('Event Tests', () => {
    test('create event', async ({ page }) => {
        const dbId = await createTestDatabase() // Pro Test
        // ...
    })

    test('edit event', async ({ page }) => {
        // Keine Datenbankisolierung - verwendet die Standarddatenbank
        // Das sieht das oben angelegte Event nicht!
    })
})
```

### Verwaltung des Datenbank-Lebenszyklus

**✅ SO GEHT'S: Passende Timeouts verwenden**
```typescript
// Pro Test: Standard-Timeout von 2 Minuten
const dbId = await createTestDatabase()

// Pro Suite: Längerer Timeout für mehrere Tests
const dbId = await createTestDatabase({ timeoutMs: 300000 }) // 5 Minuten
```

**✅ SO GEHT'S: Testdaten bei langlaufenden Tests aufräumen**
```typescript
test.afterAll(async ({ browser }) => {
    // Optional: Aufräumen bei langlaufenden Suites explizit auslösen
    // (Normalerweise nicht nötig - automatisches Aufräumen nach 30 s)
})
```

**❌ SO NICHT: Datenbank-IDs global speichern**
```typescript
// SCHLECHT: Globaler Zustand
let globalDbId: string

test.describe('Suite 1', () => {
    test.beforeAll(async () => {
        globalDbId = await createTestDatabase() // Über Suites hinweg geteilt!
    })
})

test.describe('Suite 2', () => {
    test('test', async ({ page }) => {
        // Verwendet dieselbe globalDbId - NICHT isoliert!
    })
})
```

### Cookie-Verwaltung

**✅ SO GEHT'S: Cookie vor der Navigation setzen**
```typescript
await page.context().addCookies([{
    name: 'X-DB-Identifier',
    value: dbId,
    domain: 'localhost',
    path: '/',
    httpOnly: false,
    secure: false,
    sameSite: 'Lax',
}])

await page.goto('/en/event/new') // Cookie wird automatisch gesendet
```

**❌ SO NICHT: Cookie nach der Navigation setzen**
```typescript
await page.goto('/en/event/new') // Verwendet die Standarddatenbank!
await page.context().addCookies([...]) // Zu spät
```

### Parallele Ausführung

**✅ SO GEHT'S: Parallele Ausführung für isolierte Tests aktivieren**
```typescript
// playwright.config.ts
export default defineConfig({
    workers: 4, // Sicher mit Datenbankisolierung
    fullyParallel: true,
})
```

**✅ SO GEHT'S: Serielle Ausführung bei gemeinsamer Datenbank pro Suite verwenden**
```typescript
test.describe('Event Editing', () => {
    test.describe.configure({ mode: 'serial' })
    // Tests laufen nacheinander und teilen sich die Datenbank
})
```

**❌ SO NICHT: Tests mit gemeinsamer Datenbank parallel ausführen**
```typescript
// SCHLECHT: Tests beeinflussen sich gegenseitig
test.describe('Event Editing', () => {
    // fullyParallel: true (default)
    // Tests lesen/ändern möglicherweise gleichzeitig dieselben Daten
})
```

### Fehlerbehandlung

**✅ SO GEHT'S: Fehler beim Anlegen der Datenbank behandeln**
```typescript
test.beforeEach(async ({ page }) => {
    try {
        const dbId = await createTestDatabase()
        await page.context().addCookies([...])
    } catch (error) {
        console.error('Failed to create test database:', error)
        throw error // Test früh fehlschlagen lassen
    }
})
```

**✅ SO GEHT'S: Prüfen, ob das Backend läuft**
```bash
# In der CI/CD-Pipeline
curl -f http://localhost:8080/actuator/health || exit 1
pnpm test:e2e
```

---

## Fehlerbehebung

### Anlegen der Datenbank schlägt fehl

**Fehler:** `Failed to create test database: Connection refused`

**Ursache:** Backend läuft nicht oder nicht im Profil `e2e-frontend-tests`

**Lösung:**
```bash
# Prüfen, ob das Backend läuft
curl http://localhost:8080/actuator/health

# Aktives Profil prüfen
curl http://localhost:8080/actuator/env | grep -i "activeProfiles"

# Sollte zeigen: "e2e-frontend-tests"

# Falls nicht, Backend mit dem richtigen Profil neu starten
cd backend
./start-e2e-frontend-tests.sh
```

### Falsche Datenbank wird verwendet

**Fehler:** Test sieht Daten anderer Tests oder der Standarddatenbank

**Ursache:** Cookie nicht oder falsch gesetzt

**Lösung:**
```typescript
// Prüfen, dass das Cookie VOR der Navigation gesetzt ist
await page.context().addCookies([{
    name: 'X-DB-Identifier',
    value: dbId,
    domain: 'localhost', // Muss 'localhost' sein, nicht '127.0.0.1'
    path: '/',
    httpOnly: false,
    secure: false,
    sameSite: 'Lax',
}])

// Debug-Logging hinzufügen
const cookies = await page.context().cookies()
console.log('Cookies:', cookies.filter(c => c.name === 'X-DB-Identifier'))
```

**Prüfung im Backend:**
```bash
# Backend-Logs auf Datenbank-Routing prüfen
# Sollte erscheinen:
# "X-DB-Identifier header: <uuid>"
# "Routing to database: <uuid>"
# "DataSource found and set for: <uuid>"
```

### Datenbank wird nicht aufgeräumt

**Fehler:** Zu viele Datenbanken, Speicherprobleme

**Ursache:** Cleanup-Scheduler läuft nicht oder Datenbanken sind noch aktiv

**Lösung:**
```bash
# Backend-Logs auf Cleanup-Meldungen prüfen
# Sollte alle 10 Sekunden erscheinen:
# "Checking for inactive databases..."
# "Cleaning up inactive database: <uuid>"

# Falls nicht aufgeräumt wird:
# 1. Prüfen, ob das Profil aktiv ist
curl http://localhost:8080/actuator/env | grep "e2e-frontend-tests"

# 2. Prüfen, ob der Scheduler aktiviert ist
curl http://localhost:8080/actuator/scheduledtasks

# 3. Aufräumen manuell auslösen (falls nötig)
# 30 Sekunden nach Testende warten
# Der Scheduler sollte automatisch aufräumen
```

### Liquibase-Migration schlägt fehl

**Fehler:** `Failed to run Liquibase migrations`

**Ursache:** Konflikte im Datenbankschema oder Migrationsfehler

**Lösung:**
```bash
# Liquibase-Changelog prüfen
cat backend/src/main/resources/db/changelog/db.changelog-master.yaml

# Migrationen auf der Standarddatenbank testen
cd backend
./mvnw liquibase:update -Dspring.profiles.active=e2e-frontend-tests

# Auf Konflikte prüfen
./mvnw liquibase:status -Dspring.profiles.active=e2e-frontend-tests
```

### Performanceprobleme

**Fehler:** Tests langsam, Anlegen der Datenbank dauert über 10 Sekunden

**Ursache:** Performance von Docker/Testcontainers

**Lösungen:**

1. **Docker optimieren:**
```bash
# Docker-Ressourcen erhöhen
# Docker Desktop → Settings → Resources
# - CPUs: 4+
# - Arbeitsspeicher: 8GB+
# - Swap: 2GB+
```

2. **Gemeinsame Datenbank pro Suite verwenden:**
```typescript
// Statt pro Test (7 Datenbankanlagen)
test.beforeEach(async ({ page }) => {
    const dbId = await createTestDatabase() // Langsam
})

// Pro Suite (1 Datenbankanlage)
test.beforeAll(async ({ browser }) => {
    sharedDbId = await createTestDatabase() // Schnell
})
```

3. **Liquibase-Cache nutzen:**
```java
// Liquibase-Migrationen sind bereits optimiert
// Im Testprofil lassen sich aber bestimmte Changesets überspringen
<changeSet id="heavy-migration" context="!test">
    ...
</changeSet>
```

### Probleme mit dem API-Token

**Fehler:** `403 Forbidden` oder `401 Unauthorized` beim Anlegen der Datenbank

**Ursache:** Fehlendes oder ungültiges `CREATEDATABASE_API_TOKEN`

**Solution:**
```bash
# 1. Neues Token erzeugen
openssl rand -base64 32

# 2. In der .env-Datei setzen (Projektstamm)
CREATEDATABASE_API_TOKEN=<generated-token>

# 3. In frontend/e2e/.env.local setzen
CREATEDATABASE_API_TOKEN=<same-token>

# 4. Backend neu starten
cd backend
./start-e2e-frontend-tests.sh

# 5. Prüfen, ob das Token geladen ist
curl -H "Authorization: Bearer <token>" -X POST http://localhost:8080/createDatabase
# Sollte zurückgeben: {"success":true,"data":{"identifier":"<uuid>"}}
```

---

## Überlegungen zur Performance

### Performance beim Anlegen von Datenbanken

**Benchmarks (auf einem MacBook Pro M1):**
- Erstes Anlegen einer Datenbank: ca. 2-5 Sekunden
- Weitere Datenbanken: ca. 2-5 Sekunden
- Liquibase-Migrationen: ca. 1-2 Sekunden
- Gesamter Mehraufwand pro Test: ca. 3-7 Sekunden

**Optimierungsstrategien:**

1. **Gemeinsame Datenbank pro Suite verwenden:**
   - 10 Tests mit Isolierung pro Test: 10 × 5 s = 50 s Mehraufwand
   - 10 Tests mit gemeinsamer Datenbank pro Suite: 1 × 5 s = 5 s Mehraufwand
   - **Ersparnis: 45 Sekunden (90 %)**

2. **Komplexität der Migrationen verringern:**
   ```xml
   <!-- Umfangreiche Seed-Daten in Testdatenbanken überspringen -->
   <changeSet id="seed-data" context="!test">
       <sqlFile path="seed-data.sql"/>
   </changeSet>
   ```

3. **Parallele Ausführung:**
   - 4 Worker mit isolierten Datenbanken: Keine Beschleunigung (das Anlegen der Datenbanken erfolgt nacheinander)
   - 4 Worker mit gemeinsamer Datenbank pro Suite: 4× schnellere Testausführung
   - **Am besten:** Kombination aus gemeinsamer Datenbank pro Suite und paralleler Ausführung

### Speicherverbrauch

**Speicher pro Datenbank:**
- DataSource-Objekt: ca. 1 KB
- HikariCP Connection Pool: ca. 10 MB (10 Verbindungen)
- PostgreSQL-Datenbank: ca. 10-50 MB (abhängig von den Daten)
- Gesamt pro Datenbank: ca. 20-60 MB

**Bei 10 parallelen Tests:**
- 10 Datenbanken × 50 MB = 500 MB
- Plus Standarddatenbank: 50 MB
- **Gesamt: ca. 550 MB**

**Speicheroptimierung:**
```java
// Größe des Connection Pools für Testdatenbanken verringern
@Bean
public DataSource testDataSource(String dbIdentifier) {
    HikariConfig config = new HikariConfig();
    config.setMaximumPoolSize(5); // Von 10 reduziert
    config.setMinimumIdle(2);     // Von 10 reduziert
    return new HikariDataSource(config);
}
```

### Performance beim Aufräumen

**Zeitablauf beim Aufräumen:**
- Prüfintervall: 10 Sekunden
- Inaktivitätsschwelle: 30 Sekunden
- Durchschnittliche Dauer bis zum Aufräumen: 30-40 Sekunden nach dem letzten Zugriff

**Sofortiges Aufräumen (nicht empfohlen):**
```typescript
// Manuelles Aufräumen (nicht nötig, nur zur Veranschaulichung)
test.afterEach(async () => {
    // Auf das Aufräumen durch den Scheduler warten
    await new Promise(resolve => setTimeout(resolve, 35000))
})
```

### Performance der Testsuite

**Beispiel: 22 Tests mit Datenbankisolierung**

**Vor der Optimierung:**
- Isolierung pro Test für alle Tests: 22 × 5 s = 110 s Mehraufwand
- Eigentliche Testausführung: 30 s
- **Gesamt: 140 Sekunden**

**Nach der Optimierung:**
- 8 Anlege-Tests (pro Test): 8 × 5 s = 40 s
- 10 Bearbeitungs-Tests (pro Suite): 1 × 5 s = 5 s
- 4 Validierungs-Tests (ohne Isolierung): 0 s
- Eigentliche Testausführung: 30 s
- **Gesamt: 75 Sekunden (46 % schneller)**

---

## Entwicklerleitfaden

### Neuen Test mit Datenbankisolierung hinzufügen

**Schritt 1: Isolierungsstrategie festlegen**

Frage dich:
- Legt der Test neue Daten an? → Isolierung pro Test
- Ändert der Test bestehende Daten? → Gemeinsame Datenbank pro Suite
- Validiert oder liest der Test nur? → Keine Isolierung

**Schritt 2: Muster umsetzen**

**Vorlage Isolierung pro Test:**
```typescript
import { test, expect } from '@playwright/test'
import { createTestDatabase } from './helpers/database'

test.describe('My Feature', () => {
    test.beforeEach(async ({ page }) => {
        // Isolierte Datenbank anlegen
        const dbId = await createTestDatabase()

        // Routing-Cookie setzen
        await page.context().addCookies([{
            name: 'X-DB-Identifier',
            value: dbId,
            domain: 'localhost',
            path: '/',
            httpOnly: false,
            secure: false,
            sameSite: 'Lax',
        }])

        // Zur eigenen Seite navigieren
        await page.goto('/your/page')
    })

    test('should do something', async ({ page }) => {
        // Dein Testcode
        // Kein Aufräumen nötig!
    })
})
```

**Vorlage gemeinsame Datenbank pro Suite:**
```typescript
test.describe('My Feature Edit', () => {
    test.describe.configure({ mode: 'serial' })

    let sharedDbId: string
    let sharedDataId: string

    test.beforeAll(async ({ browser }) => {
        // Gemeinsame Datenbank anlegen
        sharedDbId = await createTestDatabase({ timeoutMs: 300000 })

        // Gemeinsame Testdaten anlegen
        const context = await browser.newContext({
            storageState: 'e2e/.auth/storageState.json'
        })
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

        // Testdaten anlegen und ID speichern
        await page.goto('/create')
        await page.fill('[name="name"]', 'Shared Data')
        await page.click('button:has-text("Save")')

        // ID aus URL oder Antwort extrahieren
        const url = page.url()
        sharedDataId = url.match(/\/edit\/(\d+)/)?.[1] || ''

        await context.close()
    })

    test.beforeEach(async ({ page }) => {
        // Gemeinsame Datenbank für jeden Test verwenden
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

    test('should edit name', async ({ page }) => {
        await page.goto(`/edit/${sharedDataId}`)
        // Gemeinsame Daten bearbeiten
    })
})
```

**Schritt 3: Ausführen und prüfen**

```bash
# Eigenen Test ausführen
pnpm playwright test my-feature.spec.ts --headed

# Datenbankisolierung prüfen
# 1. Prüfen, dass der Test Daten anlegt
# 2. Prüfen, dass die Daten in anderen Tests nicht sichtbar sind
# 3. Prüfen, dass kein manuelles Aufräumen nötig ist
```

### Datenbank-Routing debuggen

**Trace-Logging aktivieren:**

```java
// application-e2e-frontend-tests.properties
logging.level.de.jobst.resulter.springapp.config.DataSourceInterceptor=TRACE
logging.level.de.jobst.resulter.springapp.config.DataSourceManager=TRACE
logging.level.de.jobst.resulter.springapp.config.DynamicRoutingDataSource=TRACE
```

**Logs prüfen:**
```
DataSourceInterceptor - Request: POST /event
DataSourceInterceptor - X-DB-Identifier header: f8a1e027-fdd0-4257-b555-8fe2fa487198
DataSourceInterceptor - Routing to database: f8a1e027-fdd0-4257-b555-8fe2fa487198
DataSourceManager - Creating new DataSource for: f8a1e027...
DataSourceManager - Running Liquibase migrations...
DynamicRoutingDataSource - Using context DataSource for current request
```

**Debugging im Test ergänzen:**
```typescript
test('debug database routing', async ({ page }) => {
    const dbId = await createTestDatabase()
    console.log('Created database:', dbId)

    await page.context().addCookies([{
        name: 'X-DB-Identifier',
        value: dbId,
        domain: 'localhost',
        path: '/',
        httpOnly: false,
        secure: false,
        sameSite: 'Lax',
    }])

    // Cookies loggen
    const cookies = await page.context().cookies()
    console.log('Set cookies:', cookies.filter(c => c.name === 'X-DB-Identifier'))

    // Netzwerkanfragen loggen
    page.on('request', req => {
        const headers = req.headers()
        console.log('Request headers:', headers)
    })

    await page.goto('/event')
})
```

### Mitwirken

**Neue Backend-Komponenten hinzufügen:**

1. Mit `@Profile("e2e-frontend-tests")` annotieren
2. `DataSourceManager` injizieren, wenn Datenbankzugriff nötig ist
3. `DynamicRoutingDataSource` verwenden (bereits konfiguriert)
4. Tests zur Prüfung der Isolierung ergänzen

**Neue Frontend-Hilfsfunktionen hinzufügen:**

1. In `frontend/e2e/helpers/` ablegen
2. Aus `database.ts` exportieren oder ein neues Modul anlegen
3. TypeScript-Best-Practices befolgen
4. JSDoc-Kommentare ergänzen
5. Diese Dokumentation aktualisieren

### Die Implementierung testen

**Backend-Tests:**
```bash
cd backend

# Endpunkt zum Anlegen von Datenbanken testen
curl -X POST -H "Authorization: Bearer test-token" \
  http://localhost:8080/createDatabase

# Sollte zurückgeben: {"success":true,"data":{"identifier":"<uuid>"}}

# Datenbank-Routing testen
# 1. Datenbank anlegen (UUID erhalten)
# 2. Anfrage mit dem Cookie X-DB-Identifier senden
# 3. Prüfen, dass die Daten in der isolierten Datenbank landen
```

**Frontend-Tests:**
```bash
cd frontend

# Hilfsfunktionen testen
pnpm playwright test create_db.spec.ts --headed

# Vollständige Isolierung testen
pnpm playwright test event-form.spec.ts --headed

# Parallele Ausführung prüfen
pnpm playwright test event-form.spec.ts --workers=4
```

---

## Anhang

### Glossar

- **Datenbankisolierung**: Jeder Test erhält eine eigene PostgreSQL-Datenbank
- **DynamicRoutingDataSource**: Spring-Komponente, die Datenbankoperationen an isolierte Datenbanken weiterleitet
- **DataSourceManager**: Verwaltet den Lebenszyklus der isolierten DataSources
- **ThreadLocal**: Java-Mechanismus für anfragebezogene Datenspeicherung
- **X-DB-Identifier**: Cookie/Header mit der UUID der Datenbank
- **Liquibase**: Werkzeug für Datenbankmigrationen
- **Testcontainers**: Bibliothek zum Ausführen von Docker-Containern in Tests
- **HikariCP**: JDBC Connection Pool

### Referenzen

- [Testcontainers-Dokumentation](https://www.testcontainers.org/)
- [Spring AbstractRoutingDataSource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/datasource/lookup/AbstractRoutingDataSource.html)
- [Best Practices für Playwright](https://playwright.dev/docs/best-practices)
- [Liquibase-Dokumentation](https://docs.liquibase.com/)

### Änderungsprotokoll

**2025-12-28:** Erste Dokumentation
- Umfassender Leitfaden zur Datenbankisolierung
- Alle drei Nutzungsmuster dokumentiert
- Vollständige Architekturdokumentation
- Anleitung zur Fehlerbehebung
- Entwicklerleitfaden

---

*Zuletzt aktualisiert: 2025-12-28*
*Version: 1.0.0*
