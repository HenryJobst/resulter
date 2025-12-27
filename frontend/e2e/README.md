# E2E Tests with Playwright

This directory contains end-to-end tests for the Resulter frontend application using Playwright.

## Overview

### Test Files

- **`auth-setup.ts`** - Authentication setup (runs before other tests)
- **`main.spec.ts`** - Main application flow tests
- **`create_db.spec.ts`** - Database creation tests
- **`event.spec.ts`** - Basic event CRUD operations
- **`event-form.spec.ts`** ⭐ **NEW** - Comprehensive EventForm.vue tests

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
     ```

2. **Backend Running**
   - Ensure backend is running on configured port
   - Or let Playwright start it automatically (configured in `playwright.config.ts`)

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
test('test description', async ({ page }) => {
    // 1. Navigate to page
    await page.goto('/en/event/new')

    // 2. Interact with elements
    await page.getByLabel('Name').fill('Test Event')

    // 3. Submit form
    await page.getByLabel('Save').click()

    // 4. Verify results
    await expect(page).toHaveURL(/\/event$/)

    // 5. Cleanup
    await page.getByRole('button').click()
})
```

## Best Practices

### ✅ DO
- Use `getByRole`, `getByLabel`, `getByText` for better accessibility
- Always clean up created test data
- Use unique names for test events (include `browserName` and `Date.now()`)
- Wait for elements with `await expect(...).toBeVisible()`
- Use `test.beforeEach` for common setup
- Use `test.afterEach` for cleanup

### ❌ DON'T
- Don't use CSS selectors when semantic selectors are available
- Don't hard-code wait times (`page.waitForTimeout()`) - use `expect` assertions
- Don't leave test data in the database
- Don't rely on test execution order

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
- Re-run `auth-setup.ts` manually
- Verify Keycloak configuration

## Related Documentation

- [Playwright Documentation](https://playwright.dev)
- [PrimeVue Documentation](https://primevue.org)
- [Project COVERAGE_PLAN.md](../COVERAGE_PLAN.md)

## Questions?

For questions about E2E tests, contact the development team or open an issue.
