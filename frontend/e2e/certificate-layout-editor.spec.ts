import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import process from 'node:process'
import { expect, test } from '@playwright/test'
import { createTestDatabase } from './helpers/database'

const backendUrl = `${process.env.BACKEND_PROTOCOL || 'http'}://localhost:${process.env.BACKEND_PORT || '8080'}`
// playwright runs in the frontend folder
const background = resolve(process.cwd(), '../backend/src/test/resources/certificate/Urkunde_BTFB_2023.jpg')

test.describe('Certificate layout editor', () => {
    test.beforeEach(async ({ page }) => {
        const dbId = await createTestDatabase()
        await page.context().addCookies([{
            name: 'X-DB-Identifier',
            value: dbId,
            domain: 'localhost',
            path: '/',
            httpOnly: false,
            secure: false,
            sameSite: 'Lax',
        }])
    })

    test('edits a layout visually, shows the preview and saves it', async ({ page }) => {
        // the CSRF token cookie is set once the app has been opened
        await page.goto('/en/certificate/new')
        await page.waitForLoadState('networkidle')
        const csrf = (await page.context().cookies()).find(c => c.name === 'XSRF-TOKEN')?.value ?? ''
        const headers = { 'X-XSRF-TOKEN': csrf }

        // an event and a background image are needed to render the preview
        const event = await page.request.post(`${backendUrl}/event`, {
            headers,
            data: {
                name: 'Layout Editor Event',
                startTime: new Date().toISOString(),
                state: { id: 'Planned' },
                organisations: [],
                certificate: null,
                hasSplitTimes: false,
                discipline: { id: 'Sprint' },
                aggregateScore: false,
            },
        })
        expect(event.ok(), `create event: ${event.status()} ${await event.text()}`).toBeTruthy()
        const upload = await page.request.post(`${backendUrl}/media/upload`, {
            headers,
            multipart: {
                file: { name: 'background.jpg', mimeType: 'image/jpeg', buffer: readFileSync(background) },
            },
        })
        expect(upload.ok(), `upload: ${upload.status()} ${await upload.text()}`).toBeTruthy()

        await page.goto('/en/certificate/new')
        await page.waitForLoadState('networkidle')

        await page.getByLabel('Name').first().fill('Editor Certificate')
        await page.locator('#event').click()
        await page.getByRole('option', { name: 'Layout Editor Event' }).click()
        await page.locator('#media').click()
        await page.getByRole('option').first().click()

        // build a layout with the visual editor
        await page.getByRole('button', { name: 'Add paragraph' }).click()
        await page.getByLabel('Text', { exact: true }).first().fill('Hello ')
        await page.getByRole('button', { name: 'GIVEN_NAME' }).first().click()
        await page.getByLabel('Font size').first().fill('30')
        await page.getByLabel('Bold').first().check()

        // the preview renders the PDF from the layout
        await expect(page.locator('.preview-panel canvas').first()).toBeVisible({ timeout: 30000 })

        // the JSON mode shows what the editor produced
        await page.getByText('JSON', { exact: true }).click()
        const json = page.locator('textarea').first()
        await expect(json).toHaveValue(/"text": "Hello \{\{GIVEN_NAME\}\}"/)
        await expect(json).toHaveValue(/"fontSize": 30/)
        await expect(page.getByRole('alert')).toHaveCount(0)

        // a schema violation is reported while typing
        await json.fill(JSON.stringify({ paragraphs: [{ blocks: [{ block: { text: 'x', fontSize: 'big' } }] }] }))
        await expect(page.getByRole('alert').first()).toContainText('fontSize')

        // restore a valid layout and save
        await json.fill(JSON.stringify({ paragraphs: [{ blocks: [{ block: { text: 'Hello {{GIVEN_NAME}}', fontSize: 30, bold: true } }] }] }))
        await expect(page.getByRole('alert')).toHaveCount(0)
        await page.getByLabel('Save').click()
        await expect(page).toHaveURL(/\/certificate$/)

        // reopening shows the saved layout in the visual editor
        const row = page.getByRole('row').filter({ hasText: 'Editor Certificate' })
        await row.getByLabel('Edit').click()
        await expect(page.getByText('Paragraph 1')).toBeVisible()
        await expect(page.getByLabel('Text', { exact: true }).first()).toHaveValue('Hello {{GIVEN_NAME}}')
    })
})
