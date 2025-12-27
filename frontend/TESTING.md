# Testing Guide

Comprehensive guide for running tests in the Resulter frontend application.

## Quick Start

```bash
# Run all tests (unit + e2e)
pnpm test:all

# Run only unit tests
pnpm test:unit

# Run only E2E tests
pnpm test:e2e
```

## Unit Tests (Vitest)

Unit tests are written with Vitest and Vue Test Utils. They test individual components, stores, and services in isolation.

### Basic Commands

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

### Running Specific Tests

```bash
# Run tests in a specific file
pnpm test:unit:file src/features/auth/store/auth.store.spec.ts

# Run tests matching a pattern (via Vitest UI)
pnpm test:unit:ui
# Then use the UI to filter tests by name
```

### Coverage

Coverage reports are generated in `coverage/` directory and show:
- Statement coverage
- Branch coverage
- Function coverage
- Line coverage

Open `coverage/index.html` in a browser to view the detailed report.

## E2E Tests (Playwright)

E2E tests use Playwright to test the application in a real browser environment. They test complete user workflows.

### Prerequisites

⚠️ **IMPORTANT: You must start both servers manually before running E2E tests!**

1. **Start Frontend Server:**
   ```bash
   pnpm dev
   ```
   Wait until you see: `VITE vX.X.X ready in XXXms`

2. **Start Backend Server:**
   ```bash
   cd ../backend
   ./mvnw spring-boot:run
   ```
   Wait until you see: `Started ResulterApplication in X.XXX seconds`

3. **Environment configured** in `e2e/.env.local`:
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

### Basic Commands

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

### Browser-Specific Tests

```bash
# Run tests on Chromium only
pnpm test:e2e:chromium

# Run tests on Firefox only
pnpm test:e2e:firefox

# Run tests on WebKit (Safari) only
pnpm test:e2e:webkit
```

### Authentication Tests

Authentication is handled via BFF (Backend-for-Frontend) pattern. Session cookies are saved and reused.

```bash
# Run only authentication setup (establishes session)
pnpm test:e2e:auth

# Run auth setup in headed mode (see login process)
pnpm test:e2e:auth:headed

# Debug authentication flow
pnpm test:e2e:auth:debug
```

**Note:** Auth session is cached for 10 minutes in `e2e/.auth/storageState.json`. Delete this file to force re-authentication.

### Specific Test Suites

```bash
# Run main page tests
pnpm test:e2e:main

# Run event CRUD tests
pnpm test:e2e:event

# Run event form tests (comprehensive)
pnpm test:e2e:event-form
```

### Reports and Traces

```bash
# View HTML test report
pnpm test:e2e:report

# View trace for failed tests (debugging)
pnpm test:e2e:trace
```

### Running Specific Tests

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

## Legacy Cypress Tests

Cypress tests are still available but Playwright is preferred for new tests.

```bash
# Run Cypress E2E tests
pnpm test:cypress

# Open Cypress in dev mode
pnpm test:cypress:dev
```

## Test Structure

### Unit Tests

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

### E2E Tests

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

### Unit Tests

✅ **DO:**
- Test one component/function per file
- Mock external dependencies
- Use descriptive test names
- Test edge cases and error conditions
- Keep tests fast and isolated

❌ **DON'T:**
- Test implementation details
- Create brittle tests that break on refactoring
- Use real API calls
- Share state between tests

### E2E Tests

✅ **DO:**
- Test complete user workflows
- Use semantic selectors (getByRole, getByLabel)
- Clean up test data after tests
- Use unique names for test data
- Wait for elements with expect assertions

❌ **DON'T:**
- Use hard-coded wait times
- Rely on test execution order
- Leave test data in database
- Use CSS selectors when semantic selectors available

## Debugging

### Unit Tests

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

### E2E Tests

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

Tests run automatically on push/PR:

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

### Local Pre-commit

Unit tests run automatically via lint-staged before commit.

## Troubleshooting

### Unit Tests

**Problem:** Tests fail with "Cannot find module"
- **Solution:** Run `pnpm install` to install dependencies

**Problem:** Tests timeout
- **Solution:** Increase timeout in test or check for async issues

**Problem:** Mock not working
- **Solution:** Ensure vi.mock() is at top of file, before imports

### E2E Tests

**Problem:** Authentication fails
- **Solution:**
  - Delete `e2e/.auth/storageState.json`
  - Check backend is running
  - Verify credentials in `e2e/.env.local`
  - Run `pnpm test:e2e:auth:headed` to see login process

**Problem:** "Element not found"
- **Solution:**
  - Check if selector changed in UI
  - Ensure element is visible before interaction
  - Check for loading states

**Problem:** "Timeout waiting for page"
- **Solution:**
  - Check backend is running
  - Increase timeout in playwright.config.ts
  - Check network requests in headed mode

**Problem:** "Session expired"
- **Solution:**
  - Delete `e2e/.auth/storageState.json` to force re-auth
  - Session is valid for 10 minutes

## Performance

### Unit Tests

- Fast: ~5-10 seconds for 639 tests
- Run in parallel by default
- Use --watch mode during development

### E2E Tests

- Slower: ~30-60 seconds depending on tests
- Run sequentially on CI
- Use headed mode for debugging only
- Cache authentication for 10 minutes

## Resources

- [Vitest Documentation](https://vitest.dev)
- [Vue Test Utils](https://test-utils.vuejs.org)
- [Playwright Documentation](https://playwright.dev)
- [Testing Library](https://testing-library.com)

## Questions?

For questions about testing, check:
1. This guide
2. Test file examples in `test/` and `e2e/`
3. Component documentation
4. Team documentation
