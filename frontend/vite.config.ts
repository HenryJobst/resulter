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
            },
            // Proxy BFF user info endpoint to backend (for cookie support)
            '/bff': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
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
