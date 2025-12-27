import { test } from '@playwright/test'
import { createTestDatabase } from './helpers/database'

test('create database', async () => {
    const dbIdentifier = await createTestDatabase()
    console.log(`Created test database with identifier: ${dbIdentifier}`)
})
