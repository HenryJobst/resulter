import process from 'node:process'
import { defineConfig, devices } from '@playwright/test'

import * as dotenv from 'dotenv'

dotenv.config({
    path: './e2e/.env.local',
})

const hostname = process.env.HOSTNAME
console.log(`Hostname: ${hostname}`)
const frontend_protocol = process.env.FRONTEND_PROTOCOL
console.log(`Frontend protocol: ${frontend_protocol}`)
const port = process.env.PORT
console.log(`Port: ${port}`)
const backend_protocol = process.env.BACKEND_PROTOCOL
console.log(`Backend protocol: ${backend_protocol}`)
const backend_port = process.env.BACKEND_PORT
console.log(`Backend port: ${backend_port}`)
const backend_profiles = process.env.BACKEND_PROFILES
console.log(`Backend profiles: ${backend_profiles}`)
const vite_mode = process.env.VITE_MODE
console.log(`Vite mode: ${vite_mode}`)

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
    testDir: './e2e',
    /* Run tests in files in parallel */
    fullyParallel: true,
    /* Fail the build on CI if you accidentally left test.only in the source code. */
    forbidOnly: !!process.env.CI,
    /* Retry on CI only */
    retries: process.env.CI ? 2 : 0,
    /* Opt out of parallel tests on CI. */
    workers: process.env.CI ? 1 : undefined,
    /* Reporter to use. See https://playwright.dev/docs/test-reporters */
    reporter: 'html',
    /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
    use: {
        /* Base URL to use in actions like `await page.goto('/')`. */
        baseURL: `${frontend_protocol}://${hostname}:${port}`,

        /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
        trace: 'on-first-retry',

        // locale: 'de-DE',
        // timezoneId: 'Europe/Berlin',
    },

    /* Configure projects for major browsers */
    projects: [
        {
            name: 'setup',
            testMatch: 'e2e/auth-setup.ts',
        },
        {
            name: 'chromium',
            use: {
                ...devices['Desktop Chrome'],
                storageState: 'e2e/.auth/storageState.json',
            },
            dependencies: ['setup'],
        },

        {
            name: 'firefox',
            use: {
                ...devices['Desktop Firefox'],
                storageState: 'e2e/.auth/storageState.json',
            },
            dependencies: ['setup'],
        },

        {
            name: 'webkit',
            use: {
                ...devices['Desktop Safari'],
                storageState: 'e2e/.auth/storageState.json',
            },
            dependencies: ['setup'],
        },

        {
            name: 'msedge',
            use: {
                channel: 'msedge',
                storageState: 'e2e/.auth/storageState.json',
            },
            dependencies: ['setup'],
        },

        /* Test against mobile viewports. */
        // {
        //   name: 'Mobile Chrome',
        //   use: { ...devices['Pixel 5'] },
        // },
        // {
        //   name: 'Mobile Safari',
        //   use: { ...devices['iPhone 12'] },
        // },

        /* Test against branded browsers. */
        // {
        //   name: 'Microsoft Edge',
        //   use: { ...devices['Desktop Edge'], channel: 'msedge' },
        // },
        // {
        //   name: 'Google Chrome',
        //   use: { ...devices['Desktop Chrome'], channel: 'chrome' },
        // },
    ],

    /* Run your local dev server before starting the tests */
    webServer: [
        {
            command: `pnpm ./node_modules/vite/bin/vite.js --mode ${vite_mode} --host ${hostname} --port ${port}`,
            url: `${frontend_protocol}://${hostname}:${port}`,
            timeout: 120 * 1000,
            reuseExistingServer: !process.env.CI,
            ignoreHTTPSErrors: true,
        },
        {
            command: `mvn compile exec:java -D exec.mainClass="de.jobst.resulter.ResulterApplication" -Dspring.profiles.active=${backend_profiles}`,
            url: `${backend_protocol}://${hostname}:${backend_port}`,
            timeout: 120 * 1000,
            reuseExistingServer: !process.env.CI,
            cwd: '../',
            ignoreHTTPSErrors: true,
        },
    ],
})
