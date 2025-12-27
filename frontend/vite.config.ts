import { dirname, resolve } from 'node:path'

import { fileURLToPath, URL } from 'node:url'
import VueI18nPlugin from '@intlify/unplugin-vue-i18n/vite'
import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'
import VueDevTools from 'vite-plugin-vue-devtools'
import packageInfo from './package.json'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        VueI18nPlugin({
            // locale messages resource pre-compile option
            include: resolve(dirname(fileURLToPath(import.meta.url)), './src/locales/**'),
        }),
        VueDevTools(),
    ],
    build: {
        outDir: 'dist',
    },
    server: {
        proxy: {
            // Proxy API requests to backend (for cookie support)
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
                // Configure proxy to forward X-DB-Identifier cookie as header (for E2E test isolation)
                configure: (proxy, _options) => {
                    proxy.on('proxyReq', (proxyReq, req, _res) => {
                        // Debug: Log all requests to see what's happening
                        console.log(`[Vite Proxy] Request to: ${req.url}`)
                        console.log(`[Vite Proxy] Cookies:`, req.headers.cookie || 'NO COOKIES')

                        // Read X-DB-Identifier cookie from request and add as header
                        const cookies = req.headers.cookie
                        if (cookies) {
                            const match = cookies.match(/X-DB-Identifier=([^;]+)/)
                            if (match && match[1]) {
                                proxyReq.setHeader('X-DB-Identifier', match[1])
                                console.log(`[Vite Proxy] Forwarding X-DB-Identifier: ${match[1]}`)
                            }
                            else {
                                console.log(`[Vite Proxy] X-DB-Identifier cookie not found in: ${cookies}`)
                            }
                        }
                    })
                },
            },
            // Proxy BFF user info endpoint to backend (for cookie support)
            '/bff': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
                // Configure proxy to forward X-DB-Identifier cookie as header (for E2E test isolation)
                configure: (proxy, _options) => {
                    proxy.on('proxyReq', (proxyReq, req, _res) => {
                        // Read X-DB-Identifier cookie from request and add as header
                        const cookies = req.headers.cookie
                        if (cookies) {
                            const match = cookies.match(/X-DB-Identifier=([^;]+)/)
                            if (match && match[1]) {
                                proxyReq.setHeader('X-DB-Identifier', match[1])
                            }
                        }
                    })
                },
            },
            // NOTE: /oauth2 and /login are NOT proxied
            // These need direct browser redirects to Keycloak
        },
    },
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)),
        },
    },
    define: {
        // Setzen der Versionsnummer als globale Konstante
        __APP_VERSION__: JSON.stringify(packageInfo.version),
    },
})
