import { expect, test } from '@playwright/test'

test('main page accessible', async ({ page }) => {
    await page.goto('/')

    await expect(page).toHaveTitle(/Resulter/)
})

test('event page (en) renders', async ({ page }) => {
    await page.goto('/en/event')

    await expect(page.getByRole('heading', { name: 'Events' })).toBeVisible()
})

test('event page (de) renders', async ({ page }) => {
    await page.goto('/de/event')

    await expect(page.getByRole('heading', { name: 'Wettkämpfe' })).toBeVisible()
})
