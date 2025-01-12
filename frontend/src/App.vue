<script setup lang="ts">
import { type Ref, computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { VueQueryDevtools } from '@tanstack/vue-query-devtools'
import Toast from 'primevue/toast'
import Menubar from 'primevue/menubar'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
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

const showDetailsDialog = ref(false)
const currentDetails = ref('')

function showDetails(details: string) {
    currentDetails.value = details
    showDetailsDialog.value = true
}
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
        <Toast position="top-right">
            <template #message="slotProps">
                <div class="flex flex-column flex-auto">
                    <div class="flex flex-row mb-3 flex-auto align-items-center">
                        <svg width="14" height="14" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg" class="p-icon p-toast-message-icon" aria-hidden="true" data-pc-section="messageicon"><path fill-rule="evenodd" clip-rule="evenodd" d="M7 14C5.61553 14 4.26215 13.5895 3.11101 12.8203C1.95987 12.0511 1.06266 10.9579 0.532846 9.67879C0.00303296 8.3997 -0.13559 6.99224 0.134506 5.63437C0.404603 4.2765 1.07129 3.02922 2.05026 2.05026C3.02922 1.07129 4.2765 0.404603 5.63437 0.134506C6.99224 -0.13559 8.3997 0.00303296 9.67879 0.532846C10.9579 1.06266 12.0511 1.95987 12.8203 3.11101C13.5895 4.26215 14 5.61553 14 7C14 8.85652 13.2625 10.637 11.9497 11.9497C10.637 13.2625 8.85652 14 7 14ZM7 1.16667C5.84628 1.16667 4.71846 1.50879 3.75918 2.14976C2.79989 2.79074 2.05222 3.70178 1.61071 4.76768C1.16919 5.83358 1.05367 7.00647 1.27876 8.13803C1.50384 9.26958 2.05941 10.309 2.87521 11.1248C3.69102 11.9406 4.73042 12.4962 5.86198 12.7212C6.99353 12.9463 8.16642 12.8308 9.23232 12.3893C10.2982 11.9478 11.2093 11.2001 11.8502 10.2408C12.4912 9.28154 12.8333 8.15373 12.8333 7C12.8333 5.45291 12.2188 3.96918 11.1248 2.87521C10.0308 1.78125 8.5471 1.16667 7 1.16667ZM4.66662 9.91668C4.58998 9.91704 4.51404 9.90209 4.44325 9.87271C4.37246 9.84333 4.30826 9.8001 4.2544 9.74557C4.14516 9.6362 4.0838 9.48793 4.0838 9.33335C4.0838 9.17876 4.14516 9.0305 4.2544 8.92113L6.17553 7L4.25443 5.07891C4.15139 4.96832 4.09529 4.82207 4.09796 4.67094C4.10063 4.51982 4.16185 4.37563 4.26872 4.26876C4.3756 4.16188 4.51979 4.10066 4.67091 4.09799C4.82204 4.09532 4.96829 4.15142 5.07887 4.25446L6.99997 6.17556L8.92106 4.25446C9.03164 4.15142 9.1779 4.09532 9.32903 4.09799C9.48015 4.10066 9.62434 4.16188 9.73121 4.26876C9.83809 4.37563 9.89931 4.51982 9.90198 4.67094C9.90464 4.82207 9.84855 4.96832 9.74551 5.07891L7.82441 7L9.74554 8.92113C9.85478 9.0305 9.91614 9.17876 9.91614 9.33335C9.91614 9.48793 9.85478 9.6362 9.74554 9.74557C9.69168 9.8001 9.62748 9.84333 9.55669 9.87271C9.4859 9.90209 9.40996 9.91704 9.33332 9.91668C9.25668 9.91704 9.18073 9.90209 9.10995 9.87271C9.03916 9.84333 8.97495 9.8001 8.9211 9.74557L6.99997 7.82444L5.07884 9.74557C5.02499 9.8001 4.96078 9.84333 4.88999 9.87271C4.81921 9.90209 4.74326 9.91704 4.66662 9.91668Z" fill="currentColor" /></svg>
                        <div class="p-toast-message-text ml-2" data-pc-section="messagetext">
                            <span class="p-toast-summary" data-pc-section="summary">{{ slotProps.message.summary }}</span>
                        </div>
                    </div>
                    <div class="flex flex-row">
                        <Button :label="$t('messages.details')" severity="secondary" size="small" variant="text" outlined @click="showDetails(slotProps.message.detail)" />
                    </div>
                </div>
            </template>
        </Toast>
        <Dialog v-model:visible="showDetailsDialog" :header="$t('messages.detail')" modal dismissable-mask :style="{ width: '25rem' }" :breakpoints="{ '1199px': '75vw', '575px': '90vw' }">
            <p>{{ currentDetails }}</p>
        </Dialog>
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
</style>
