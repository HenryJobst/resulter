import type { Router, RouteRecordRaw } from 'vue-router'
import { createRouter, createWebHistory } from 'vue-router'
import { getLocale, loadLocaleMessages, setI18nLanguage, SUPPORT_LOCALES } from '../i18n'
import type { I18n } from 'vue-i18n'
import { startRouting } from '@/features/start/start-routing'
import { aboutRouting } from '@/features/about/about-routing'
import { eventRouting } from '@/features/event/event-routing'

export function setupRouter(i18n: I18n): Router {
  const locale = getLocale(i18n)

  // setup routes
  const routes: RouteRecordRaw[] = [
    ...startRouting,
    ...aboutRouting,
    ...eventRouting,
    {
      path: '/:pathMatch(.*)*',
      redirect: () => `/${locale}`
    }
  ]

  // create router instance
  const router = createRouter({
    history: createWebHistory(),
    routes
  })

  // navigation guards
  router.beforeEach(async (to) => {
    const paramsLocale = to.params.locale as string
    if (!SUPPORT_LOCALES.includes(paramsLocale)) {
      return `/${locale}`
    }
    if (!i18n.global.availableLocales.includes(paramsLocale)) {
      await loadLocaleMessages(i18n, paramsLocale)
    }
    setI18nLanguage(i18n, paramsLocale)
  })

  return router
}