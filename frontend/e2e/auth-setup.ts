import * as fs from 'node:fs'
import process from 'node:process'
import { test } from '@playwright/test'
import * as dotenv from 'dotenv'

// we don't want to store credentials in the repository
dotenv.config({
    path: './e2e/.env.local',
})

const storageState = 'e2e/.auth/storageState.json'

test('authenticate user with BFF', async ({ page }) => {
    if (process.env.USERNAME === '**REMOVED**') {
        throw new Error('Env file is not correct')
    }

    const stats = fs.existsSync(storageState!.toString()) ? fs.statSync(storageState!.toString()) : null
    if (stats && stats.mtimeMs > new Date().getTime() - 600000) {
        console.log(`\x1B[2m\tSign in skipped because session is fresh\x1B[0m`)
        return
    }

    console.log(`\x1B[2m\tBFF authentication started\x1B[0m`)

    // BFF Pattern: Navigate to backend OAuth2 authorization endpoint
    // This will redirect to Keycloak, then back to the backend, then to the frontend
    const backendUrl = `${process.env.BACKEND_PROTOCOL}://${process.env.HOSTNAME}:${process.env.BACKEND_PORT}`
    const oauth2Url = `${backendUrl}/oauth2/authorization/keycloak`

    console.log(`\x1B[2m\tNavigating to OAuth2 endpoint: ${oauth2Url}\x1B[0m`)

    // Start the OAuth2 flow by navigating to the backend endpoint
    // This will redirect to Keycloak login page
    await page.goto(oauth2Url, { waitUntil: 'networkidle' })

    // Debug: Log current URL after navigation
    console.log(`\x1B[2m\tCurrent URL after OAuth2 redirect: ${page.url()}\x1B[0m`)

    // Wait for Keycloak login page to load
    // Keycloak URL can vary: /realms/{realm}/protocol/openid-connect/auth
    // Try multiple possible URL patterns
    const keycloakUrlPatterns = [
        '**/realms/**/protocol/openid-connect/auth**',
        '**/auth/realms/**/protocol/openid-connect/auth**',
        '**/openid-connect/auth**',
    ]

    let keycloakPageLoaded = false
    for (const pattern of keycloakUrlPatterns) {
        try {
            console.log(`\x1B[2m\tTrying URL pattern: ${pattern}\x1B[0m`)
            await page.waitForURL(pattern, { timeout: 2000 })
            keycloakPageLoaded = true
            console.log(`\x1B[2m\tKeycloak login page loaded (matched pattern: ${pattern})\x1B[0m`)
            break
        }
        catch {
            // Try next pattern
            continue
        }
    }

    // If no pattern matched, check if we're already on a page with username/password fields
    if (!keycloakPageLoaded) {
        console.log(`\x1B[2m\tNo URL pattern matched. Checking for login form...\x1B[0m`)
        try {
            await page.getByRole('textbox', { name: /username/i }).waitFor({ timeout: 2000 })
            console.log(`\x1B[2m\tLogin form found on page: ${page.url()}\x1B[0m`)
            keycloakPageLoaded = true
        }
        catch {
            console.log(`\x1B[31m\tERROR: Could not find Keycloak login page\x1B[0m`)
            console.log(`\x1B[31m\tCurrent URL: ${page.url()}\x1B[0m`)
            throw new Error(`Keycloak login page not found. Current URL: ${page.url()}`)
        }
    }

    console.log(`\x1B[2m\tSigning in as '${process.env.USERNAME}'\x1B[0m`)

    // Fill in Keycloak login form
    await page.getByRole('textbox', { name: /username/i }).fill(process.env.USERNAME as string)
    await page.getByRole('textbox', { name: /password/i }).fill(process.env.PASSWORD as string)

    console.log(`\x1B[2m\tSubmitting login form\x1B[0m`)

    // Submit login form
    await page.getByRole('button', { name: 'Sign In' }).click()

    // Wait for redirect back to the frontend
    // After successful login, Keycloak redirects to backend, which sets session cookie
    // and redirects to the frontend
    console.log(`\x1B[2m\tWaiting for redirect to frontend...\x1B[0m`)
    await page.waitForURL(/localhost:5173/, { timeout: 15000 })

    console.log(`\x1B[2m\tSuccessfully redirected to frontend\x1B[0m`)
    console.log(`\x1B[2m\tSession cookie established\x1B[0m`)

    // Fetch CSRF token to ensure it's set in cookies
    console.log(`\x1B[2m\tFetching CSRF token...\x1B[0m`)
    await page.request.get(`${backendUrl}/bff/csrf`)
    console.log(`\x1B[2m\tCSRF token fetched\x1B[0m`)

    // Save the storage state including session cookies and CSRF token
    await page.context().storageState({ path: storageState })

    console.log(`\x1B[2m\tAuthentication completed and state saved\x1B[0m`)
})
