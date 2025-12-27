import { expect, test } from '@playwright/test'
import { createTestDatabase } from './helpers/database'

/**
 * Generate a future date string in DD.MM.YYYY format
 * @param daysInFuture Number of days to add to current date (default: 30)
 * @returns Formatted date string
 */
function getFutureDate(daysInFuture: number = 30): string {
    const futureDate = new Date()
    futureDate.setDate(futureDate.getDate() + daysInFuture)
    const day = String(futureDate.getDate()).padStart(2, '0')
    const month = String(futureDate.getMonth() + 1).padStart(2, '0')
    const year = futureDate.getFullYear()
    return `${day}.${month}.${year}`
}

test.describe('Event Creation', () => {
    test.beforeEach(async ({ page }) => {
        // Create isolated test database for each test (per-test isolation)
        const dbId = await createTestDatabase()

        // Set X-DB-Identifier cookie for database routing (same pattern as event-form.spec.ts)
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

    test('create test event', async ({ page, browserName }) => {
        const eventTitle = `Test on ${browserName}`
        await page.getByLabel('Name').fill(eventTitle)

        // Use a far future date to ensure event appears at top of list
        await page.locator('#startDate').getByRole('combobox').fill(getFutureDate(500))
        await page.keyboard.press('Escape') // Close date picker overlay
        await page.locator('#startTime').getByRole('combobox').fill('15:30')
        await page.keyboard.press('Escape') // Close time picker overlay
        await page.getByLabel('Save').click()

        // Verify redirect to event list
        await expect(page).toHaveURL(/\/event$/)

        // Reload page to bypass TanStack Query cache and ensure fresh data
        await page.reload()
        await page.waitForLoadState('networkidle')

        // Wait for table to be fully loaded
        await page.waitForSelector('table', { state: 'visible' })

        // Verify event appears in list
        const row = page.getByRole('row').filter({ hasText: eventTitle })
        await expect(row).toBeVisible({ timeout: 10000 })

        // No manual cleanup needed - database will be automatically cleaned up after 30s
    })
})
