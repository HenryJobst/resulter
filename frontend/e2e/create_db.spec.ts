import { test } from '@playwright/test'

test('create database', async ({ request }) => {

    const url = `/createDatabase`
    const response = await request.post(url)
    if (response.ok()) {
        console.log(`Database created with identifier: ${await response.text()}`)
    } else {
        throw new Error(`Failed to create database: ${response.status()} ${response.statusText()}`)
    }

})
