import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import { setupRouter } from './router'
import { setupI18n } from '@/i18n'
import en from './locales/en.json'
import de from './locales/de.json'
import PrimeVue from 'primevue/config'

import 'primevue/resources/themes/bootstrap4-light-blue/theme.css'
import 'primeflex/primeflex.css'
import 'primeicons/primeicons.css'

const i18n = setupI18n({
  legacy: false,
  locale: 'en',
  fallbackLocale: 'de',
  messages: { en, de }
})

const router = setupRouter(i18n)

const app = createApp(App)
app.use(PrimeVue, { ripple: true })
app.use(createPinia())
app.use(i18n)
app.use(router)
app.mount('#app')
