import messages from '@intlify/unplugin-vue-i18n/messages'

import { definePreset } from '@primeuix/themes'
import Lara from '@primeuix/themes/lara'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import { AxiosError } from 'axios'
import { createPinia } from 'pinia'
// noinspection SpellCheckingInspection
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import PrimeVue from 'primevue/config'
import ToastService from 'primevue/toastservice'
import Tooltip from 'primevue/tooltip'
import { createApp, unref } from 'vue'
import AuthStorePlugin from '@/features/auth/plugins/authStorePlugin'
import { getErrorStore } from '@/features/common/stores/getErrorStore'
import { setupI18n } from '@/i18n'
import { primevueLocaleMessages } from '@/PrimevueMessages'

import App from './App.vue'

import { setupRouter } from './router'
import './assets/main.css'
import 'primeflex/primeflex.css'
import 'primeicons/primeicons.css'
import 'flag-icons/css/flag-icons.min.css'

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

export const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

const router = setupRouter(i18n)

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 1000 * 5, // 5 seconds - prevents duplicate requests on fast navigation
        },
    },
})

const MyPreset = definePreset(Lara, {
    semantic: {
        primary: {
            50: '{orange.50}',
            100: '{orange.100}',
            200: '{orange.200}',
            300: '{orange.300}',
            400: '{orange.400}',
            500: '{orange.500}',
            600: '{orange.600}',
            700: '{orange.700}',
            800: '{orange.800}',
            900: '{orange.900}',
            950: '{orange.950}',
        },
        colorScheme: {
            light: {
                surface: {
                    0: '#ffffff',
                    50: '{orange.50}',
                    100: '{orange.100}',
                    200: '{orange.200}',
                    300: '{orange.300}',
                    400: '{orange.400}',
                    500: '{orange.500}',
                    600: '{orange.600}',
                    700: '{orange.700}',
                    800: '{orange.800}',
                    900: '{orange.900}',
                    950: '{orange.950}',
                },
            },
            dark: {
                surface: {
                    0: '#ffffff',
                    50: '{slate.50}',
                    100: '{slate.100}',
                    200: '{slate.200}',
                    300: '{slate.300}',
                    400: '{slate.400}',
                    500: '{slate.500}',
                    600: '{slate.600}',
                    700: '{slate.700}',
                    800: '{slate.800}',
                    900: '{slate.900}',
                    950: '{slate.950}',
                },
            },
        },
    },
})

function mergePrimeVueLocale(base: any, override: any) {
    if (!base)
        return override
    const result = { ...base, ...override }
    if (base.aria || override?.aria) {
        result.aria = { ...(base.aria || {}), ...(override?.aria || {}) }
    }
    return result
}

export function renderApp() {
    const app = createApp(App)
    app.directive('tooltip', Tooltip)
    app.use(PrimeVue, {
        ripple: true,
        locale: primevueLocaleMessages[unref(i18n.global.locale) as string],
        theme: {
            preset: MyPreset,
            options: {
                prefix: 'p',
                darkModeSelector: 'system',
                cssLayer: false,
            },
        },
    })
    // Ensure PrimeVue locale keeps defaults while applying our overrides
    const primevue = app.config.globalProperties.$primevue
    if (primevue?.config) {
        primevue.config.locale = mergePrimeVueLocale(primevue.config.locale, primevueLocaleMessages[unref(i18n.global.locale) as string])
    }
    app.use(AuthStorePlugin, { pinia, router })
    app.use(VueQueryPlugin, { queryClient })
    app.use(ToastService)
    app.use(pinia)
    app.use(i18n)
    app.use(router)
    app.mount('#app')

    // Global error handler
    app.config.errorHandler = (err) => {
        if (err instanceof AxiosError) {
            const axiosError = err as AxiosError
            if (axiosError?.isAxiosError) {
                // ignore error, error reporting is already handled by the axios token interceptor
                return
            }
        }

        console.error('Global error:', err)
        const errorStore = getErrorStore()
        errorStore.addError(err)
        if (router.currentRoute.value.name !== 'start-page') {
            router.push({ name: 'start-page' }).catch(() => {
                /* ignore to prevent error loop */
            })
        }
    }

    return app
}

// BFF mode: render app immediately, auth check happens after mount via AuthStorePlugin
renderApp()
