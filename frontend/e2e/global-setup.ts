import type { APIResponse } from '@playwright/test'
import { request } from '@playwright/test'
import process from 'node:process'

async function globalSetup() {
    // Read secrets from cloud and set as env vars

    // GET SERVICE API AUTH TOKEN
    try {
        const resp: APIResponse = await (
                await request.newContext()
        ).post(process.env.AUTH_TOKEN_URL as string, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            form: {
                client_id: process.env.TEST_CLIENT_ID as string,
                //scope: process.env.SCOPE as string,
                client_secret: process.env.TEST_CLIENT_SECRET as string
            },
            timeout: 300000
        })

        const respJson = await resp.json()
        // set auth token as env variable
        process.env.AUTH_TOKEN = respJson.access_token

    } catch (e) {
        console.error('Unable to authenticate. Occurred error: ' + e)
    }
}

export default globalSetup
