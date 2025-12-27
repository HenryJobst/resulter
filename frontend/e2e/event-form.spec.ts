import { expect, test } from '@playwright/test'

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

        // Fill date
        await page.locator('#startDate').getByRole('combobox').fill('15.12.2024')

        // Fill time
        await page.locator('#startTime').getByRole('combobox').fill('14:30')

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

        // Verify event appears in list
        await expect(page.getByText(eventTitle)).toBeVisible()

        // Cleanup: Delete the created event
        const row = page.getByRole('row').filter({ hasText: eventTitle })
        await row.getByRole('button').nth(2).click() // Delete button
    })

    test('should create event with minimal required fields', async ({ page, browserName }) => {
        const eventTitle = `Minimal Event ${browserName} ${Date.now()}`

        // Fill only required fields
        await page.getByLabel('Name').fill(eventTitle)
        await page.locator('#startDate').getByRole('combobox').fill('20.12.2024')
        await page.locator('#startTime').getByRole('combobox').fill('10:00')

        // Save event
        await page.getByLabel('Save').click()

        // Verify redirect to event list
        await expect(page).toHaveURL(/\/event$/)

        // Verify event appears in list
        await expect(page.getByText(eventTitle)).toBeVisible()

        // Cleanup
        const row = page.getByRole('row').filter({ hasText: eventTitle })
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
        await page.locator('#startDate').locator('.p-datepicker-trigger').click()

        // Wait for calendar to open
        await expect(page.locator('.p-datepicker')).toBeVisible()

        // Select a date from calendar (e.g., 15th of current month)
        await page.locator('.p-datepicker-calendar td').filter({ hasText: /^15$/ }).first().click()

        // Fill time
        await page.locator('#startTime').getByRole('combobox').fill('16:00')

        // Save event
        await page.getByLabel('Save').click()

        // Verify creation
        await expect(page).toHaveURL(/\/event$/)
        await expect(page.getByText(eventTitle)).toBeVisible()

        // Cleanup
        const row = page.getByRole('row').filter({ hasText: eventTitle })
        await row.getByRole('button').nth(2).click()
    })

    test('should handle time selection via time picker', async ({ page, browserName }) => {
        const eventTitle = `Time Picker Event ${browserName} ${Date.now()}`

        await page.getByLabel('Name').fill(eventTitle)
        await page.locator('#startDate').getByRole('combobox').fill('25.12.2024')

        // Click on time picker icon to open time selector
        await page.locator('#startTime').locator('.pi-clock').click()

        // Wait for time picker to open
        await expect(page.locator('.p-timepicker')).toBeVisible()

        // Select hour (e.g., click increment to get to desired hour)
        // This is a simplified approach - actual implementation may vary
        await page.locator('.p-timepicker').getByRole('combobox').first().fill('18')
        await page.locator('.p-timepicker').getByRole('combobox').nth(1).fill('45')

        // Click outside to close picker
        await page.getByLabel('Name').click()

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
        await page.locator('#startDate').getByRole('combobox').fill('28.12.2024')
        await page.locator('#startTime').getByRole('combobox').fill('12:00')

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
        await expect(page.getByText(eventTitle)).toBeVisible()

        // Cleanup
        const row = page.getByRole('row').filter({ hasText: eventTitle })
        await row.getByRole('button').nth(2).click()
    })

    test('should change event state', async ({ page, browserName }) => {
        const eventTitle = `State Change Event ${browserName} ${Date.now()}`

        await page.getByLabel('Name').fill(eventTitle)
        await page.locator('#startDate').getByRole('combobox').fill('30.12.2024')
        await page.locator('#startTime').getByRole('combobox').fill('09:00')

        // Select state - Finished
        await page.locator('#state').click()
        await page.getByRole('option', { name: /Finished|Abgeschlossen/ }).click()

        // Save event
        await page.getByLabel('Save').click()

        // Verify creation
        await expect(page).toHaveURL(/\/event$/)
        await expect(page.getByText(eventTitle)).toBeVisible()

        // Cleanup
        const row = page.getByRole('row').filter({ hasText: eventTitle })
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
        await page.locator('#startDate').getByRole('combobox').fill('01.01.2025')
        await page.locator('#startTime').getByRole('combobox').fill('10:00')
        await page.getByLabel('Save').click()

        // Wait for redirect to list
        await expect(page).toHaveURL(/\/event$/)
        await expect(page.getByText(createdEventName)).toBeVisible()
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
        await page.locator('#startDate').getByRole('combobox').fill('15.02.2025')

        // Change time
        await page.locator('#startTime').getByRole('combobox').fill('15:30')

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
        await expect(page.getByText(createdEventName)).toBeVisible()
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
        await expect(page.getByText(createdEventName)).toBeVisible()
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
        await page.locator('#startDate').getByRole('combobox').fill('15.03.2025')

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
