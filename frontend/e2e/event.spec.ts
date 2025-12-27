import { expect, test } from '@playwright/test'

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

test('create and delete test event', async ({ page, browserName }) => {
    await page.goto('/en/event/new')
    const eventTitle = `Test on ${browserName}`
    await page.getByLabel('Name').fill(eventTitle)

    // Use a future date to ensure event appears at top of list
    await page.locator('#startDate').getByRole('combobox').fill(getFutureDate(30))
    await page.keyboard.press('Escape') // Close date picker overlay
    await page.locator('#startTime').getByRole('combobox').fill('15:30')
    await page.keyboard.press('Escape') // Close time picker overlay
    await page.getByLabel('Save').click()

    // Wait for redirect to event list
    await expect(page).toHaveURL(/\/event$/)

    // Wait for table to be fully loaded and network requests to complete
    await page.waitForSelector('table', { state: 'visible' })
    await page.waitForLoadState('networkidle')

    // Verify event appears in list (with increased timeout for query refetch)
    const row = page.getByRole('row').filter({ hasText: eventTitle })
    await expect(row).toBeVisible({ timeout: 15000 })

    // Find and delete the event by clicking delete button in the row containing the event name
    await row.getByRole('button').nth(2).click()
    await expect(row).not.toBeVisible()
})
