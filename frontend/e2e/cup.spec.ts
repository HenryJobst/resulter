import { expect, test } from '@playwright/test'
import { createTestDatabase } from './helpers/database'

test.describe('Cup Creation', () => {
    test.beforeEach(async ({ page }) => {
        // Create isolated test database for each test (per-test isolation)
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

        await page.goto('/en/cup/new')

        // Wait for page to be fully loaded
        await page.waitForLoadState('networkidle')
        await expect(page.getByLabel('Name')).toBeVisible()
    })

    test('create test cup', async ({ page, browserName }) => {
        const cupName = `Test Cup on ${browserName}`

        await page.getByLabel('Name').fill(cupName)

        // Fill year field (should be pre-filled with current year)
        const currentYear = new Date().getFullYear()
        await page.locator('#year').getByRole('spinbutton').fill(String(currentYear))

        // Select cup type (required field)
        await page.locator('#type').click()
        await page.waitForSelector('[role="option"]', { state: 'visible' })
        await page.getByRole('option').first().click()

        await page.getByLabel('Save').click()

        // Verify redirect to cup list
        await expect(page).toHaveURL(/\/cup$/)

        // Verify cup appears in list
        const row = page.getByRole('row').filter({ hasText: cupName })
        await expect(row).toHaveCount(1)

        // No manual cleanup needed - database will be automatically cleaned up after 30s
    })
})
