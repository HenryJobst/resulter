import { test as base } from '@playwright/test'
import process from 'node:process'

const backend_protocol = process.env.BACKEND_PROTOCOL || 'http'
const backend_host = process.env.BACKEND_HOST || 'localhost'
const backend_port = process.env.BACKEND_PORT || 8080

export const test = base.extend({
    identifier: async ({ request }, use) => {
        const response = await request.post(`${backend_protocol}://${backend_host}:${backend_port}/createDatabase`,
                {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${process.env.AUTH_TOKEN}`
                    }
                })
        let identifier: string | undefined = undefined
        if (response.ok()) {
            identifier = await response.text()
        } else {
            throw new Error(`Failed to create database: ${response.status()} ${response.statusText()}`)
        }
        await use(identifier)
        // TODO: Shutdown database
    },
    page: async ({ page, identifier }, use) => {
        // Setzen des Headers auf der Seite
        await page.setExtraHTTPHeaders({
            'X-DB-Identifier': identifier
        })
        await use(page)
    }
})

/*
export const test = base.extend<{
    dbIdentifier: string;
}>({
    dbIdentifier: async ({ browser }, use) => {
        async function createDatabase(): Promise<string> {
            const context = await browser.newContext({
                storageState: 'e2e/.auth/storageState.json' // Pfad zu Ihrem Storage State
            })
            const requestContext = await context.request
            const response = await requestContext.post(`${backend_protocol}://${backend_host}:${backend_port}/createDatabase`)
            if (response.ok()) {
                return await response.text()
            } else {
                throw new Error(`Failed to create database: ${response.status()} ${response.statusText()}`)
            }
        }

        // Erstellen der Datenbank und Abrufen des Identifiers
        const identifier = await createDatabase()
        console.log('Database created with identifier:', identifier)

        await use(identifier)
    },
    page: async ({ page, dbIdentifier }, use) => {
        // Setzen des Headers auf der Seite
        await page.setExtraHTTPHeaders({
            'X-DB-Identifier': dbIdentifier
        })

        await use(page)
    }
})
*/
