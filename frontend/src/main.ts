import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
// noinspection SpellCheckingInspection
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import { VueQueryPlugin } from '@tanstack/vue-query'
import messages from '@intlify/unplugin-vue-i18n/messages'
import App from './App.vue'
import { setupRouter } from './router'
import AuthStorePlugin from '@/features/keycloak/plugins/authStorePlugin'
import keycloakService from '@/features/keycloak/services/keycloak'

import { setupI18n } from '@/i18n'

import 'primevue/resources/themes/bootstrap4-light-blue/theme.css'
import 'primeflex/primeflex.css'
import 'primeicons/primeicons.css'

import '@sfxcode/formkit-primevue/dist/sass/formkit-primevue.scss'
import { primevueInstall } from '@/primevue'
import { formkitInstall } from '@/formkit'

const savedLocale = localStorage.getItem('userLocale')
// get user language from browser
const userLanguage = navigator.language || navigator.languages[0] || 'en'
// extract language without region code
const defaultLocale = savedLocale || userLanguage.split('-')[0]

const i18n = setupI18n({
    legacy: false,
    locale: defaultLocale,
    fallbackLocale: 'en',
    messages,
})

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

const router = setupRouter(i18n)

function renderApp() {
    const app = createApp(App)
    app.use(AuthStorePlugin, { pinia })
    app.use(VueQueryPlugin)
    app.use(pinia)
    app.use(i18n)
    app.use(router)

    primevueInstall(app, i18n)
    formkitInstall(app, i18n)

    app.mount('#app')
}

keycloakService.callInit(renderApp).then()
