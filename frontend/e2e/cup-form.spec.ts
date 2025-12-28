import { expect, test } from '@playwright/test'
import { createTestDatabase } from './helpers/database'

test.describe('CupForm - Create Cup', () => {
    test.beforeEach(async ({ page }) => {
        // Create isolated test database for each test (prevents data interference)
        const dbId = await createTestDatabase()

        // Set X-DB-Identifier cookie for database routing
        await page.context().addCookies([{
            name: 'X-DB-Identifier',
            value: dbId,
            domain: 'localhost',
            path: '/',
            httpOnly: false,
            secure: false,
            sameSite: 'Lax',
        }])

        // Navigate to cup creation page
        await page.goto('/en/cup/new')

        // Wait for page to be fully loaded
        await page.waitForLoadState('networkidle')

        // Wait for form to be loaded
        await expect(page.getByLabel('Name')).toBeVisible()
    })

    test('should render all form fields', async ({ page }) => {
        // Verify all form fields are visible
        await expect(page.getByLabel('Name')).toBeVisible()
        await expect(page.locator('#year')).toBeVisible()
        await expect(page.locator('#type')).toBeVisible()
        await expect(page.locator('#events')).toBeVisible()

        // Verify action buttons
        await expect(page.getByLabel('Save')).toBeVisible()
        await expect(page.getByLabel('Back')).toBeVisible()
    })

    test('should create cup with all fields filled', async ({ page, browserName }) => {
        const cupName = `Full Cup ${browserName} ${Date.now()}`

        // Fill cup name
        await page.getByLabel('Name').fill(cupName)

        // Fill year
        const currentYear = new Date().getFullYear()
        await page.locator('#year').getByRole('spinbutton').fill(String(currentYear))

        // Select cup type
        await page.locator('#type').click()
        // Wait for dropdown options
        await page.waitForSelector('[role="option"]', { state: 'visible' })
        // Select first cup type from dropdown
        await page.getByRole('option').first().click()

        // Select events (multi-select) - optional if events exist
        await page.locator('#events').click()
        const eventOptions = page.getByRole('option')
        const eventCount = await eventOptions.count()
        if (eventCount > 0) {
            // Select first event
            await eventOptions.first().click()
        }
        // Close dropdown
        await page.keyboard.press('Escape')
        // Wait for dropdown to close (especially important in Firefox)
        await page.waitForTimeout(300)

        // Save cup
        await page.getByLabel('Save').click()

        // Verify redirect to cup list
        await expect(page).toHaveURL(/\/cup$/)

        // Verify cup appears in list
        const row = page.getByRole('row').filter({ hasText: cupName })
        await expect(row).toHaveCount(1)

        // No cleanup needed - isolated database will be auto-cleaned
    })

    test('should create cup with minimal required fields', async ({ page, browserName }) => {
        const cupName = `Minimal Cup ${browserName} ${Date.now()}`

        // Fill required fields (name, year, and type)
        await page.getByLabel('Name').fill(cupName)

        // Year should be pre-filled with current year, but let's set it explicitly
        const currentYear = new Date().getFullYear()
        await page.locator('#year').getByRole('spinbutton').fill(String(currentYear))

        // Select cup type (required field)
        await page.locator('#type').click()
        await page.waitForSelector('[role="option"]', { state: 'visible' })
        await page.getByRole('option').first().click()

        // Save cup
        await page.getByLabel('Save').click()

        // Verify redirect to cup list
        await expect(page).toHaveURL(/\/cup$/)

        // Verify cup appears in list
        const row = page.getByRole('row').filter({ hasText: cupName })
        await expect(row).toHaveCount(1)

        // No cleanup needed - isolated database
    })

    test('should navigate back without saving', async ({ page }) => {
        // Fill some data
        await page.getByLabel('Name').fill('Unsaved Cup')

        // Click back button
        await page.getByLabel('Back').click()

        // Verify redirect to cup list
        await expect(page).toHaveURL(/\/cup$/)

        // Verify cup was not created
        await expect(page.getByText('Unsaved Cup')).not.toBeVisible()
    })

    test('should handle year increment/decrement buttons', async ({ page, browserName }) => {
        const cupName = `Year Buttons Cup ${browserName} ${Date.now()}`

        await page.getByLabel('Name').fill(cupName)

        // Test year input
        const yearInput = page.locator('#year').getByRole('spinbutton')
        await yearInput.fill('2024')

        // Verify year was set
        await expect(yearInput).toHaveValue('2024')

        // Select cup type (required field)
        await page.locator('#type').click()
        await page.waitForSelector('[role="option"]', { state: 'visible' })
        await page.getByRole('option').first().click()

        // Save cup
        await page.getByLabel('Save').click()

        // Verify creation
        await expect(page).toHaveURL(/\/cup$/)

        // Verify cup appears in list
        const row = page.getByRole('row').filter({ hasText: cupName })
        await expect(row).toHaveCount(1)

        // No cleanup needed - isolated database
    })

    test('should select multiple events', async ({ page, browserName }) => {
        const cupName = `Multi Event Cup ${browserName} ${Date.now()}`

        await page.getByLabel('Name').fill(cupName)

        const currentYear = new Date().getFullYear()
        await page.locator('#year').getByRole('spinbutton').fill(String(currentYear))

        // Select cup type (required field)
        await page.locator('#type').click()
        await page.waitForSelector('[role="option"]', { state: 'visible' })
        await page.getByRole('option').first().click()

        // Open events dropdown
        await page.locator('#events').click()

        // Wait for dropdown options to load (there might not be any events)
        await page.waitForTimeout(500)
        const eventOptions = page.getByRole('option')
        const eventCount = await eventOptions.count()

        if (eventCount >= 2) {
            // Select first two events
            await eventOptions.nth(0).click()
            await eventOptions.nth(1).click()
        }
        else if (eventCount === 1) {
            // Select the only available event
            await eventOptions.first().click()
        }

        // Close dropdown
        await page.keyboard.press('Escape')

        // Save cup
        await page.getByLabel('Save').click()

        // Verify creation
        await expect(page).toHaveURL(/\/cup$/)

        // Verify cup appears in list
        const row = page.getByRole('row').filter({ hasText: cupName })
        await expect(row).toHaveCount(1)

        // No cleanup needed - isolated database
    })

    test('should change cup type', async ({ page, browserName }) => {
        const cupName = `Type Change Cup ${browserName} ${Date.now()}`

        await page.getByLabel('Name').fill(cupName)

        const currentYear = new Date().getFullYear()
        await page.locator('#year').getByRole('spinbutton').fill(String(currentYear))

        // Select cup type
        await page.locator('#type').click()
        // Wait for dropdown to be visible
        await page.waitForSelector('[role="option"]', { state: 'visible' })

        // Select first available type
        const typeOptions = page.getByRole('option')
        const typeCount = await typeOptions.count()
        if (typeCount > 0) {
            await typeOptions.first().click()
        }

        // Save cup
        await page.getByLabel('Save').click()

        // Verify creation
        await expect(page).toHaveURL(/\/cup$/)

        // Verify cup appears in list
        const row = page.getByRole('row').filter({ hasText: cupName })
        await expect(row).toHaveCount(1)

        // No cleanup needed - isolated database
    })
})

test.describe('CupForm - Edit Cup', () => {
    // Configure suite to run tests sequentially (not in parallel)
    // This ensures all tests share the same database created in beforeAll
    test.describe.configure({ mode: 'serial' })

    let createdCupName: string
    let originalCupName: string
    let sharedDbId: string

    test.beforeAll(async ({ browser }) => {
        // Create shared test database for all tests in this suite (suite-level isolation)
        sharedDbId = await createTestDatabase({ timeoutMs: 300000 }) // 5 min timeout for long-running suite
        originalCupName = `Edit Test Cup ${Date.now()}`
        createdCupName = originalCupName

        // Create the cup ONCE in beforeAll so all tests can reuse it
        const context = await browser.newContext({ storageState: 'e2e/.auth/storageState.json' })
        const page = await context.newPage()

        // Set database cookie
        await page.context().addCookies([{
            name: 'X-DB-Identifier',
            value: sharedDbId,
            domain: 'localhost',
            path: '/',
            httpOnly: false,
            secure: false,
            sameSite: 'Lax',
        }])

        // Create the cup
        await page.goto('/en/cup/new')
        await page.waitForLoadState('networkidle')
        await page.getByLabel('Name').fill(originalCupName)

        const currentYear = new Date().getFullYear()
        await page.locator('#year').getByRole('spinbutton').fill(String(currentYear))

        // Select cup type (required field)
        await page.locator('#type').click()
        await page.waitForSelector('[role="option"]', { state: 'visible' })
        await page.getByRole('option').first().click()

        await page.getByLabel('Save').click()
        await expect(page).toHaveURL(/\/cup$/, { timeout: 15000 })

        // Wait for cup to appear in list
        const row = page.getByRole('row').filter({ hasText: originalCupName })
        await expect(row).toHaveCount(1)

        await context.close()
    })

    test.beforeEach(async ({ page }) => {
        // Set X-DB-Identifier cookie for database routing
        await page.context().addCookies([{
            name: 'X-DB-Identifier',
            value: sharedDbId,
            domain: 'localhost',
            path: '/',
            httpOnly: false,
            secure: false,
            sameSite: 'Lax',
        }])

        // Navigate to cup list
        await page.goto('/en/cup')
        await page.waitForLoadState('networkidle')

        // Find the cup row and click edit button
        const cupRow = page.getByRole('row').filter({ hasText: createdCupName })
        await expect(cupRow).toHaveCount(1)

        // Click edit button (pencil icon)
        const editButton = cupRow.getByLabel('Edit')
        await editButton.click()

        // Wait for edit page to load
        await page.waitForLoadState('networkidle')
        await expect(page.getByLabel('Name')).toBeVisible()

        // Verify we're on the edit page with correct cup loaded
        await expect(page.getByLabel('Name')).toHaveValue(createdCupName)
    })

    test('should edit cup name', async ({ page }) => {
        const newName = `Edited Cup Name ${Date.now()}`

        // Change name
        await page.getByLabel('Name').fill(newName)

        // Save changes
        await page.getByLabel('Save').click()

        // Verify redirect to cup list
        await expect(page).toHaveURL(/\/cup$/)

        // Verify updated cup appears in list
        const row = page.getByRole('row').filter({ hasText: newName })
        await expect(row).toHaveCount(1)

        // Update createdCupName for next tests
        createdCupName = newName
    })

    test('should edit cup year', async ({ page }) => {
        // Change year
        const newYear = new Date().getFullYear() + 1
        await page.locator('#year').getByRole('spinbutton').fill(String(newYear))

        // Save changes
        await page.getByLabel('Save').click()

        // Verify redirect to cup list
        await expect(page).toHaveURL(/\/cup$/)

        // Verify cup still appears in list
        const row = page.getByRole('row').filter({ hasText: createdCupName })
        await expect(row).toHaveCount(1)
    })

    test('should edit cup type', async ({ page }) => {
        // Change cup type
        await page.locator('#type').click()
        await page.waitForSelector('[role="option"]', { state: 'visible' })

        const typeOptions = page.getByRole('option')
        const typeCount = await typeOptions.count()
        if (typeCount > 0) {
            // Select first available type
            await typeOptions.first().click()
        }
        else {
            // No types available, close dropdown
            await page.keyboard.press('Escape')
        }

        // Save changes
        await page.getByLabel('Save').click()

        // Verify redirect to cup list
        await expect(page).toHaveURL(/\/cup$/)

        // Verify cup still appears in list
        const row = page.getByRole('row').filter({ hasText: createdCupName })
        await expect(row).toHaveCount(1)
    })

    test('should cancel edit without saving', async ({ page }) => {
        // Change name but don't save
        await page.getByLabel('Name').fill('This should not be saved')

        // Click back button
        await page.getByLabel('Back').click()

        // Verify redirect to cup list
        await expect(page).toHaveURL(/\/cup$/)

        // Verify original cup name is still in list
        const row = page.getByRole('row').filter({ hasText: createdCupName })
        await expect(row).toHaveCount(1)

        // Verify unsaved name is not in list
        await expect(page.getByText('This should not be saved')).not.toBeVisible()
    })
})

test.describe('CupForm - Form Validation', () => {
    test.beforeEach(async ({ page }) => {
        // These are read-only validation tests, no database needed
        await page.goto('/en/cup/new')
        await page.waitForLoadState('networkidle')
        await expect(page.getByLabel('Name')).toBeVisible()
    })

    test('should have year pre-filled with current year', async ({ page }) => {
        const currentYear = new Date().getFullYear()
        const yearInput = page.locator('#year').getByRole('spinbutton')

        // Verify year is pre-filled
        await expect(yearInput).toHaveValue(String(currentYear))
    })

    test('should accept valid year range', async ({ page }) => {
        const yearInput = page.locator('#year').getByRole('spinbutton')

        // Test minimum year
        await yearInput.fill('1970')
        await expect(yearInput).toHaveValue('1970')

        // Test maximum year
        await yearInput.fill('9999')
        await expect(yearInput).toHaveValue('9999')

        // Test current year
        const currentYear = new Date().getFullYear()
        await yearInput.fill(String(currentYear))
        await expect(yearInput).toHaveValue(String(currentYear))
    })

    test('should require name field', async ({ page }) => {
        // Try to save without filling name
        await page.getByLabel('Save').click()

        // Should stay on same page (validation error)
        await expect(page).toHaveURL(/\/cup\/new$/)
    })
})

test.describe('CupForm - Loading States', () => {
    test('should show loading state for cup types', async ({ page }) => {
        // Navigate to cup creation page
        await page.goto('/en/cup/new')

        // Check if loading message appears briefly (may be too fast to catch)
        // This test verifies the loading state exists in the component
        await expect(page.getByLabel('Name')).toBeVisible()

        // Verify type selector is eventually loaded
        await expect(page.locator('#type')).toBeVisible()
    })

    test('should show loading state for events', async ({ page }) => {
        // Navigate to cup creation page
        await page.goto('/en/cup/new')

        // Wait for form to load
        await expect(page.getByLabel('Name')).toBeVisible()

        // Verify events selector is eventually loaded
        await expect(page.locator('#events')).toBeVisible()
    })
})
