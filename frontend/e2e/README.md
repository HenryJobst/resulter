# E2E Tests with Playwright

This directory contains end-to-end tests for the Resulter frontend application using Playwright.

## Overview

### Test Files

- **`auth-setup.ts`** - BFF authentication setup (runs before other tests, establishes session cookies)
- **`main.spec.ts`** - Main application flow tests
- **`create_db.spec.ts`** - Database creation tests
- **`event.spec.ts`** - Basic event CRUD operations
- **`event-form.spec.ts`** ⭐ **NEW** - Comprehensive EventForm.vue tests

## Authentication (BFF Pattern)

The E2E tests use the Backend-for-Frontend (BFF) authentication pattern:

1. **`auth-setup.ts`** initiates OAuth2 flow by navigating to `/oauth2/authorization/keycloak`
2. Backend redirects to Keycloak login page
3. Test fills in credentials and submits
4. Keycloak redirects back to backend with authorization code
5. Backend exchanges code for tokens and establishes a session (HTTP-only cookie)
6. Backend redirects to frontend
7. Playwright saves the storage state including session cookies
8. All subsequent tests use the saved session cookies for authentication

**Important:** Session cookies are saved in `e2e/.auth/storageState.json` and are valid for 10 minutes. After that, `auth-setup.ts` will re-authenticate automatically.

## Database Isolation

E2E tests use **isolated test databases** to prevent interference between tests. Each test can choose its isolation strategy based on its needs.

### How It Works

1. **Backend:** The `testcontainers` profile creates isolated PostgreSQL databases on-demand
2. **Cookie-based Routing:** Tests set an `X-DB-Identifier` cookie to route requests to their isolated database
3. **Auto-cleanup:** Databases are automatically cleaned up after 30 seconds of inactivity
4. **Liquibase:** Schema migrations run automatically when databases are created

**Why cookies instead of headers?**
- Headers would interfere with Keycloak OAuth2 authentication
- Cookies are only sent to `localhost:8080` (backend), not to Keycloak
- Simple and reliable with Playwright's `addCookies()` API

### Three Isolation Patterns

#### 1️⃣ Per-Test Isolation (Recommended for Create/Modify Tests)

Each test gets a fresh database. Best for tests that create or modify data.

```typescript
import { createTestDatabase } from './helpers/database'

test.describe('My Test Suite', () => {
    test.beforeEach(async ({ page }) => {
        // Create fresh database for each test
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

        // Now all requests use the isolated database
        await page.goto('/en/event/new')
    })

    test('should create event', async ({ page }) => {
        await page.getByLabel('Name').fill('Test Event')
        await page.getByLabel('Save').click()

        // No cleanup needed - database auto-deleted after 30s!
    })
})
```

**Example:** `event-form.spec.ts` - Create Event suite

#### 2️⃣ Suite-Level Isolation (Shared Database)

All tests in a suite share one database. Best for edit/update tests that need existing data.

```typescript
test.describe('Edit Tests', () => {
    let sharedDbId: string
    let eventName: string

    test.beforeAll(async () => {
        // Create database once for entire suite (5min timeout)
        sharedDbId = await createTestDatabase({ timeoutMs: 300000 })
        eventName = `Shared Event ${Date.now()}`
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

        // Create shared event if it doesn't exist
        await page.goto('/en/event')
        const exists = await page.getByRole('row')
            .filter({ hasText: eventName })
            .isVisible()
            .catch(() => false)

        if (!exists) {
            // Create event (first test only)
        }
    })

    test('edit event name', async ({ page }) => {
        // Edits the shared event
    })

    test('edit event date', async ({ page }) => {
        // Also edits the shared event
    })
})
```

**Example:** `event-form.spec.ts` - Edit Event suite

#### 3️⃣ No Isolation (Read-Only Tests)

Tests that only read data don't need isolation. Use the standard database.

```typescript
test.describe('Form Validation', () => {
    // No database isolation setup

    test('should validate required fields', async ({ page }) => {
        await page.goto('/en/event/new')
        await page.getByLabel('Save').click()

        // Check validation errors (no data created)
    })
})
```

**Example:** `event-form.spec.ts` - Form Validation & Loading States suites

### Configuration

**Backend** (`testcontainers` profile must be active):
```bash
# .env file
CREATEDATABASE_API_TOKEN=test-database-token

# Start backend with testcontainers profile
cd backend
SPRING_PROFILES_ACTIVE=testcontainers ./mvnw spring-boot:run
```

**Frontend** (`e2e/.env.local`):
```env
BACKEND_PROFILES=testcontainers
CREATEDATABASE_API_TOKEN=test-database-token
```

### Benefits

✅ **No test interference** - Tests can't contaminate each other's data
✅ **No manual cleanup** - Databases auto-deleted after 30 seconds
✅ **Parallel execution safe** - Each test has its own database
✅ **Simpler test code** - No cleanup logic needed (12+ lines removed per test)
✅ **Fresh state** - Every test starts with a clean database

### Troubleshooting

**Database creation fails:**
- Check backend is running with `testcontainers` profile
- Verify Docker is running (testcontainers needs Docker)
- Check `CREATEDATABASE_API_TOKEN` is set in both `.env` and `e2e/.env.local`

**Tests timeout:**
- Database creation takes 5-10 seconds (Docker startup)
- Use longer timeout for suite-level: `createTestDatabase({ timeoutMs: 300000 })`

**OAuth2 fails:**
- Cookie-based isolation doesn't interfere with Keycloak
- If you see `auth_error=oauth2_failed`, check backend `.env` has correct `BFF_OAUTH2_CLIENT_SECRET`

## EventForm E2E Tests

The `event-form.spec.ts` file contains comprehensive tests for the EventForm component, which is too complex for unit testing due to tight PrimeVue integration and browser API dependencies.

### Test Coverage

#### 1. Create Event Tests (8 tests)
- ✅ Render all form fields (name, date, time, state, organisations, certificate)
- ✅ Create event with all fields filled
- ✅ Create event with minimal required fields only
- ✅ Navigate back without saving
- ✅ Date selection via calendar picker
- ✅ Time selection via time picker
- ✅ Select multiple organisations (multi-select)
- ✅ Change event state/status

#### 2. Edit Event Tests (4 tests)
- ✅ Edit event name
- ✅ Edit event date and time
- ✅ Add organisation to existing event
- ✅ Add certificate to existing event

#### 3. Form Validation Tests (3 tests)
- ✅ Handle empty form submission
- ✅ Validate date format
- ✅ Clear form when navigating away and back

#### 4. Loading States Tests (1 test)
- ✅ Show loading indicators when fetching data

**Total: 16 comprehensive E2E tests**

## Prerequisites

1. **Environment Configuration**
   - Create `e2e/.env.local` file (copy from `e2e/.env.local.example` if available)
   - Configure the following variables:
     ```env
     HOSTNAME=localhost
     FRONTEND_PROTOCOL=http
     PORT=5173
     BACKEND_PROTOCOL=http
     BACKEND_PORT=8080
     BACKEND_PROFILES=dev
     VITE_MODE=development
     USERNAME=<your-test-username>
     PASSWORD=<your-test-password>
     ```

2. **Servers Running** ⚠️ **IMPORTANT**

   **You must start both servers manually before running E2E tests:**

   ```bash
   # Terminal 1: Start frontend dev server
   pnpm dev

   # Terminal 2: Start backend server
   cd ../backend
   ./mvnw spring-boot:run

   # Terminal 3: Run E2E tests (after both servers are ready)
   pnpm test:e2e
   ```

   **Why manual start?**
   - More reliable than automatic startup
   - Easier to debug server issues
   - Faster test execution (servers stay running)
   - Better control over server state

3. **Test Data**
   - Some tests require existing organisations and certificates in the database
   - Create test data manually or use `create_db.spec.ts`

## Running Tests

### Run all E2E tests
```bash
pnpm test:e2e
```

### Run only EventForm tests
```bash
pnpm playwright test event-form.spec.ts
```

### Run in headed mode (see browser)
```bash
pnpm playwright test event-form.spec.ts --headed
```

### Run in debug mode
```bash
pnpm playwright test event-form.spec.ts --debug
```

### Run specific test
```bash
pnpm playwright test event-form.spec.ts -g "should create event with all fields"
```

### Run on specific browser
```bash
pnpm playwright test event-form.spec.ts --project=chromium
pnpm playwright test event-form.spec.ts --project=firefox
pnpm playwright test event-form.spec.ts --project=webkit
```

## View Test Results

### HTML Report
```bash
pnpm playwright show-report
```

### Trace Viewer (for failed tests)
```bash
pnpm playwright show-trace
```

## Test Structure

Each test follows this pattern:

```typescript
import { createTestDatabase } from './helpers/database'

test.describe('My Tests', () => {
    test.beforeEach(async ({ page }) => {
        // 1. Setup database isolation (for tests that create/modify data)
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

        // 2. Navigate to page
        await page.goto('/en/event/new')
    })

    test('test description', async ({ page }) => {
        // 3. Interact with elements
        await page.getByLabel('Name').fill('Test Event')

        // 4. Submit form
        await page.getByLabel('Save').click()

        // 5. Verify results
        await expect(page).toHaveURL(/\/event$/)

        // No cleanup needed - isolated database auto-deleted!
    })
})
```

## Best Practices

### ✅ DO
- **Use database isolation** for tests that create/modify data (per-test or suite-level)
- Use `getByRole`, `getByLabel`, `getByText` for better accessibility
- Use unique names for test data (include `Date.now()` for uniqueness)
- Wait for elements with `await expect(...).toBeVisible()`
- Use `test.beforeEach` for common setup (including database isolation)
- Choose the right isolation pattern: per-test for creates, suite-level for edits, none for read-only

### ❌ DON'T
- **Don't write manual cleanup code** - database isolation handles it automatically
- Don't use CSS selectors when semantic selectors are available
- Don't hard-code wait times (`page.waitForTimeout()`) - use `expect` assertions
- Don't rely on test execution order (isolation makes tests independent)
- Don't forget to set the database cookie if your test creates/modifies data

## Debugging Tips

### 1. Slow down test execution
```typescript
test.use({ slowMo: 500 }) // 500ms delay between actions
```

### 2. Take screenshots
```typescript
await page.screenshot({ path: 'screenshot.png' })
```

### 3. Pause execution
```typescript
await page.pause() // Opens Playwright Inspector
```

### 4. Console logs
```typescript
page.on('console', msg => console.log(msg.text()))
```

### 5. Network requests
```typescript
page.on('request', request => console.log('>>', request.method(), request.url()))
page.on('response', response => console.log('<<', response.status(), response.url()))
```

## CI/CD Integration

Tests are configured to run on CI with:
- Retry on failure (2 retries)
- Single worker (no parallelization)
- Automatic server startup
- HTML report generation

## Maintenance

### Update Selectors
If PrimeVue components change, update selectors in tests:
- Check Playwright Inspector for current selectors
- Use `data-testid` attributes for stability

### Add New Tests
When adding new EventForm features:
1. Add corresponding E2E test
2. Follow existing test structure
3. Include cleanup logic
4. Update this README

## Troubleshooting

### Test fails: "Element not found"
- Check if selector changed in UI
- Ensure element is visible before interaction
- Check for loading states

### Test fails: "Timeout"
- Increase timeout in `playwright.config.ts`
- Check if backend is running
- Verify network requests complete

### Authentication fails
- Check `e2e/.auth/storageState.json` exists
- Re-run `auth-setup.ts` manually: `pnpm playwright test auth-setup.ts`
- Verify backend BFF endpoint is accessible: `http://localhost:8080/oauth2/authorization/keycloak`
- Verify Keycloak configuration
- Check that backend is running and session cookies are being set
- Ensure `BACKEND_PROTOCOL`, `BACKEND_PORT`, and `HOSTNAME` are correctly configured in `e2e/.env.local`

## Known Issues

### Create Event Tests Failing in Parallel Execution

**Status:** Under Investigation

**Symptom:** Create Event tests (6 tests total) pass individually but fail when run in parallel with `--workers=4`.

**Tests Affected:**
- `event-form.spec.ts`: All Create Event suite tests
- `event.spec.ts`: create test event

**Current Test Status:** 16/22 tests passing (73%)
- ✅ All Edit Event tests passing (database isolation confirmed working)
- ✅ Form Validation tests passing
- ✅ Other tests passing
- ❌ Create Event tests failing in parallel only

**Observed Behavior:** Event list shows events from default database instead of isolated database after event creation.

**Workaround:**
- Run tests sequentially: `pnpm playwright test --workers=1`
- Run Create Event tests individually: `pnpm playwright test event-form.spec.ts --grep="Create Event"`

**Investigation Notes:**
- Database isolation is confirmed working (Edit Event tests prove this)
- Issue appears to be timing/caching related during parallel execution
- X-DB-Identifier cookie/header may not be properly forwarded for GET requests after CREATE in parallel scenarios

## Related Documentation

- [Playwright Documentation](https://playwright.dev)
- [PrimeVue Documentation](https://primevue.org)

## Questions?

For questions about E2E tests, contact the development team or open an issue.
