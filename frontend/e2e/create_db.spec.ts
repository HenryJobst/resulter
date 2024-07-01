import process from 'node:process'
import { test } from '@playwright/test'

const backend_protocol = process.env.BACKEND_PROTOCOL || 'http'
const backend_host = process.env.BACKEND_HOST || 'localhost'
const backend_port = process.env.BACKEND_PORT || 8080

test('create database', async ({ request }) => {
    await request.post(`${backend_protocol}://${backend_host}:${backend_port}/createDatabase`, { timeout: 100000 })
})
