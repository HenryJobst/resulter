# E2E Database Isolation - Complete Technical Documentation

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [How It Works](#how-it-works)
4. [Components](#components)
5. [Implementation Guide](#implementation-guide)
6. [Usage Patterns](#usage-patterns)
7. [Configuration](#configuration)
8. [Technical Details](#technical-details)
9. [Best Practices](#best-practices)
10. [Troubleshooting](#troubleshooting)
11. [Performance Considerations](#performance-considerations)
12. [Developer Guide](#developer-guide)

---

## Overview

### What is E2E Database Isolation?

E2E Database Isolation is a testing infrastructure that provides each Playwright E2E test with its own isolated PostgreSQL database. This eliminates test interference, enables safe parallel test execution, and removes the need for manual cleanup logic.

### Key Benefits

✅ **Complete Test Isolation** - Tests cannot contaminate each other's data
✅ **Zero Manual Cleanup** - Databases auto-delete after 30 seconds of inactivity
✅ **Parallel Execution Safe** - Each test has its own database
✅ **Simpler Test Code** - Removed 12+ cleanup lines per test
✅ **Fresh State** - Every test starts with a clean, migrated database
✅ **Flexible Granularity** - Choose per-test, suite-level, or no isolation

### The Problem It Solves

**Before Database Isolation:**
```typescript
test('create event', async ({ page }) => {
    const uniqueName = `Event ${Date.now()}`

    // Create event
    await page.fill('[name="name"]', uniqueName)
    await page.click('button:has-text("Save")')

    // Manual cleanup (12+ lines)
    await page.goto('/event')
    const row = page.locator(`tr:has-text("${uniqueName}")`)
    await row.locator('button[aria-label="Delete"]').click()
    await page.locator('button:has-text("Confirm")').click()
    await expect(row).not.toBeVisible()

    // Problem: If test fails before cleanup, data remains
    // Problem: Tests interfere with each other
    // Problem: Parallel execution causes race conditions
})
```

**After Database Isolation:**
```typescript
test('create event', async ({ page }) => {
    // Database created automatically in beforeEach

    // Create event
    await page.fill('[name="name"]', 'Test Event')
    await page.click('button:has-text("Save")')

    // That's it! No cleanup needed.
    // Database auto-deletes after 30 seconds.
})
```

---

## Architecture

### System Overview

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

### Data Flow

1. **Test Setup Phase:**
   ```
   Test → createTestDatabase() → POST /createDatabase
        ← UUID (e.g., "f8a1e027-...")
   ```

2. **Request Phase:**
   ```
   Browser → HTTP Request with Cookie: X-DB-Identifier=f8a1e027...
           → DataSourceInterceptor reads cookie
           → DataSourceManager creates/retrieves DataSource
           → ThreadLocal stores DataSource
   ```

3. **Database Operation Phase:**
   ```
   Spring Data JDBC → DynamicRoutingDataSource.determineTargetDataSource()
                    → Reads DataSource from ThreadLocal
                    → Routes query to test-db-f8a1e027...
   ```

4. **Cleanup Phase:**
   ```
   DataSourceCleanupScheduler (every 10s)
   → Check all DataSources
   → If lastAccessTime > 30s ago
   → Close DataSource + Drop database
   ```

---

## How It Works

### Cookie-Based Routing

**Why Cookies Instead of Headers?**

Headers would interfere with OAuth2/Keycloak authentication flow:
- Keycloak redirects would lose custom headers
- Multiple redirects in OAuth2 flow
- Cookies persist across redirects
- Cookies only sent to `localhost:8080` (backend), not to Keycloak

**Cookie Configuration:**
```typescript
await page.context().addCookies([{
    name: 'X-DB-Identifier',
    value: dbIdentifier,
    domain: 'localhost',
    path: '/',
    httpOnly: false,  // Accessible to JavaScript
    secure: false,    // HTTP (dev environment)
    sameSite: 'Lax',  // Sent with navigation
}])
```

### Database Lifecycle

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

## Components

### Backend Components

#### 1. TestContainersConfig.java
```java
@Configuration
@Profile("e2e-frontend-tests")
public class TestContainersConfig {
    private static final PostgreSQLContainer<?> postgresContainer = ...;

    static {
        postgresContainer.start(); // Start once, reuse for all tests
    }

    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("defaultDataSource") DataSource defaultDataSource) {
        return new DynamicRoutingDataSource(defaultDataSource);
    }
}
```

**Purpose:** Starts a single PostgreSQL container and configures dynamic routing

**Key Features:**
- Single container for all tests (performance)
- Each test gets its own database within the container
- DynamicRoutingDataSource as primary DataSource

#### 2. DynamicRoutingDataSource.java
```java
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected DataSource determineTargetDataSource() {
        DataSource contextDataSource = DataSourceContextHolder.getDataSource();

        if (contextDataSource != null) {
            return contextDataSource; // Use isolated database
        }

        return defaultDataSource; // Fallback to default
    }
}
```

**Purpose:** Routes database operations to the correct isolated database

**How It Works:**
- Spring calls `determineTargetDataSource()` for every DB operation
- Reads DataSource from ThreadLocal (set by interceptor)
- Returns isolated DataSource if present, otherwise default

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
        DataSourceContextHolder.clear(); // Clean ThreadLocal
    }
}
```

**Purpose:** Intercepts HTTP requests and sets up database routing

**Key Operations:**
1. Reads `X-DB-Identifier` from cookie or header
2. Gets or creates DataSource for that identifier
3. Stores in ThreadLocal for current request
4. Cleans up ThreadLocal after request completes

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

        // Create database in PostgreSQL container
        createDatabaseInContainer(dbName);

        // Create DataSource pointing to new database
        DataSource dataSource = DataSourceBuilder.create()
            .url(postgresContainer.getJdbcUrl().replace("/testdb", "/" + dbName))
            .username(postgresContainer.getUsername())
            .password(postgresContainer.getPassword())
            .build();

        // Run Liquibase migrations
        runLiquibaseMigrations(dataSource);

        return new DataSourceWrapper(dataSource);
    }
}
```

**Purpose:** Manages lifecycle of isolated DataSources

**Key Features:**
- Thread-safe DataSource creation (ConcurrentHashMap)
- Lazy creation (only when needed)
- Tracks last access time for cleanup
- Runs Liquibase migrations automatically

#### 5. DataSourceCleanupScheduler.java
```java
@Component
@Profile("e2e-frontend-tests")
public class DataSourceCleanupScheduler {

    @Scheduled(fixedDelay = 10000) // Every 10 seconds
    public void cleanupInactiveDatabases() {
        long now = System.currentTimeMillis();
        long inactivityThreshold = 30000; // 30 seconds

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

**Purpose:** Automatic cleanup of unused databases

**Behavior:**
- Runs every 10 seconds
- Checks all DataSources for inactivity >30 seconds
- Closes connections and drops database
- Removes from tracking map

#### 6. DatabaseController.java
```java
@RestController
@Profile("e2e-frontend-tests")
public class DatabaseController {

    @PostMapping("/createDatabase")
    @PreAuthorize("hasRole('CREATE_DATABASE')")
    public ResponseEntity<Map<String, String>> createDatabase() {
        String uuid = UUID.randomUUID().toString();

        // Pre-create the database
        dataSourceManager.getOrCreateDataSource(uuid);

        return ResponseEntity.ok(Map.of("identifier", uuid));
    }
}
```

**Purpose:** API endpoint for tests to request new databases

**Security:**
- Requires `ROLE_CREATE_DATABASE` (from API token)
- Only active in `e2e-frontend-tests` profile

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

**Purpose:** API token authentication for /createDatabase endpoint

**How It Works:**
- Checks Authorization header: `Bearer <token>`
- Alternative: `X-CREATE-DATABASE-TOKEN` header
- Validates against configured token
- Sets authentication with `ROLE_CREATE_DATABASE`

### Frontend Components

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

**Purpose:** Helper functions for test database creation and routing

**Features:**
- Creates database via API call
- Returns UUID identifier
- Provides Playwright-compatible cookie configuration
- Configurable timeout (default 2 minutes for container startup)

---

## Implementation Guide

### Step-by-Step Implementation

#### 1. Backend Setup

**Enable the Profile:**
```bash
cd backend
export SPRING_PROFILES_ACTIVE=e2e-frontend-tests
./mvnw spring-boot:run
```

**Or use the convenience script:**
```bash
./start-e2e-frontend-tests.sh
```

**Required Environment Variables:**
```bash
# .env file in project root
CREATEDATABASE_API_TOKEN=your-secret-token-here
RESULTER_MEDIA_FILE_PATH=/tmp/resulter-media
RESULTER_MEDIA_FILE_PATH_THUMBNAILS=/tmp/resulter-media/thumbnails
API_OAUTH2_RESOURCE_SERVER_JWT_ISSUER_URI=https://keycloak.example.com/realms/resulter
API_OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI=https://keycloak.example.com/realms/resulter/protocol/openid-connect/certs
# ... (see .env.example for full list)
```

#### 2. Frontend Setup

**Create Environment File:**
```bash
# frontend/e2e/.env.local
BACKEND_PROFILES=e2e-frontend-tests
CREATEDATABASE_API_TOKEN=your-secret-token-here
```

**Import Helper Functions:**
```typescript
import { createTestDatabase, useDatabaseIsolation } from './helpers/database'
```

#### 3. Write Tests with Database Isolation

**Pattern 1: Per-Test Isolation**
```typescript
import { test, expect } from '@playwright/test'
import { createTestDatabase } from './helpers/database'

test.describe('Event Creation', () => {
    test.beforeEach(async ({ page }) => {
        // Create fresh database for this test
        const dbId = await createTestDatabase()

        // Set cookie to route to isolated database
        await page.context().addCookies([{
            name: 'X-DB-Identifier',
            value: dbId,
            domain: 'localhost',
            path: '/',
            httpOnly: false,
            secure: false,
            sameSite: 'Lax',
        }])

        // Navigate to page - cookie is sent automatically
        await page.goto('/en/event/new')
    })

    test('should create event with all fields', async ({ page }) => {
        await page.fill('[name="name"]', 'Test Event')
        await page.fill('[name="date"]', '2027-12-31')
        await page.click('button:has-text("Save")')

        // Verify event created
        await expect(page).toHaveURL(/\/event$/)
        await expect(page.locator('text=Test Event')).toBeVisible()

        // NO CLEANUP NEEDED - database auto-deleted after 30s
    })
})
```

**Pattern 2: Suite-Level Sharing**
```typescript
test.describe('Event Editing', () => {
    let sharedDbId: string
    let eventName: string

    test.beforeAll(async ({ browser }) => {
        // Create database once for entire suite
        sharedDbId = await createTestDatabase({ timeoutMs: 300000 }) // 5 min timeout
        eventName = `Shared Event ${Date.now()}`

        // Create test data ONCE
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

        // Create event
        await page.goto('/en/event/new')
        await page.fill('[name="name"]', eventName)
        await page.click('button:has-text("Save")')

        await context.close()
    })

    test.beforeEach(async ({ page }) => {
        // Each test uses the same database
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
        // Uses same database, sees "Updated Name" from previous test
        // This is intentional for edit tests
    })
})
```

**Pattern 3: No Isolation (Read-Only)**
```typescript
test.describe('Form Validation', () => {
    // No database setup - uses default database

    test('should validate required fields', async ({ page }) => {
        await page.goto('/en/event/new')
        await page.click('button:has-text("Save")')

        // Check validation errors
        await expect(page.locator('text=Name is required')).toBeVisible()
    })
})
```

---

## Usage Patterns

### When to Use Each Pattern

| Pattern | Use Case | Example Tests | Database Lifetime |
|---------|----------|---------------|-------------------|
| **Per-Test Isolation** | Create/modify operations that need fresh state | Event creation, import results, user registration | 1 database per test (~30s) |
| **Suite-Level Sharing** | Edit/update operations on same entity | Edit event, add certificate, update settings | 1 database per suite (~5min) |
| **No Isolation** | Read-only validation, UI tests | Form validation, loading states, navigation | Uses default database |

### Pattern Decision Tree

```
Is your test creating new data?
├─ Yes → Does each test need isolated data?
│         ├─ Yes → Per-Test Isolation
│         └─ No → Suite-Level Sharing
└─ No → Does it modify existing data?
          ├─ Yes → Suite-Level Sharing
          └─ No → No Isolation
```

### Code Examples by Pattern

#### Per-Test Isolation Example
```typescript
// ✅ GOOD: Each test gets fresh database
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

#### Suite-Level Sharing Example
```typescript
// ✅ GOOD: All tests share one database and one event
test.describe('Event Editing', () => {
    test.describe.configure({ mode: 'serial' }) // Run tests sequentially

    let sharedDbId: string

    test.beforeAll(async ({ browser }) => {
        sharedDbId = await createTestDatabase({ timeoutMs: 300000 })
        // Create shared test event
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

#### No Isolation Example
```typescript
// ✅ GOOD: Read-only tests don't need isolation
test.describe('Form Validation', () => {
    // No beforeEach - no database isolation

    test('validate required fields', async ({ page }) => {
        await page.goto('/en/event/new')
        await page.click('button:has-text("Save")')
        await expect(page.locator('text=Name is required')).toBeVisible()
    })
})
```

---

## Configuration

### Backend Configuration

**File: `backend/src/main/resources/application-e2e-frontend-tests.properties`**

```properties
# Server Configuration
server.port=8080
cors.allowed-origins=http://localhost:5173,http://localhost:8080

# Disable Spring Docker Compose (we use Testcontainers directly)
spring.docker.compose.enabled=false
spring.docker.compose.lifecycle-management=none

# Security: Create Database API Token
security.createdatabase.api-token=${CREATEDATABASE_API_TOKEN}

# Media File Paths (required via environment variables)
resulter.media-file-path=${RESULTER_MEDIA_FILE_PATH}
resulter.media-file-path-thumbnails=${RESULTER_MEDIA_FILE_PATH_THUMBNAILS}

# OAuth2 Resource Server (required via environment variables)
spring.security.oauth2.resourceserver.jwt.issuer-uri=${API_OAUTH2_RESOURCE_SERVER_JWT_ISSUER_URI}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${API_OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI}

# OAuth2 Client BFF (required via environment variables)
spring.security.oauth2.client.registration.keycloak.client-id=${BFF_OAUTH2_CLIENT_ID}
spring.security.oauth2.client.registration.keycloak.client-secret=${BFF_OAUTH2_CLIENT_SECRET}
spring.security.oauth2.client.provider.keycloak.authorization-uri=${KEYCLOAK_AUTHORIZATION_URI}
spring.security.oauth2.client.provider.keycloak.token-uri=${KEYCLOAK_TOKEN_URI}
spring.security.oauth2.client.provider.keycloak.user-info-uri=${KEYCLOAK_USER_INFO_URI}
spring.security.oauth2.client.provider.keycloak.jwk-set-uri=${KEYCLOAK_JWK_SET_URI}

# Logging Configuration
logging.level.root=WARN
logging.level.de.jobst.resulter.springapp.config.DataSourceInterceptor=DEBUG
logging.level.de.jobst.resulter.springapp.config.DataSourceManager=DEBUG
```

### Frontend Configuration

**File: `frontend/e2e/.env.local`**

```env
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
USERNAME=test-user
PASSWORD=test-password

# API Tokens
CREATEDATABASE_API_TOKEN=your-secret-token-here
```

### Environment Variables

**Required Variables (must be set in `.env` file in project root):**

```bash
# OAuth2 Resource Server
API_OAUTH2_RESOURCE_SERVER_JWT_ISSUER_URI=https://keycloak.example.com/realms/resulter
API_OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI=https://keycloak.example.com/realms/resulter/protocol/openid-connect/certs

# BFF OAuth2 Client
BFF_OAUTH2_CLIENT_ID=resulter-bff
BFF_OAUTH2_CLIENT_SECRET=your-oauth2-secret

# Keycloak Provider URIs
KEYCLOAK_AUTHORIZATION_URI=https://keycloak.example.com/realms/resulter/protocol/openid-connect/auth
KEYCLOAK_TOKEN_URI=https://keycloak.example.com/realms/resulter/protocol/openid-connect/token
KEYCLOAK_USER_INFO_URI=https://keycloak.example.com/realms/resulter/protocol/openid-connect/userinfo
KEYCLOAK_JWK_SET_URI=https://keycloak.example.com/realms/resulter/protocol/openid-connect/certs

# Media File Paths
RESULTER_MEDIA_FILE_PATH=/tmp/resulter-media
RESULTER_MEDIA_FILE_PATH_THUMBNAILS=/tmp/resulter-media/thumbnails

# API Tokens (generate with: openssl rand -base64 32)
CREATEDATABASE_API_TOKEN=your-create-database-token
PROMETHEUS_API_TOKEN=your-prometheus-token
```

---

## Technical Details

### Threading Model

**ThreadLocal Storage:**
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
        contextHolder.remove(); // Prevent memory leaks
    }
}
```

**Why ThreadLocal?**
- Each HTTP request runs in its own thread
- ThreadLocal provides request-scoped storage
- No cross-request contamination
- Automatic isolation between parallel requests
- Must be cleared after request to prevent leaks

**Request Flow:**
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

**Each Isolated DataSource has its own HikariCP pool:**

```java
DataSource dataSource = DataSourceBuilder.create()
    .url(jdbcUrl)
    .username(username)
    .password(password)
    .type(HikariDataSource.class)
    .build();

// Default HikariCP configuration
// - maximumPoolSize: 10
// - minimumIdle: 10
// - connectionTimeout: 30000ms
// - idleTimeout: 600000ms (10 minutes)
```

**Resource Usage:**
- Default DB: 1 connection pool (10 connections)
- Per test DB: 1 connection pool (10 connections)
- With 5 parallel tests: ~60 connections total

**Connection Lifecycle:**
```
Create DB → Create DataSource → HikariCP Pool Created
         → Test runs → Connections used
         → Test ends → Connections idle
         → 30s timeout → Cleanup triggered
         → Pool shutdown → Database dropped
         → Connections closed
```

### Liquibase Integration

**Automatic Migration on Database Creation:**

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

**What Gets Migrated:**
- All schema definitions (tables, indexes, constraints)
- All seed data (countries, organisations, initial cups)
- All functions and stored procedures

**Migration Performance:**
- First database creation: ~2-5 seconds (Liquibase migrations)
- Subsequent database creations: ~2-5 seconds (same migrations)
- Migrations run sequentially (Liquibase lock mechanism)

### Database Cleanup Mechanism

**Cleanup Scheduler Configuration:**

```java
@Scheduled(fixedDelay = 10000, initialDelay = 10000)
public void cleanupInactiveDatabases() {
    long now = System.currentTimeMillis();
    long inactivityThreshold = 30000; // 30 seconds

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
        // 1. Close all connections in pool
        if (wrapper.getDataSource() instanceof HikariDataSource hikari) {
            hikari.close();
        }

        // 2. Drop database using admin connection
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

**Cleanup Timing:**
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

### Test Organization

**✅ DO: Group by isolation strategy**
```typescript
// Suite 1: Create tests (per-test isolation)
test.describe('Event Creation', () => {
    test.beforeEach(async ({ page }) => {
        const dbId = await createTestDatabase()
        // ...
    })

    test('test 1', ...)
    test('test 2', ...)
})

// Suite 2: Edit tests (suite-level sharing)
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

**❌ DON'T: Mix isolation strategies in one suite**
```typescript
// BAD: Inconsistent isolation
test.describe('Event Tests', () => {
    test('create event', async ({ page }) => {
        const dbId = await createTestDatabase() // Per-test
        // ...
    })

    test('edit event', async ({ page }) => {
        // No database isolation - uses default DB
        // This won't see the event created above!
    })
})
```

### Database Lifecycle Management

**✅ DO: Use appropriate timeouts**
```typescript
// Per-test: Default 2-minute timeout
const dbId = await createTestDatabase()

// Suite-level: Longer timeout for multiple tests
const dbId = await createTestDatabase({ timeoutMs: 300000 }) // 5 minutes
```

**✅ DO: Clean up long-running test data**
```typescript
test.afterAll(async ({ browser }) => {
    // Optional: Explicitly trigger cleanup for long-running suites
    // (Not usually needed - auto-cleanup after 30s)
})
```

**❌ DON'T: Store database IDs globally**
```typescript
// BAD: Global state
let globalDbId: string

test.describe('Suite 1', () => {
    test.beforeAll(async () => {
        globalDbId = await createTestDatabase() // Shared across suites!
    })
})

test.describe('Suite 2', () => {
    test('test', async ({ page }) => {
        // Uses same globalDbId - NOT isolated!
    })
})
```

### Cookie Management

**✅ DO: Set cookie before navigation**
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

await page.goto('/en/event/new') // Cookie sent automatically
```

**❌ DON'T: Set cookie after navigation**
```typescript
await page.goto('/en/event/new') // Uses default DB!
await page.context().addCookies([...]) // Too late
```

### Parallel Execution

**✅ DO: Enable parallel execution for isolated tests**
```typescript
// playwright.config.ts
export default defineConfig({
    workers: 4, // Safe with database isolation
    fullyParallel: true,
})
```

**✅ DO: Use serial mode for suite-sharing**
```typescript
test.describe('Event Editing', () => {
    test.describe.configure({ mode: 'serial' })
    // Tests run one after another, sharing database
})
```

**❌ DON'T: Run suite-sharing tests in parallel**
```typescript
// BAD: Tests will interfere
test.describe('Event Editing', () => {
    // fullyParallel: true (default)
    // Tests may read/modify same data simultaneously
})
```

### Error Handling

**✅ DO: Handle database creation failures**
```typescript
test.beforeEach(async ({ page }) => {
    try {
        const dbId = await createTestDatabase()
        await page.context().addCookies([...])
    } catch (error) {
        console.error('Failed to create test database:', error)
        throw error // Fail test early
    }
})
```

**✅ DO: Check backend is running**
```bash
# In CI/CD pipeline
curl -f http://localhost:8080/actuator/health || exit 1
pnpm test:e2e
```

---

## Troubleshooting

### Database Creation Fails

**Error:** `Failed to create test database: Connection refused`

**Cause:** Backend not running or not in `e2e-frontend-tests` profile

**Solution:**
```bash
# Check backend is running
curl http://localhost:8080/actuator/health

# Check active profile
curl http://localhost:8080/actuator/env | grep -i "activeProfiles"

# Should show: "e2e-frontend-tests"

# If not, restart backend with correct profile
cd backend
./start-e2e-frontend-tests.sh
```

### Wrong Database Used

**Error:** Test sees data from other tests or default database

**Cause:** Cookie not set or set incorrectly

**Solution:**
```typescript
// Verify cookie is set BEFORE navigation
await page.context().addCookies([{
    name: 'X-DB-Identifier',
    value: dbId,
    domain: 'localhost', // Must be 'localhost', not '127.0.0.1'
    path: '/',
    httpOnly: false,
    secure: false,
    sameSite: 'Lax',
}])

// Add debug logging
const cookies = await page.context().cookies()
console.log('Cookies:', cookies.filter(c => c.name === 'X-DB-Identifier'))
```

**Backend Verification:**
```bash
# Check backend logs for database routing
# Should see:
# "X-DB-Identifier header: <uuid>"
# "Routing to database: <uuid>"
# "DataSource found and set for: <uuid>"
```

### Database Not Cleaned Up

**Error:** Too many databases, memory issues

**Cause:** Cleanup scheduler not running or databases still active

**Solution:**
```bash
# Check backend logs for cleanup messages
# Should see every 10 seconds:
# "Checking for inactive databases..."
# "Cleaning up inactive database: <uuid>"

# If not cleaning up:
# 1. Check profile is active
curl http://localhost:8080/actuator/env | grep "e2e-frontend-tests"

# 2. Check scheduler is enabled
curl http://localhost:8080/actuator/scheduledtasks

# 3. Manually trigger cleanup (if needed)
# Wait 30 seconds after test completes
# Scheduler should auto-clean
```

### Liquibase Migration Fails

**Error:** `Failed to run Liquibase migrations`

**Cause:** Database schema conflicts or migration errors

**Solution:**
```bash
# Check Liquibase changelog
cat backend/src/main/resources/db/changelog/db.changelog-master.yaml

# Test migrations on default database
cd backend
./mvnw liquibase:update -Dspring.profiles.active=e2e-frontend-tests

# Check for conflicts
./mvnw liquibase:status -Dspring.profiles.active=e2e-frontend-tests
```

### Performance Issues

**Error:** Tests slow, database creation takes >10 seconds

**Cause:** Docker/Testcontainers performance

**Solutions:**

1. **Optimize Docker:**
```bash
# Increase Docker resources
# Docker Desktop → Settings → Resources
# - CPUs: 4+
# - Memory: 8GB+
# - Swap: 2GB+
```

2. **Use Suite-Level Sharing:**
```typescript
// Instead of per-test (7 database creations)
test.beforeEach(async ({ page }) => {
    const dbId = await createTestDatabase() // Slow
})

// Use suite-level (1 database creation)
test.beforeAll(async ({ browser }) => {
    sharedDbId = await createTestDatabase() // Fast
})
```

3. **Cache Liquibase:**
```java
// Liquibase migrations are already optimized
// But you can skip certain changesets in test profile
<changeSet id="heavy-migration" context="!test">
    ...
</changeSet>
```

### API Token Issues

**Error:** `403 Forbidden` or `401 Unauthorized` when creating database

**Cause:** Missing or invalid `CREATEDATABASE_API_TOKEN`

**Solution:**
```bash
# 1. Generate new token
openssl rand -base64 32

# 2. Set in .env file (project root)
CREATEDATABASE_API_TOKEN=<generated-token>

# 3. Set in frontend/e2e/.env.local
CREATEDATABASE_API_TOKEN=<same-token>

# 4. Restart backend
cd backend
./start-e2e-frontend-tests.sh

# 5. Verify token is loaded
curl -H "Authorization: Bearer <token>" -X POST http://localhost:8080/createDatabase
# Should return: {"success":true,"data":{"identifier":"<uuid>"}}
```

---

## Performance Considerations

### Database Creation Performance

**Benchmarks (on MacBook Pro M1):**
- First database creation: ~2-5 seconds
- Subsequent creations: ~2-5 seconds
- Liquibase migrations: ~1-2 seconds
- Total per test overhead: ~3-7 seconds

**Optimization Strategies:**

1. **Use Suite-Level Sharing:**
   - 10 tests with per-test isolation: 10 × 5s = 50s overhead
   - 10 tests with suite-level sharing: 1 × 5s = 5s overhead
   - **Savings: 45 seconds (90%)**

2. **Reduce Migration Complexity:**
   ```xml
   <!-- Skip heavy seed data in test databases -->
   <changeSet id="seed-data" context="!test">
       <sqlFile path="seed-data.sql"/>
   </changeSet>
   ```

3. **Parallel Execution:**
   - 4 workers with isolated databases: No speedup (DB creation is sequential)
   - 4 workers with suite-level sharing: 4× speedup on test execution
   - **Best:** Mix of suite-level sharing + parallel execution

### Memory Usage

**Per Database Memory:**
- DataSource object: ~1 KB
- HikariCP connection pool: ~10 MB (10 connections)
- PostgreSQL database: ~10-50 MB (depends on data)
- Total per database: ~20-60 MB

**With 10 Parallel Tests:**
- 10 databases × 50 MB = 500 MB
- Plus default database: 50 MB
- **Total: ~550 MB**

**Memory Optimization:**
```java
// Reduce connection pool size for test databases
@Bean
public DataSource testDataSource(String dbIdentifier) {
    HikariConfig config = new HikariConfig();
    config.setMaximumPoolSize(5); // Reduced from 10
    config.setMinimumIdle(2);     // Reduced from 10
    return new HikariDataSource(config);
}
```

### Cleanup Performance

**Cleanup Timing:**
- Check interval: 10 seconds
- Inactivity threshold: 30 seconds
- Average cleanup time: 30-40 seconds after last access

**Immediate Cleanup (Not Recommended):**
```typescript
// Manual cleanup (not needed, just for reference)
test.afterEach(async () => {
    // Wait for scheduler to clean up
    await new Promise(resolve => setTimeout(resolve, 35000))
})
```

### Test Suite Performance

**Example: 22 Tests with Database Isolation**

**Before Optimization:**
- Per-test isolation for all tests: 22 × 5s = 110s overhead
- Actual test execution: 30s
- **Total: 140 seconds**

**After Optimization:**
- 8 Create tests (per-test): 8 × 5s = 40s
- 10 Edit tests (suite-level): 1 × 5s = 5s
- 4 Validation tests (no isolation): 0s
- Actual test execution: 30s
- **Total: 75 seconds (46% faster)**

---

## Developer Guide

### Adding a New Test with Database Isolation

**Step 1: Determine Isolation Strategy**

Ask yourself:
- Does this test create new data? → Per-test isolation
- Does this test modify existing data? → Suite-level sharing
- Does this test only validate/read? → No isolation

**Step 2: Implement the Pattern**

**Per-Test Isolation Template:**
```typescript
import { test, expect } from '@playwright/test'
import { createTestDatabase } from './helpers/database'

test.describe('My Feature', () => {
    test.beforeEach(async ({ page }) => {
        // Create isolated database
        const dbId = await createTestDatabase()

        // Set routing cookie
        await page.context().addCookies([{
            name: 'X-DB-Identifier',
            value: dbId,
            domain: 'localhost',
            path: '/',
            httpOnly: false,
            secure: false,
            sameSite: 'Lax',
        }])

        // Navigate to your page
        await page.goto('/your/page')
    })

    test('should do something', async ({ page }) => {
        // Your test code
        // No cleanup needed!
    })
})
```

**Suite-Level Sharing Template:**
```typescript
test.describe('My Feature Edit', () => {
    test.describe.configure({ mode: 'serial' })

    let sharedDbId: string
    let sharedDataId: string

    test.beforeAll(async ({ browser }) => {
        // Create shared database
        sharedDbId = await createTestDatabase({ timeoutMs: 300000 })

        // Create shared test data
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

        // Create test data and store ID
        await page.goto('/create')
        await page.fill('[name="name"]', 'Shared Data')
        await page.click('button:has-text("Save")')

        // Extract ID from URL or response
        const url = page.url()
        sharedDataId = url.match(/\/edit\/(\d+)/)?.[1] || ''

        await context.close()
    })

    test.beforeEach(async ({ page }) => {
        // Use shared database for each test
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
        // Edit shared data
    })
})
```

**Step 3: Run and Verify**

```bash
# Run your test
pnpm playwright test my-feature.spec.ts --headed

# Verify database isolation
# 1. Check test creates data
# 2. Check data not visible in other tests
# 3. Check no manual cleanup needed
```

### Debugging Database Routing

**Enable Trace Logging:**

```java
// application-e2e-frontend-tests.properties
logging.level.de.jobst.resulter.springapp.config.DataSourceInterceptor=TRACE
logging.level.de.jobst.resulter.springapp.config.DataSourceManager=TRACE
logging.level.de.jobst.resulter.springapp.config.DynamicRoutingDataSource=TRACE
```

**Check Logs:**
```
DataSourceInterceptor - Request: POST /event
DataSourceInterceptor - X-DB-Identifier header: f8a1e027-fdd0-4257-b555-8fe2fa487198
DataSourceInterceptor - Routing to database: f8a1e027-fdd0-4257-b555-8fe2fa487198
DataSourceManager - Creating new DataSource for: f8a1e027...
DataSourceManager - Running Liquibase migrations...
DynamicRoutingDataSource - Using context DataSource for current request
```

**Add Test Debugging:**
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

    // Log cookies
    const cookies = await page.context().cookies()
    console.log('Set cookies:', cookies.filter(c => c.name === 'X-DB-Identifier'))

    // Log network requests
    page.on('request', req => {
        const headers = req.headers()
        console.log('Request headers:', headers)
    })

    await page.goto('/event')
})
```

### Contributing

**Adding New Backend Components:**

1. Annotate with `@Profile("e2e-frontend-tests")`
2. Inject `DataSourceManager` if you need database access
3. Use `DynamicRoutingDataSource` (already configured)
4. Add tests to verify isolation

**Adding New Frontend Helpers:**

1. Place in `frontend/e2e/helpers/`
2. Export from `database.ts` or create new module
3. Follow TypeScript best practices
4. Add JSDoc comments
5. Update this documentation

### Testing the Implementation

**Backend Tests:**
```bash
cd backend

# Test database creation endpoint
curl -X POST -H "Authorization: Bearer test-token" \
  http://localhost:8080/createDatabase

# Should return: {"success":true,"data":{"identifier":"<uuid>"}}

# Test database routing
# 1. Create database (get UUID)
# 2. Make request with X-DB-Identifier cookie
# 3. Verify data goes to isolated database
```

**Frontend Tests:**
```bash
cd frontend

# Test helper functions
pnpm playwright test create_db.spec.ts --headed

# Test full isolation
pnpm playwright test event-form.spec.ts --headed

# Verify parallel execution
pnpm playwright test event-form.spec.ts --workers=4
```

---

## Appendix

### Glossary

- **Database Isolation**: Each test gets its own PostgreSQL database
- **DynamicRoutingDataSource**: Spring component that routes DB operations to isolated databases
- **DataSourceManager**: Manages lifecycle of isolated DataSources
- **ThreadLocal**: Java mechanism for request-scoped data storage
- **X-DB-Identifier**: Cookie/header containing database UUID
- **Liquibase**: Database migration tool
- **Testcontainers**: Library for running Docker containers in tests
- **HikariCP**: JDBC connection pool

### References

- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Spring AbstractRoutingDataSource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/datasource/lookup/AbstractRoutingDataSource.html)
- [Playwright Best Practices](https://playwright.dev/docs/best-practices)
- [Liquibase Documentation](https://docs.liquibase.com/)

### Change Log

**2025-12-28:** Initial documentation
- Comprehensive guide for database isolation
- All three usage patterns documented
- Complete architecture documentation
- Troubleshooting guide
- Developer guide

---

*Last Updated: 2025-12-28*
*Version: 1.0.0*
