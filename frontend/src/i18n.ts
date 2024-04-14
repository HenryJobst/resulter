import { isRef, nextTick, unref } from 'vue'
import type { Composer, I18n, I18nMode, I18nOptions, Locale, VueI18n } from 'vue-i18n'
import { createI18n } from 'vue-i18n'
import { usePrimeVue } from 'primevue/config'
import { primevueLocaleMessages } from '@/utils/primevueMessages'
import { changeLocale } from '@formkit/vue'

export const SUPPORT_LOCALES = ['en', 'de']

function isComposer(instance: VueI18n | Composer, mode: I18nMode): instance is Composer {
    return mode === 'composition' && isRef(instance.locale)
}

export function getLocale(i18n: I18n): string {
    if (isComposer(i18n.global, i18n.mode))
        return i18n.global.locale.value
    else return i18n.global.locale
}

export function setLocale(i18n: I18n, locale: Locale): void {
    if (isComposer(i18n.global, i18n.mode)) {
        i18n.global.locale.value = locale
    } else {
        i18n.global.locale = locale
    }

    // set primevue locale
    try {
        const { config } = usePrimeVue()
        config.locale = primevueLocaleMessages(unref(locale) as string)
    } catch (e) {
    }

    changeLocale(locale)
}

export function setupI18n(options: I18nOptions = { locale: 'en' }): I18n {
    const i18n = createI18n(options)
    setI18nLanguage(i18n, options.locale!)
    return i18n
}

export function setI18nLanguage(i18n: I18n, locale: Locale): void {
    setLocale(i18n, locale)
    /**
     * NOTE:
     * If you need to specify the language setting for headers, such as the `fetch` API, set it here.
     * The following is an example for axios.
     *
     * axios.defaults.headers.common['Accept-Language'] = locale
     */
    document.querySelector('html')!.setAttribute('lang', locale)
}

const getResourceMessages = (r: any) => r.default || r

export async function loadLocaleMessages(i18n: I18n, locale: Locale) {
    // load locale messages
    const messages = await import(`./locales/${locale}.json`).then(getResourceMessages)

    // set locale and locale message
    i18n.global.setLocaleMessage(locale, messages)


    return nextTick()
}
