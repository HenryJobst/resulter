import type { Page } from '@playwright/test'

/** The backend requires a discipline when saving an event, so pick the first one. */
export async function selectFirstDiscipline(page: Page): Promise<void> {
    await page.locator('#discipline').click()
    await page.getByRole('option').first().click()
}
