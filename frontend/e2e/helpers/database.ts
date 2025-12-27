/**
 * Database isolation helpers for E2E tests using testcontainers.
 *
 * This module provides utilities to create isolated test databases per test
 * to prevent test interference through shared test data.
 */

import type { Page } from '@playwright/test'
import process from 'node:process'

export interface DatabaseConfig {
    /**
     * Timeout in milliseconds for database creation.
     * Default: 120000ms (2 minutes) to account for testcontainer startup time.
     */
    timeoutMs?: number
}

/**
 * Creates a new isolated test database using the backend's testcontainers infrastructure.
 *
 * The backend will:
 * 1. Spin up a new PostgreSQL testcontainer instance
 * 2. Run Liquibase migrations automatically
 * 3. Return a unique database identifier (UUID)
 * 4. Auto-cleanup the database after 30 seconds of inactivity
 *
 * @param config Optional configuration (timeout)
 * @returns Database identifier (UUID string) to be used with useDatabaseIsolation()
 * @throws Error if database creation fails or times out
 *
 * @example
 * ```typescript
 * test('my test', async ({ page }) => {
 *   const dbId = await createTestDatabase()
 *   test.use(useDatabaseIsolation(dbId))
 *   // Test runs in isolated database
 * })
 * ```
 */
export async function createTestDatabase(config?: DatabaseConfig): Promise<string> {
    const backend_protocol = process.env.BACKEND_PROTOCOL || 'http'
    const backend_host = process.env.HOSTNAME || 'localhost'
    const backend_port = process.env.BACKEND_PORT || '8080'
    const token = process.env.CREATEDATABASE_API_TOKEN || 'test-database-token'
    const timeout = config?.timeoutMs || 120000 // 2 minutes default

    const url = `${backend_protocol}://${backend_host}:${backend_port}/createDatabase`

    console.log(`[Database Helper] Creating test database via ${url}`)
    console.log(`[Database Helper] Using timeout: ${timeout}ms`)

    try {
        const controller = new AbortController()
        const timeoutId = setTimeout(() => controller.abort(), timeout)

        const requestBody = config?.timeoutMs ? { timeoutMinutes: Math.ceil(config.timeoutMs / 60000) } : {}

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            body: JSON.stringify(requestBody),
            signal: controller.signal,
        })

        clearTimeout(timeoutId)

        if (!response.ok) {
            const errorText = await response.text()
            throw new Error(
                `Failed to create test database: ${response.status} ${response.statusText}\n${errorText}`,
            )
        }

        const dbIdentifier = (await response.text()).trim()

        if (!dbIdentifier || typeof dbIdentifier !== 'string') {
            throw new Error(`Invalid database identifier received: ${dbIdentifier}`)
        }

        console.log(`[Database Helper] Database created successfully: ${dbIdentifier}`)
        return dbIdentifier
    }
    catch (error) {
        if (error instanceof Error) {
            if (error.name === 'AbortError') {
                throw new Error(
                    `Database creation timed out after ${timeout}ms. `
                    + 'Testcontainer startup may be taking longer than expected. '
                    + 'Consider increasing the timeout or checking Docker availability.',
                )
            }
            throw error
        }
        throw new Error(`Unknown error during database creation: ${error}`)
    }
}

/**
 * Returns Playwright configuration to use an isolated test database.
 *
 * This function returns a configuration object that can be passed to test.use()
 * to inject the X-DB-Identifier header into all HTTP requests, routing them
 * to the specified isolated database.
 *
 * @param dbIdentifier Database identifier returned by createTestDatabase()
 * @returns Playwright UseOptions with extraHTTPHeaders configured
 *
 * @example
 * ```typescript
 * test('my test', async ({ page }) => {
 *   const dbId = await createTestDatabase()
 *   test.use(useDatabaseIsolation(dbId))
 *   // All requests will use X-DB-Identifier header
 * })
 * ```
 */
export function useDatabaseIsolation(dbIdentifier: string) {
    return {
        extraHTTPHeaders: {
            'X-DB-Identifier': dbIdentifier,
        },
    }
}

/**
 * Setup request interception to add X-DB-Identifier header to API requests.
 * This is necessary because Vite proxy doesn't forward cookies from browser to backend.
 *
 * This function intercepts ONLY /api/** requests (not OAuth2 requests) and adds
 * the database identifier header, ensuring the isolated database is used.
 *
 * @param page - Playwright page instance
 * @param dbIdentifier - Database identifier to use for routing
 *
 * @example
 * ```typescript
 * test('my test', async ({ page }) => {
 *   const dbId = await createTestDatabase()
 *   await setupDatabaseRouting(page, dbId)
 *   await page.goto('/en/event/new')
 *   // All /api/** requests will include X-DB-Identifier header
 * })
 * ```
 */
export async function setupDatabaseRouting(page: Page, dbIdentifier: string): Promise<void> {
    // Intercept only API requests (not OAuth2) and add database identifier header
    // Pattern matches any URL containing /api/ (includes both localhost:5173 and localhost:8080)
    await page.route(/\/api\//, async (route) => {
        const url = route.request().url()
        console.log(`[Route Interception] Intercepting request to: ${url}`)
        console.log(`[Route Interception] Adding X-DB-Identifier: ${dbIdentifier}`)

        const headers = {
            ...route.request().headers(),
            'X-DB-Identifier': dbIdentifier,
        }
        await route.continue({ headers })
    })
}
