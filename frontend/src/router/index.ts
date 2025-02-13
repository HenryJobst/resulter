import type { RouteRecordRaw, Router } from 'vue-router'
import { createRouter, createWebHistory } from 'vue-router'
import { ToastEventBus } from 'primevue'
import type { I18n } from 'vue-i18n'
import { SUPPORT_LOCALES, getLocale, loadLocaleMessages, setI18nLanguage } from '@/i18n'
import { startRouting } from '@/features/start/start-routing'
import { aboutRouting } from '@/features/about/about-routing'
import { eventRouting } from '@/features/event/event-routing'
import { organisationRouting } from '@/features/organisation/organisation-routing'
import { personRouting } from '@/features/person/person-routing'
import { cupRouting } from '@/features/cup/cup-routing'
import { mediaRouting } from '@/features/media/media-routing'
import { certificateRouting } from '@/features/certificate/certificate-routing'

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
