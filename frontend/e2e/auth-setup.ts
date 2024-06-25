import * as fs from 'node:fs'
import process from 'node:process'
import { test } from '@playwright/test'
import * as dotenv from 'dotenv'

// we don't want to store credentials in the repository
dotenv.config({
    path: './e2e/.env.local',
})

const storageState = 'e2e/.auth/storageState.json'

test('authenticate user', async ({ page }) => {
    if (process.env.username === '**REMOVED**') {
        throw new Error('Env file is not correct')
    }

    const stats = fs.existsSync(storageState!.toString()) ? fs.statSync(storageState!.toString()) : null
    if (stats && stats.mtimeMs > new Date().getTime() - 600000) {
        console.log(`\x1B[2m\tSign in skipped because token is fresh\x1B[0m`)
        return
    }

    console.log(`\x1B[2m\tSign in started'\x1B[0m`)

    // when we're not authenticated, the app redirects to the login page
    await page.goto('/en')
    await page.getByRole('link', { name: 'login' }).click()
    // await page.waitForURL('**/openid-connect/auth')

    // console.log(`\x1b[2m\tSelect English'\x1b[0m`)
    // await page.getByRole('list')..getByRole('link', { name: 'English' }).click()

    console.log(`\x1B[2m\tSign in as '${process.env.username}'\x1B[0m`)

    await page.getByRole('textbox', { name: /username/i }).fill(process.env.username as string)
    await page.getByRole('textbox', { name: /password/i }).fill(process.env.password as string)

    // await page.getByRole('textbox', { name: /username/i }).fill(process.env.username as string)
    // await page.getByLabel('Password').fill(process.env.password as string)

    console.log(`\x1B[2m\tSign in processing\x1B[0m`)

    await page.getByRole('button', { name: 'Sign In' }).click()

    console.log(`\x1B[2m\tSign in processed\x1B[0m`)

    await page.context().storageState({ path: storageState })
})
