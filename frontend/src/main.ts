import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
// noinspection SpellCheckingInspection
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import { VueQueryPlugin } from '@tanstack/vue-query'
import AuthStorePlugin from '@/features/keycloak/plugins/authStorePlugin'
import keycloakService from '@/features/keycloak/services/keycloak'

import App from './App.vue'
import { setupRouter } from './router'
import { setupI18n } from '@/i18n'
import en from './locales/en.json'
import PrimeVue from 'primevue/config'

import 'primevue/resources/themes/bootstrap4-light-blue/theme.css'
import 'primeflex/primeflex.css'
import 'primeicons/primeicons.css'
import ToastService from 'primevue/toastservice'

const i18n = setupI18n({
  legacy: false,
  locale: 'en',
  fallbackLocale: 'de',
  messages: { en }
})

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

const router = setupRouter(i18n)

const renderApp = () => {
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
