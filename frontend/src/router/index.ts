import type { I18n } from 'vue-i18n'
import type { Router, RouteRecordRaw } from 'vue-router'
import { ToastEventBus } from 'primevue'
import { createRouter, createWebHistory } from 'vue-router'
import { aboutRouting } from '@/features/about/about-routing'
import { certificateRouting } from '@/features/certificate/certificate-routing'
import { cupRouting } from '@/features/cup/cup-routing'
import { eventRouting } from '@/features/event/event-routing'
import { imprintRouting } from '@/features/imprint/imprint-routing'
import { mediaRouting } from '@/features/media/media-routing'
import { organisationRouting } from '@/features/organisation/organisation-routing'
import { personRouting } from '@/features/person/person-routing'
import { startRouting } from '@/features/start/start-routing'
import { getLocale, loadLocaleMessages, setI18nLanguage, SUPPORT_LOCALES } from '@/i18n'

export function setupRouter(i18n: I18n): Router {
    const locale = getLocale(i18n)

    // setup routes
    const routes: RouteRecordRaw[] = [
        ...startRouting,
        ...aboutRouting,
        ...eventRouting,
        ...organisationRouting,
        ...personRouting,
        ...cupRouting,
        ...mediaRouting,
        ...certificateRouting,
        ...imprintRouting,
        {
            path: '/:pathMatch(.*)*',
            redirect: () => `/${locale}`,
        },
    ]

    // create router instance
    const router = createRouter({
        history: createWebHistory(),
        routes,
    })

    // navigation guards
    router.beforeEach(async (to) => {
        const paramsLocale = to.params.locale as string
        if (!SUPPORT_LOCALES.includes(paramsLocale))
            return `/${locale}`

        if (!i18n.global.availableLocales.includes(paramsLocale))
            await loadLocaleMessages(i18n, paramsLocale)

        setI18nLanguage(i18n, paramsLocale)
    })

    router.onError((error) => {
        const t: (msg_id: string, options?: object) => string = i18n.global.t
        console.error(error)
        ToastEventBus.emit({ severity: 'error', summary: t('messages.error', { message: error.message }), detail: error.stack })
    })

    return router
}
