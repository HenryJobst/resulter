# E2E Tests with Playwright

End-to-end tests for the Resulter frontend application with **database isolation** for reliable, parallel test execution.

## 📚 Documentation

- **[E2E_DATABASE_ISOLATION.md](./E2E_DATABASE_ISOLATION.md)** - Complete technical guide to database isolation (architecture, implementation, best practices)
- **[TROUBLESHOOTING.md](./TROUBLESHOOTING.md)** - Detailed troubleshooting guide for common issues
- **This README** - Quick start and overview

---

## 🚀 Quick Start

### 1. Prerequisites

**Environment Setup:**
```bash
# Create e2e/.env.local
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

**Ensure Docker is running** (required for database isolation):
```bash
docker ps  # Should show running containers
```

### 2. Start Servers

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

### 3. Run Tests

**Terminal 3 - E2E Tests:**
```bash
# Run all tests
pnpm test:e2e

# Run specific test file
pnpm playwright test event-form.spec.ts

# Run in headed mode (see browser)
pnpm playwright test event-form.spec.ts --headed

# Debug mode (step through)
pnpm playwright test event-form.spec.ts --debug
```

---

## 📋 Overview

### Test Files

| File | Description | Database Isolation |
|------|-------------|-------------------|
| `auth-setup.ts` | BFF authentication setup | None |
| `main.spec.ts` | Main application flow | None |
| `create_db.spec.ts` | Database creation demo | Per-test |
| `event.spec.ts` | Basic event CRUD | Per-test |
| `event-form.spec.ts` | Comprehensive form tests | Mixed (per-test + suite-level) |

### Test Statistics

**Total:** 85 tests across 4 browsers
**Passing:** 67/85 (79%)

| Browser | Status | Passing | Notes |
|---------|--------|---------|-------|
| Chromium | ✅ | 22/22 | All tests passing |
| Firefox | ✅ | 21/22 | 1 minor failure |
| Edge | ✅ | 22/22 | All tests passing |
| Webkit | ⚠️ | 7/22 | Browser-specific issues |

---

## 🔒 Authentication (BFF Pattern)

E2E tests use Backend-for-Frontend (BFF) OAuth2 authentication:

1. `auth-setup.ts` initiates OAuth2 flow → `/oauth2/authorization/keycloak`
2. Backend redirects to Keycloak login page
3. Test fills credentials and submits
4. Keycloak redirects back with authorization code
5. Backend exchanges code for tokens → session cookie (HTTP-only)
6. Backend redirects to frontend
7. Playwright saves session in `e2e/.auth/storageState.json`
8. All tests reuse the session (valid for 10 minutes)

**Important:** Session auto-refreshes if <10 minutes old. Delete `e2e/.auth/storageState.json` to force re-authentication.

---

## 💾 Database Isolation

Each test can run in its own isolated PostgreSQL database, preventing data contamination and enabling safe parallel execution.

### How It Works (Quick Version)

1. **Test requests database:** `createTestDatabase()` → Returns UUID
2. **Test sets cookie:** `X-DB-Identifier: <uuid>`
3. **Backend routes requests:** Cookie → Isolated database
4. **Auto-cleanup:** Database deleted after 30 seconds of inactivity

**For complete details, see:** [E2E_DATABASE_ISOLATION.md](./E2E_DATABASE_ISOLATION.md)

### Three Isolation Patterns

#### 🔹 Per-Test Isolation
**Use for:** Create/modify operations that need fresh state

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

#### 🔹 Suite-Level Sharing
**Use for:** Edit/update operations on same entity

```typescript
let sharedDbId: string

test.beforeAll(async ({ browser }) => {
    sharedDbId = await createTestDatabase({ timeoutMs: 300000 }) // 5 min

    // Create shared test data once
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

    // Create event
    await page.goto('/en/event/new')
    await page.fill('[name="name"]', 'Shared Event')
    await page.click('button:has-text("Save")')

    await context.close()
})

test.beforeEach(async ({ page }) => {
    // All tests use same database
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

#### 🔹 No Isolation
**Use for:** Read-only validation/UI tests

```typescript
test('validate form', async ({ page }) => {
    await page.goto('/en/event/new')
    await page.click('button:has-text("Save")')

    await expect(page.locator('text=Name is required')).toBeVisible()
})
```

### Benefits

✅ **No test interference** - Complete data isolation
✅ **No manual cleanup** - Auto-deleted after 30s
✅ **Parallel execution safe** - Each test has own database
✅ **Simpler code** - Removed 12+ cleanup lines per test
✅ **Fresh state** - Clean database every time

---

## 🧪 EventForm E2E Tests

Comprehensive tests for `EventForm.vue` component (16 tests total):

### Test Coverage

**Create Event Tests (8 tests)** - Per-test isolation
- Render all form fields
- Create with all fields / minimal fields
- Navigate back without saving
- Date/time picker interactions
- Multi-select organisations
- Change event state

**Edit Event Tests (4 tests)** - Suite-level sharing
- Edit event name
- Edit date and time
- Add organisation
- Add certificate

**Form Validation (3 tests)** - No isolation
- Empty form submission
- Date format validation
- Form clearing

**Loading States (1 test)** - No isolation
- Loading indicators

---

## 📖 Running Tests

### Basic Commands

```bash
# All tests
pnpm test:e2e

# Specific file
pnpm playwright test event-form.spec.ts

# Specific test
pnpm playwright test event-form.spec.ts -g "should create event"

# Specific browser
pnpm playwright test --project=chromium
pnpm playwright test --project=firefox
pnpm playwright test --project=msedge
```

### Debug & Inspect

```bash
# Headed mode (see browser)
pnpm playwright test event-form.spec.ts --headed

# Debug mode (step through)
pnpm playwright test event-form.spec.ts --debug

# Show HTML report
pnpm playwright show-report

# Show trace for failed test
pnpm playwright show-trace test-results/*/trace.zip
```

---

## ✅ Best Practices

### DO ✅

- **Use database isolation** for create/modify tests
- Use semantic selectors: `getByRole`, `getByLabel`, `getByText`
- Wait with assertions: `await expect(locator).toBeVisible()`
- Choose right pattern: per-test for creates, suite-level for edits, none for read-only
- Use unique test data names: `Test Event ${Date.now()}`

### DON'T ❌

- **Don't write manual cleanup** - isolation handles it
- Don't use CSS selectors when semantic ones available
- Don't hard-code waits: `page.waitForTimeout(5000)`
- Don't rely on test execution order
- Don't forget to set cookie for create/modify tests

---

## 🐛 Troubleshooting

### Quick Checks

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

### Common Issues

| Issue | Quick Fix |
|-------|-----------|
| **Database creation fails** | Check backend running with `e2e-frontend-tests` profile |
| **Tests timeout** | Increase timeout: `createTestDatabase({ timeoutMs: 300000 })` |
| **Wrong database used** | Ensure cookie set BEFORE `page.goto()` |
| **Authentication fails** | Delete `e2e/.auth/storageState.json` and retry |
| **Element not found** | Run with `--headed` to see what's happening |

**For detailed troubleshooting, see:** [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)

---

## 📊 Test Structure Example

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

## 🛠️ Debugging Tools

### In Test Code

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

After test failure:
```bash
pnpm playwright show-report
# Click on failed test → Trace shows automatically
```

Features:
- See every action step
- Screenshots for each step
- Network requests timeline
- Console logs
- Source code

---

## 🔧 Configuration

### Backend

**File:** `backend/src/main/resources/application-e2e-frontend-tests.properties`

```properties
# Server
server.port=8080

# Security
security.createdatabase.api-token=${CREATEDATABASE_API_TOKEN}

# OAuth2
spring.security.oauth2.resourceserver.jwt.issuer-uri=${API_OAUTH2_RESOURCE_SERVER_JWT_ISSUER_URI}
# ... (see file for complete config)
```

**Start Script:** `backend/start-e2e-frontend-tests.sh`
- Loads variables from `.env`
- Validates required variables
- Sets `SPRING_PROFILES_ACTIVE=e2e-frontend-tests`
- Starts backend

### Frontend

**File:** `e2e/.env.local`

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

## ⚙️ CI/CD Integration

Tests configured for CI with:
- Automatic server startup
- Retry on failure (2 retries)
- Parallel execution (4 workers)
- HTML report generation
- Trace on failure

**GitHub Actions Example:**
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

## 🔍 Known Issues

### Webkit Browser Tests

**Status:** Under Investigation

**Issue:** 15/22 tests fail on webkit browser only

**Symptoms:**
- Database isolation works (DB created successfully)
- DOM elements become detached during execution
- Unexpected navigation to root path
- Error: "element was detached from the DOM"

**Cause:** Webkit-specific browser behavior (NOT database isolation)

**Workaround:**
```bash
# Exclude webkit
pnpm playwright test --project=chromium --project=firefox --project=msedge
```

### Database Isolation - ✅ RESOLVED

**Resolution:** Implemented `DynamicRoutingDataSource` for request-based routing

**Results:**
- ✅ Chromium: 22/22 passing
- ✅ Firefox: 21/22 passing
- ✅ Edge: 22/22 passing
- ⚠️ Webkit: 7/22 passing (unrelated issue)

---

## 📚 Additional Resources

### Documentation
- **[E2E_DATABASE_ISOLATION.md](./E2E_DATABASE_ISOLATION.md)** - Complete technical guide (architecture, implementation, performance)
- **[TROUBLESHOOTING.md](./TROUBLESHOOTING.md)** - Detailed troubleshooting for all common issues

### External Resources
- [Playwright Documentation](https://playwright.dev) - Official Playwright docs
- [PrimeVue Documentation](https://primevue.org) - UI components used in tests
- [Testcontainers Documentation](https://www.testcontainers.org/) - Database containers

---

## 📞 Getting Help

**Before asking for help:**

1. ✅ Check [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)
2. ✅ Run quick diagnostics (see Troubleshooting section)
3. ✅ Review [E2E_DATABASE_ISOLATION.md](./E2E_DATABASE_ISOLATION.md) if DB-related

**When asking for help, provide:**
- Test file and specific test name
- Error message and stack trace
- Screenshots/videos from test results
- Backend and frontend logs
- Environment info (OS, Node version, Docker version)

**Create HTML report:**
```bash
pnpm playwright show-report
```

---

## 🚧 Maintenance

### Adding New Tests

1. Choose isolation pattern (per-test, suite-level, or none)
2. Follow existing test structure (see Test Structure Example)
3. Use semantic selectors (`getByRole`, `getByLabel`)
4. Add appropriate assertions
5. Test locally with `--headed` mode
6. Update this README if adding new test files

### Updating Selectors

If PrimeVue components change:
1. Run test with `--debug`
2. Use Playwright Inspector to find new selectors
3. Prefer semantic selectors over CSS
4. Consider adding `data-testid` attributes for stability

---

*Last Updated: 2025-12-28*
*Version: 2.0.0*
