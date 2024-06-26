<script setup lang="ts">
import { type Ref, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { VueQueryDevtools } from '@tanstack/vue-query-devtools'
import Toast from 'primevue/toast'
import { type PrimeVueLocaleOptions, usePrimeVue } from 'primevue/config'
import moment from 'moment/min/moment-with-locales'
import { SUPPORT_LOCALES } from './i18n'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import BackendVersion from '@/features/backend_version/BackendVersion.vue'

const router = useRouter()
const { t, locale } = useI18n()
const authStore = useAuthStore()

const currentLocale = ref(locale.value)
const fullUrl = ref('')

interface LocaleMessages {
    [key: string]: PrimeVueLocaleOptions
}

const primeVueLocales: Ref<LocaleMessages> = ref({})

const primevue = usePrimeVue()

const frontendVersion = __APP_VERSION__

onMounted(() => {
    fullUrl.value = cleanUrl(window.location.href)
})

// Synchronisation, um die Lokalisierung vom Router-Pfad zu ändern
watch(router.currentRoute, (route) => {
    currentLocale.value = route.params.locale as string
    fullUrl.value = cleanUrl(window.location.href)
})

function cleanUrl(url: string) {
    const urlObj = new URL(url)
    // delete router state or error parameters
    urlObj.hash = ''
    // Optional: delete query parameters, if desired
    // urlObj.search = '';
    return urlObj.toString()
}

async function switchPrimeVueLocale(locale: string) {
    if (!locale)
        return
    // noinspection SpellCheckingInspection
    if (!primeVueLocales.value[locale]) {
        try {
            // noinspection TypeScriptCheckImport
            const module = await import(`./locales/primevue/${locale}.json`)
            primeVueLocales.value[locale] = module.default[locale]
        }
        catch (error) {
            console.error('Failed to load locale:', error)
        }
    }

    const primeVueLocale = primeVueLocales.value[locale]
    Object.assign(primevue.config.locale!, primeVueLocale)
}

watch(currentLocale, (val) => {
    router.push({
        name: router.currentRoute.value.name!,
        params: { locale: val },
    })
    switchPrimeVueLocale(val)
    moment.locale(val)

    localStorage.setItem('userLocale', val)
})
</script>

<template>
    <div class="flex flex-col h-full">
        <header class="flex justify-between items-center bg-gray-200 p-4">
            <!-- Logo und Menüeinträge -->
            <div class="flex items-center">
                <img
                    :alt="t('labels.logo')"
                    class="mr-6"
                    src="@/assets/Logo_Resulter.png"
                    width="60"
                    height="60"
                >
                <nav>
                    <ul class="flex flex-row flex-wrap text-2xl">
                        <li class="mr-4">
                            <router-link :to="{ name: 'start-page', params: { locale } }">
                                {{ t('navigations.start') }}
                            </router-link>
                        </li>
                        <li class="mr-4">
                            <router-link :to="{ name: 'cup-list', params: { locale } }">
                                {{ t('navigations.cups') }}
                            </router-link>
                        </li>
                        <li class="mr-4">
                            <router-link :to="{ name: 'event-list', params: { locale } }">
                                {{ t('navigations.events') }}
                            </router-link>
                        </li>
                        <li v-if="authStore.isAdmin" class="mr-4">
                            <router-link :to="{ name: 'organisation-list', params: { locale } }">
                                {{ t('navigations.organisations') }}
                            </router-link>
                        </li>
                        <li v-if="authStore.isAdmin" class="mr-4">
                            <router-link :to="{ name: 'person-list', params: { locale } }">
                                {{ t('navigations.persons') }}
                            </router-link>
                        </li>
                        <li v-if="authStore.isAdmin" class="mr-4">
                            <router-link :to="{ name: 'media-list', params: { locale } }">
                                {{ t('navigations.media-files') }}
                            </router-link>
                        </li>
                        <li v-if="authStore.isAdmin" class="mr-4">
                            <router-link :to="{ name: 'certificate-list', params: { locale } }">
                                {{ t('navigations.certificates') }}
                            </router-link>
                        </li>
                        <li class="mr-4">
                            <router-link :to="{ name: 'about-page', params: { locale } }">
                                {{ t('navigations.about') }}
                            </router-link>
                        </li>
                        <!-- Weitere Menüeinträge hier hinzufügen -->
                    </ul>
                </nav>
            </div>
            <!-- Sprachauswahl -->
            <div class="flex flex-row flex-wrap">
                <a
                    v-if="!authStore.isAuthenticated"
                    href="#"
                    class="text-xl mr-4"
                    @click="authStore.login(fullUrl, currentLocale)"
                >
                    {{ t('navigations.login') }}
                </a>
                <a
                    v-if="authStore.isAuthenticated"
                    href="#"
                    class="text-xl mr-4"
                    @click="authStore.logout()"
                >
                    {{ t('navigations.logout') }}
                </a>
                <div class="flex flex-row flex-nowrap">
                    <label class="mr-2 mt-1" for="locale-select">{{ t('labels.language') }}</label>
                    <select id="locale-select" v-model="currentLocale" class="form-select">
                        <option
                            v-for="optionLocale in SUPPORT_LOCALES"
                            :key="optionLocale"
                            :value="optionLocale"
                        >
                            {{ optionLocale }}
                        </option>
                    </select>
                </div>
            </div>
        </header>

        <div class="flex-1 m-4">
            <Toast />
            <router-view />
            <VueQueryDevtools />
        </div>

        <footer class="flex justify-between items-center bg-gray-200 p-4">
            <div class="flex items-center">
                <div>
                    {{ t('labels.frontend_version', { version: frontendVersion }) }}
                </div>
                <div class="ml-4">
                    <BackendVersion />
                </div>
                <div v-if="authStore.authenticated" class="ml-4">
                    {{ t('labels.login_user', { username: authStore.user.username }) }}
                </div>
            </div>
        </footer>
    </div>
</template>

<style scoped></style>
