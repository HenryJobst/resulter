<script setup lang="ts">
import { type Ref, computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { VueQueryDevtools } from '@tanstack/vue-query-devtools'
import Toast from 'primevue/toast'
import Menubar from 'primevue/menubar'
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

const navItems = reactive([
    {
        show: true,
        route: { name: 'start-page', params: { locale } },
        label: computed(() => t('navigations.start')),
    },
    {
        show: true,
        route: { name: 'cup-list', params: { locale } },
        label: computed(() => t('navigations.cups')),
    },
    {
        show: true,
        route: { name: 'event-list', params: { locale } },
        label: computed(() => t('navigations.events')),
    },
    {
        show: authStore.isAdmin,
        route: { name: 'organisation-list', params: { locale } },
        label: computed(() => t('navigations.organisations')),
    },
    {
        show: authStore.isAdmin,
        route: { name: 'person-list', params: { locale } },
        label: computed(() => t('navigations.persons')),
    },
    {
        show: authStore.isAdmin,
        route: { name: 'media-list', params: { locale } },
        label: computed(() => t('navigations.media-files')),
    },
    {
        show: authStore.isAdmin,
        route: { name: 'certificate-list', params: { locale } },
        label: computed(() => t('navigations.certificates')),
    },
    {
        show: true,
        route: { name: 'about-page', params: { locale } },
        label: computed(() => t('navigations.about')),
    },
])
</script>

<template>
    <header class="bg-gray-200 p-4 h-full">
        <Menubar :model="navItems">
            <!-- Logo -->
            <template #start>
                <img
                    :alt="t('labels.logo')"
                    class="mr-6"
                    src="@/assets/Logo_Resulter_Circle.png"
                    width="60"
                    height="60"
                >
            </template>
            <!-- Navigation -->
            <template #item="{ item }">
                <router-link v-if="item.show" :to="item.route">
                    <span class="mr-4 text-2xl">{{ item.label }}</span>
                </router-link>
            </template>
            <template #end>
                <!-- Sprachauswahl und Logout -->
                <div class="flex flex-row flex-wrap justify-content-end">
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
            </template>
        </Menubar>
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
</template>

<style scoped>
.router-link-active {
    color: var(--default-font-color)
}
</style>
