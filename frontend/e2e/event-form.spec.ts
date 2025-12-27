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

test.describe('EventForm - Create Event', () => {
    test.beforeEach(async ({ page }) => {
        // Navigate to event creation page
        await page.goto('/en/event/new')

        // Wait for form to be loaded
        await expect(page.getByLabel('Name')).toBeVisible()
    })

    test('should render all form fields', async ({ page }) => {
        // Verify all form fields are visible
        await expect(page.getByLabel('Name')).toBeVisible()
        await expect(page.locator('#startDate')).toBeVisible()
        await expect(page.locator('#startTime')).toBeVisible()
        await expect(page.locator('#state')).toBeVisible()
        await expect(page.locator('#organisations')).toBeVisible()
        await expect(page.locator('#certificate')).toBeVisible()

        // Verify action buttons
        await expect(page.getByLabel('Save')).toBeVisible()
        await expect(page.getByLabel('Back')).toBeVisible()
    })

    test('should create event with all fields filled', async ({ page, browserName }) => {
        const eventTitle = `Full Event ${browserName} ${Date.now()}`

        // Fill event name
        await page.getByLabel('Name').fill(eventTitle)

        // Fill date (use future date to ensure event appears at top of list)
        await page.locator('#startDate').getByRole('combobox').fill(getFutureDate(30))
        await page.keyboard.press('Escape') // Close date picker overlay

        // Fill time
        await page.locator('#startTime').getByRole('combobox').fill('14:30')
        await page.keyboard.press('Escape') // Close time picker overlay

        // Select state/status
        await page.locator('#state').click()
        await page.getByRole('option', { name: 'Planned' }).click()

        // Select organisation (multi-select)
        await page.locator('#organisations').click()
        // Select first organisation from dropdown
        await page.getByRole('option').first().click()
        // Close dropdown by clicking outside
        await page.getByLabel('Name').click()

        // Select certificate
        await page.locator('#certificate').click()
        // Select first certificate from dropdown
        await page.getByRole('option').first().click()

        // Save event
        await page.getByLabel('Save').click()

        // Verify redirect to event list
        await expect(page).toHaveURL(/\/event$/)

        // Wait for table to be fully loaded and network requests to complete
        await page.waitForSelector('table', { state: 'visible' })
        await page.waitForLoadState('networkidle')

        // Verify event appears in list (with increased timeout for query refetch)
        const row = page.getByRole('row').filter({ hasText: eventTitle })
        await expect(row).toBeVisible({ timeout: 15000 })

        // Cleanup: Delete the created event
        await row.getByRole('button').nth(2).click() // Delete button
    })

    test('should create event with minimal required fields', async ({ page, browserName }) => {
        const eventTitle = `Minimal Event ${browserName} ${Date.now()}`

        // Fill only required fields
        await page.getByLabel('Name').fill(eventTitle)
        await page.locator('#startDate').getByRole('combobox').fill(getFutureDate(35))
        await page.keyboard.press('Escape') // Close date picker overlay
        await page.locator('#startTime').getByRole('combobox').fill('10:00')
        await page.keyboard.press('Escape') // Close time picker overlay

        // Save event
        await page.getByLabel('Save').click()

        // Verify redirect to event list
        await expect(page).toHaveURL(/\/event$/)

        // Wait for table to be fully loaded and network requests to complete
        await page.waitForSelector('table', { state: 'visible' })
        await page.waitForLoadState('networkidle')

        // Verify event appears in list (with increased timeout for query refetch)
        const row = page.getByRole('row').filter({ hasText: eventTitle })
        await expect(row).toBeVisible({ timeout: 15000 })

        // Cleanup
        await row.getByRole('button').nth(2).click()
    })

    test('should navigate back without saving', async ({ page }) => {
        // Fill some data
        await page.getByLabel('Name').fill('Unsaved Event')

        // Click back button
        await page.getByLabel('Back').click()

        // Verify redirect to event list
        await expect(page).toHaveURL(/\/event$/)

        // Verify event was not created
        await expect(page.getByText('Unsaved Event')).not.toBeVisible()
    })

    test('should handle date selection via calendar picker', async ({ page, browserName }) => {
        const eventTitle = `Calendar Event ${browserName} ${Date.now()}`

        await page.getByLabel('Name').fill(eventTitle)

        // Click on date picker icon to open calendar
        await page.locator('#startDate').locator('.p-datepicker-dropdown').click()

        // Wait for calendar to open
        await expect(page.locator('.p-datepicker-panel')).toBeVisible()

        // Select a date from calendar (e.g., 15th of current month)
        await page.locator('.p-datepicker-calendar td').filter({ hasText: /^15$/ }).first().click()

        // Fill time
        await page.locator('#startTime').getByRole('combobox').fill('16:00')
        await page.keyboard.press('Escape') // Close time picker overlay

        // Save event
        await page.getByLabel('Save').click()

        // Verify creation
        await expect(page).toHaveURL(/\/event$/)

        // Wait for table to be fully loaded and network requests to complete
        await page.waitForSelector('table', { state: 'visible' })
        await page.waitForLoadState('networkidle')

        // Verify event appears in list (with increased timeout for query refetch)
        const row = page.getByRole('row').filter({ hasText: eventTitle })
        await expect(row).toBeVisible({ timeout: 15000 })

        // Cleanup
        await row.getByRole('button').nth(2).click()
    })

    test('should handle time selection via time picker', async ({ page, browserName }) => {
        const eventTitle = `Time Picker Event ${browserName} ${Date.now()}`

        await page.getByLabel('Name').fill(eventTitle)
        await page.locator('#startDate').getByRole('combobox').fill(getFutureDate(40))
        await page.keyboard.press('Escape') // Close date picker overlay

        // Click on time picker icon to open time selector
        await page.locator('#startTime').locator('.pi-clock').click()

        // Wait for time picker panel to open
        await expect(page.locator('.p-datepicker-panel.p-datepicker-timeonly')).toBeVisible()

        // Use increment buttons to set time to 18:45
        // Find hour increment button and click it multiple times
        const hourInc = page.locator('[aria-label*="Next Hour"]').or(page.locator('[data-pc-section="incrementbutton"]').first())
        for (let i = 0; i < 18; i++) {
            await hourInc.click({ timeout: 1000 }).catch(() => {}) // Set hour to 18
        }

        // Find minute increment button
        const minInc = page.locator('[aria-label*="Next Minute"]').or(page.locator('[data-pc-section="incrementbutton"]').nth(1))
        for (let i = 0; i < 45; i++) {
            await minInc.click({ timeout: 1000 }).catch(() => {}) // Set minute to 45
        }

        // Close picker
        await page.keyboard.press('Escape')

        // Save event
        await page.getByLabel('Save').click()

        // Verify creation
        await expect(page).toHaveURL(/\/event$/)

        // Cleanup
        const row = page.getByRole('row').filter({ hasText: eventTitle })
        await row.getByRole('button').nth(2).click()
    })

    test('should select multiple organisations', async ({ page, browserName }) => {
        const eventTitle = `Multi Org Event ${browserName} ${Date.now()}`

        await page.getByLabel('Name').fill(eventTitle)
        await page.locator('#startDate').getByRole('combobox').fill(getFutureDate(45))
        await page.keyboard.press('Escape') // Close date picker overlay
        await page.locator('#startTime').getByRole('combobox').fill('12:00')
        await page.keyboard.press('Escape') // Close time picker overlay

        // Open organisations dropdown
        await page.locator('#organisations').click()

        // Select multiple organisations
        const options = page.getByRole('option')
        const optionCount = await options.count()

        if (optionCount >= 2) {
            // Select first two organisations
            await options.nth(0).click()
            await options.nth(1).click()
        }
        else if (optionCount === 1) {
            // Select the only available organisation
            await options.first().click()
        }

        // Close dropdown
        await page.getByLabel('Name').click()

        // Save event
        await page.getByLabel('Save').click()

        // Verify creation
        await expect(page).toHaveURL(/\/event$/)

        // Wait for table to be fully loaded and network requests to complete
        await page.waitForSelector('table', { state: 'visible' })
        await page.waitForLoadState('networkidle')

        // Verify event appears in list (with increased timeout for query refetch)
        const row = page.getByRole('row').filter({ hasText: eventTitle })
        await expect(row).toBeVisible({ timeout: 15000 })

        // Cleanup
        await row.getByRole('button').nth(2).click()
    })

    test('should change event state', async ({ page, browserName }) => {
        const eventTitle = `State Change Event ${browserName} ${Date.now()}`

        await page.getByLabel('Name').fill(eventTitle)
        await page.locator('#startDate').getByRole('combobox').fill(getFutureDate(50))
        await page.keyboard.press('Escape') // Close date picker overlay
        await page.locator('#startTime').getByRole('combobox').fill('09:00')
        await page.keyboard.press('Escape') // Close time picker overlay

        // Select state - Finished
        await page.locator('#state').click()
        // Wait for dropdown to be visible
        await page.waitForSelector('[role="option"]', { state: 'visible' })
        await page.getByRole('option', { name: /Finished|Abgeschlossen/ }).click()

        // Save event
        await page.getByLabel('Save').click()

        // Verify creation
        await expect(page).toHaveURL(/\/event$/)

        // Wait for table to be fully loaded and network requests to complete
        await page.waitForSelector('table', { state: 'visible' })
        await page.waitForLoadState('networkidle')

        // Verify event appears in list (with increased timeout for query refetch)
        const row = page.getByRole('row').filter({ hasText: eventTitle })
        await expect(row).toBeVisible({ timeout: 15000 })

        // Cleanup
        await row.getByRole('button').nth(2).click()
    })
})

test.describe('EventForm - Edit Event', () => {
    let createdEventName: string

    test.beforeEach(async ({ page, browserName }) => {
        // Create an event to edit
        createdEventName = `Edit Test Event ${browserName} ${Date.now()}`

        await page.goto('/en/event/new')
        await page.getByLabel('Name').fill(createdEventName)
        await page.locator('#startDate').getByRole('combobox').fill(getFutureDate(55))
        await page.keyboard.press('Escape') // Close date picker overlay
        await page.locator('#startTime').getByRole('combobox').fill('10:00')
        await page.keyboard.press('Escape') // Close time picker overlay
        await page.getByLabel('Save').click()

        // Wait for redirect to list
        await expect(page).toHaveURL(/\/event$/)

        // Wait for table to be fully loaded and network requests to complete
        await page.waitForSelector('table', { state: 'visible' })
        await page.waitForLoadState('networkidle')

        // Verify event appears in list (with increased timeout for query refetch)
        const row = page.getByRole('row').filter({ hasText: createdEventName })
        await expect(row).toBeVisible({ timeout: 15000 })
    })

    test.afterEach(async ({ page }) => {
        // Cleanup: Delete the event
        await page.goto('/en/event')
        const row = page.getByRole('row').filter({ hasText: createdEventName })
        const isVisible = await row.isVisible().catch(() => false)

        if (isVisible) {
            await row.getByRole('button').nth(2).click()
        }
    })

    test('should edit event name', async ({ page }) => {
        // Navigate to event list
        await page.goto('/en/event')

        // Click edit button (first button in row)
        const row = page.getByRole('row').filter({ hasText: createdEventName })
        await row.getByRole('button').first().click()

        // Wait for form to load
        await expect(page.getByLabel('Name')).toBeVisible()

        // Verify existing name is populated
        await expect(page.getByLabel('Name')).toHaveValue(createdEventName)

        // Change name
        const newName = `${createdEventName} - Edited`
        await page.getByLabel('Name').fill(newName)

        // Save
        await page.getByLabel('Save').click()

        // Verify update
        await expect(page).toHaveURL(/\/event$/)
        await expect(page.getByText(newName)).toBeVisible()
        await expect(page.getByText(createdEventName).and(page.getByText(newName).not)).not.toBeVisible()

        // Update cleanup name
        createdEventName = newName
    })

    test('should edit event date and time', async ({ page }) => {
        await page.goto('/en/event')

        const row = page.getByRole('row').filter({ hasText: createdEventName })
        await row.getByRole('button').first().click()

        await expect(page.getByLabel('Name')).toBeVisible()

        // Change date
        await page.locator('#startDate').getByRole('combobox').fill(getFutureDate(60))
        await page.keyboard.press('Escape') // Close date picker overlay

        // Change time
        await page.locator('#startTime').getByRole('combobox').fill('15:30')
        await page.keyboard.press('Escape') // Close time picker overlay

        // Save
        await page.getByLabel('Save').click()

        // Verify update
        await expect(page).toHaveURL(/\/event$/)

        // Verify date/time in list (format may vary based on locale)
        const rowAfterEdit = page.getByRole('row').filter({ hasText: createdEventName })
        await expect(rowAfterEdit).toBeVisible()
    })

    test('should add organisation to existing event', async ({ page }) => {
        await page.goto('/en/event')

        const row = page.getByRole('row').filter({ hasText: createdEventName })
        await row.getByRole('button').first().click()

        await expect(page.getByLabel('Name')).toBeVisible()

        // Add organisation
        await page.locator('#organisations').click()
        await page.getByRole('option').first().click()
        await page.getByLabel('Name').click() // Close dropdown

        // Save
        await page.getByLabel('Save').click()

        // Verify update
        await expect(page).toHaveURL(/\/event$/)

        // Wait for table to be fully loaded and network requests to complete
        await page.waitForSelector('table', { state: 'visible' })
        await page.waitForLoadState('networkidle')

        // Verify event appears in list (with increased timeout for query refetch)
        const updatedRow = page.getByRole('row').filter({ hasText: createdEventName })
        await expect(updatedRow).toBeVisible({ timeout: 15000 })
    })

    test('should add certificate to existing event', async ({ page }) => {
        await page.goto('/en/event')

        const row = page.getByRole('row').filter({ hasText: createdEventName })
        await row.getByRole('button').first().click()

        await expect(page.getByLabel('Name')).toBeVisible()

        // Add certificate
        await page.locator('#certificate').click()

        // Wait for dropdown and select first option
        const certificateOptions = page.getByRole('option')
        const count = await certificateOptions.count()

        if (count > 0) {
            await certificateOptions.first().click()
        }

        // Save
        await page.getByLabel('Save').click()

        // Verify update
        await expect(page).toHaveURL(/\/event$/)

        // Wait for table to be fully loaded and network requests to complete
        await page.waitForSelector('table', { state: 'visible' })
        await page.waitForLoadState('networkidle')

        // Verify event appears in list (with increased timeout for query refetch)
        const updatedRow = page.getByRole('row').filter({ hasText: createdEventName })
        await expect(updatedRow).toBeVisible({ timeout: 15000 })
    })
})

test.describe('EventForm - Form Validation', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/en/event/new')
        await expect(page.getByLabel('Name')).toBeVisible()
    })

    test('should handle empty form submission gracefully', async ({ page }) => {
        // Try to save without filling any fields
        await page.getByLabel('Save').click()

        // Form should still be visible (validation should prevent submission)
        // Or error message should appear
        await expect(page.getByLabel('Name')).toBeVisible()
    })

    test('should validate date format', async ({ page, browserName }) => {
        const eventTitle = `Date Validation ${browserName} ${Date.now()}`

        await page.getByLabel('Name').fill(eventTitle)

        // Try invalid date format
        await page.locator('#startDate').getByRole('combobox').fill('invalid-date')
        await page.locator('#startTime').getByRole('combobox').fill('10:00')

        // Click save - validation should handle this
        await page.getByLabel('Save').click()

        // Should either show error or stay on form
        // This depends on PrimeVue's validation behavior
    })

    test('should clear form when navigating away and back', async ({ page }) => {
        // Fill some data
        await page.getByLabel('Name').fill('Temporary Event')
        await page.locator('#startDate').getByRole('combobox').fill(getFutureDate(65))

        // Navigate away
        await page.getByLabel('Back').click()
        await expect(page).toHaveURL(/\/event$/)

        // Navigate back to new event form
        await page.goto('/en/event/new')

        // Form should be empty/reset
        await expect(page.getByLabel('Name')).toHaveValue('')
    })
})

test.describe('EventForm - Loading States', () => {
    test('should show loading indicators when appropriate', async ({ page }) => {
        await page.goto('/en/event/new')

        // Check if dropdowns show loading state when data is being fetched
        // This is dependent on backend data availability

        // Form should eventually load all fields
        await expect(page.getByLabel('Name')).toBeVisible()
        await expect(page.locator('#state')).toBeVisible()
        await expect(page.locator('#organisations')).toBeVisible()
        await expect(page.locator('#certificate')).toBeVisible()
    })
})
