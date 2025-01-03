import { URL, fileURLToPath } from 'node:url'

import { dirname, resolve } from 'node:path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import VueI18nPlugin from '@intlify/unplugin-vue-i18n/vite'
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
