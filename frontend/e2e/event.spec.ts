import { expect, test } from '@playwright/test'

test('create and delete test event', async ({ page, browserName }) => {
    await page.goto('/en/event/new')
    const eventTitle = `Test on ${browserName}`
    await page.getByLabel('Name').fill(eventTitle)
    await page.locator('#startDate').getByRole('combobox').fill('23.06.2024')
    await page.locator('#startTime').getByRole('combobox').fill('11:00')
    await page.getByLabel('Save').click()

    await page.goto('/en/event')
    const options = { name: `${eventTitle} June 23, 2024 11:00` }
    await expect(page.getByRole('row', options)).toBeVisible()

    await page.getByRole('row', options).getByRole('button').nth(2).click()
    await expect(page.getByRole('row', options)).not.toBeVisible()
})
