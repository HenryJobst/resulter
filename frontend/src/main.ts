import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
// noinspection SpellCheckingInspection
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import { VueQueryPlugin } from '@tanstack/vue-query'
import PrimeVue from 'primevue/config'
import ToastService from 'primevue/toastservice'
import App from './App.vue'
import { setupRouter } from './router'
import en from './locales/en.json'
import AuthStorePlugin from '@/features/keycloak/plugins/authStorePlugin'
import keycloakService from '@/features/keycloak/services/keycloak'

import { setupI18n } from '@/i18n'

import 'primevue/resources/themes/bootstrap4-light-blue/theme.css'
import 'primeflex/primeflex.css'
import 'primeicons/primeicons.css'

const i18n = setupI18n({
    legacy: false,
    locale: 'en',
    fallbackLocale: 'de',
    messages: { en },
})

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

const router = setupRouter(i18n)

function renderApp() {
    const app = createApp(App)
    app.use(PrimeVue, { ripple: true, locale: i18n.global.locale })
    app.use(AuthStorePlugin, { pinia })
    app.use(VueQueryPlugin)
    app.use(ToastService)
    app.use(pinia)
    app.use(i18n)
    app.use(router)
    app.mount('#app')
}

keycloakService.callInit(renderApp).then()
