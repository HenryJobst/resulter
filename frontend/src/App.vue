<script setup lang="ts">
import type { PrimeVueLocaleOptions } from 'primevue/config'
import type { MenuItem } from 'primevue/menuitem'
import type { Ref } from 'vue'
import { VueQueryDevtools } from '@tanstack/vue-query-devtools'
import moment from 'moment/min/moment-with-locales'
import Button from 'primevue/button'
import { usePrimeVue } from 'primevue/config'
import ConfirmDialog from 'primevue/confirmdialog'
import Menubar from 'primevue/menubar'
import Select from 'primevue/select'
import Toast from 'primevue/toast'
import { onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import BackendVersion from '@/features/backend_version/BackendVersion.vue'
import MessageDetailDialog from '@/features/common/components/MessageDetailDialog.vue'

import { useMessageDetailStore } from '@/features/common/stores/useMessageDetailStore'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { getFlagClass, SUPPORT_LOCALES } from './i18n'

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

function showImprint() {
    router.push({ name: 'imprint-page', params: { locale: currentLocale.value } })
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

const navItems = ref<Array<MenuItem>>([
    {
        key: 'start',
        label: () => t('navigations.start'),
        command: () => {
            router.push({ name: 'start-page', params: { locale: currentLocale.value } })
        },
        class: 'text-2xl',
    },
    {
        key: 'cups',
        label: () => t('navigations.cups'),
        command: () => {
            router.push({ name: 'cup-list', params: { locale: currentLocale.value } })
        },
        class: 'text-2xl',
    },
    {
        key: 'events',
        label: () => t('navigations.events'),
        command: () => {
            router.push({ name: 'event-list', params: { locale: currentLocale.value } })
        },
        class: 'text-2xl',
    },
    {
        key: 'organisations',
        visible: !!authStore.isAdmin,
        label: () => t('navigations.organisations'),
        command: () => {
            router.push({ name: 'organisation-list', params: { locale: currentLocale.value } })
        },
        class: 'text-2xl',
    },
    {
        key: 'users',
        visible: !!authStore.isAdmin,
        label: () => t('navigations.users'),
        command: () => {
            router.push({ name: 'user-list', params: { locale: currentLocale.value } })
        },
        class: 'text-2xl',
    },
    {
        key: 'roles',
        visible: !!authStore.isAdmin,
        label: () => t('navigations.roles'),
        command: () => {
            router.push({ name: 'role-list', params: { locale: currentLocale.value } })
        },
        class: 'text-2xl',
    },
    {
        key: 'groups',
        visible: !!authStore.isAdmin,
        label: () => t('navigations.groups'),
        command: () => {
            router.push({ name: 'group-list', params: { locale: currentLocale.value } })
        },
        class: 'text-2xl',
    },
    {
        key: 'persons',
        visible: !!authStore.isAdmin,
        label: () => t('navigations.persons'),
        command: () => {
            router.push({ name: 'person-list', params: { locale: currentLocale.value } })
        },
        class: 'text-2xl',
    },
    {
        key: 'media',
        visible: !!authStore.isAdmin,
        label: () => t('navigations.media-files'),
        command: () => {
            router.push({ name: 'media-list', params: { locale: currentLocale.value } })
        },
        class: 'text-2xl',
    },
    {
        key: 'certificates',
        visible: !!authStore.isAdmin,
        label: () => t('navigations.certificates'),
        command: () => {
            router.push({ name: 'certificate-list', params: { locale: currentLocale.value } })
        },
        class: 'text-2xl',
    },
    {
        key: 'about',
        label: () => t('navigations.about'),
        command: () => {
            router.push({ name: 'about-page', params: { locale: currentLocale.value } })
        },
        class: 'text-2xl',
    },
])

const messageDetailStore = useMessageDetailStore()

function setDetails(details: string) {
    if (details) {
        messageDetailStore.show(details)
    }
    else {
        messageDetailStore.hide()
    }
}
</script>

<template>
    <header class="p-4 h-full">
        <Menubar :model="navItems">
            <!-- Logo -->
            <template #start>
                <img
                    :alt="t('labels.logo')"
                    class="mr-6"
                    src="@/assets/Logo_Resulter_60px.webp"
                    width="60"
                    height="59"
                    type="image/webp"
                    fetchpriority="high"
                >
            </template>
            <!-- Navigation -->
            <!-- template #item="{ item }">
                <router-link v-if="item.show" :to="item.route">
                    <span class="mr-4 text-2xl">{{ item.label }}</span>
                </router-link>
            </template -->
            <template #end>
                <!-- Sprachauswahl und Logout -->
                <div class="flex flex-row flex-wrap justify-content-end align-items-center">
                    <a
                        v-if="!authStore.isAuthenticated"
                        href="#"
                        class="text-2xl mr-4"
                        @click="authStore.login(fullUrl, currentLocale)"
                    >
                        {{ t('navigations.login') }}
                    </a>
                    <a
                        v-if="authStore.isAuthenticated"
                        href="#"
                        class="text-2xl mr-4"
                        @click="authStore.logout()"
                    >
                        {{ t('navigations.logout') }}
                    </a>
                    <div class="flex flex-row flex-nowrap align-items-center">
                        <Select
                            v-model="currentLocale"
                            :options="SUPPORT_LOCALES"
                            class="w-full"
                            style="border: none; background: none; outline: none; outline-color: currentColor; box-shadow: none"
                            :aria-label="t('labels.select_language')"
                        >
                            <template #value="{ value }">
                                <span :class="`${getFlagClass(value)}`" />
                                <span class="sr-only">{{ value }}</span>
                            </template>
                            <template #option="{ option }">
                                <span :class="`mr-2 ${getFlagClass(option)}`" />
                                <span class="sr-only">{{ option }}</span>
                                <div>{{ option }}</div>
                            </template>
                            <template #dropdownicon>
                                <i class="pi pi-language" />
                            </template>
                        </Select>
                    </div>
                </div>
            </template>
        </Menubar>
    </header>

    <div class="flex-1 m-4">
        <Toast position="top-right">
            <template #message="slotProps">
                <div class="flex flex-column flex-auto">
                    <div class="flex flex-row mb-2 flex-auto align-items-center">
                        <svg
                            v-if="slotProps.message.severity === 'warn'"
                            width="14"
                            height="14"
                            viewBox="0 0 14 14"
                            fill="none"
                            xmlns="http://www.w3.org/2000/svg"
                            class="p-icon p-toast-message-icon"
                            aria-hidden="true"
                            data-pc-section="messageicon"
                        >
                            <path
                                d="M13.4018 13.1893H0.598161C0.49329 13.189 0.390283 13.1615 0.299143 13.1097C0.208003 13.0578 0.131826 12.9832 0.0780112 12.8932C0.0268539 12.8015 0 12.6982 0 12.5931C0 12.4881 0.0268539 12.3848 0.0780112 12.293L6.47985 1.08982C6.53679 1.00399 6.61408 0.933574 6.70484 0.884867C6.7956 0.836159 6.897 0.810669 7 0.810669C7.103 0.810669 7.2044 0.836159 7.29516 0.884867C7.38592 0.933574 7.46321 1.00399 7.52015 1.08982L13.922 12.293C13.9731 12.3848 14 12.4881 14 12.5931C14 12.6982 13.9731 12.8015 13.922 12.8932C13.8682 12.9832 13.792 13.0578 13.7009 13.1097C13.6097 13.1615 13.5067 13.189 13.4018 13.1893ZM1.63046 11.989H12.3695L7 2.59425L1.63046 11.989Z"
                                fill="currentColor"
                            />
                            <path
                                d="M6.99996 8.78801C6.84143 8.78594 6.68997 8.72204 6.57787 8.60993C6.46576 8.49782 6.40186 8.34637 6.39979 8.18784V5.38703C6.39979 5.22786 6.46302 5.0752 6.57557 4.96265C6.68813 4.85009 6.84078 4.78686 6.99996 4.78686C7.15914 4.78686 7.31179 4.85009 7.42435 4.96265C7.5369 5.0752 7.60013 5.22786 7.60013 5.38703V8.18784C7.59806 8.34637 7.53416 8.49782 7.42205 8.60993C7.30995 8.72204 7.15849 8.78594 6.99996 8.78801Z"
                                fill="currentColor"
                            />
                            <path
                                d="M6.99996 11.1887C6.84143 11.1866 6.68997 11.1227 6.57787 11.0106C6.46576 10.8985 6.40186 10.7471 6.39979 10.5885V10.1884C6.39979 10.0292 6.46302 9.87658 6.57557 9.76403C6.68813 9.65147 6.84078 9.58824 6.99996 9.58824C7.15914 9.58824 7.31179 9.65147 7.42435 9.76403C7.5369 9.87658 7.60013 10.0292 7.60013 10.1884V10.5885C7.59806 10.7471 7.53416 10.8985 7.42205 11.0106C7.30995 11.1227 7.15849 11.1866 6.99996 11.1887Z"
                                fill="currentColor"
                            />
                        </svg>
                        <svg
                            v-else-if="slotProps.message.severity === 'info'"
                            width="14"
                            height="14"
                            viewBox="0 0 14 14"
                            fill="none"
                            xmlns="http://www.w3.org/2000/svg"
                            class="p-icon p-toast-message-icon"
                            aria-hidden="true"
                            data-pc-section="messageicon"
                        >
                            <path
                                fill-rule="evenodd"
                                clip-rule="evenodd"
                                d="M3.11101 12.8203C4.26215 13.5895 5.61553 14 7 14C8.85652 14 10.637 13.2625 11.9497 11.9497C13.2625 10.637 14 8.85652 14 7C14 5.61553 13.5895 4.26215 12.8203 3.11101C12.0511 1.95987 10.9579 1.06266 9.67879 0.532846C8.3997 0.00303296 6.99224 -0.13559 5.63437 0.134506C4.2765 0.404603 3.02922 1.07129 2.05026 2.05026C1.07129 3.02922 0.404603 4.2765 0.134506 5.63437C-0.13559 6.99224 0.00303296 8.3997 0.532846 9.67879C1.06266 10.9579 1.95987 12.0511 3.11101 12.8203ZM3.75918 2.14976C4.71846 1.50879 5.84628 1.16667 7 1.16667C8.5471 1.16667 10.0308 1.78125 11.1248 2.87521C12.2188 3.96918 12.8333 5.45291 12.8333 7C12.8333 8.15373 12.4912 9.28154 11.8502 10.2408C11.2093 11.2001 10.2982 11.9478 9.23232 12.3893C8.16642 12.8308 6.99353 12.9463 5.86198 12.7212C4.73042 12.4962 3.69102 11.9406 2.87521 11.1248C2.05941 10.309 1.50384 9.26958 1.27876 8.13803C1.05367 7.00647 1.16919 5.83358 1.61071 4.76768C2.05222 3.70178 2.79989 2.79074 3.75918 2.14976ZM7.00002 4.8611C6.84594 4.85908 6.69873 4.79698 6.58977 4.68801C6.48081 4.57905 6.4187 4.43185 6.41669 4.27776V3.88888C6.41669 3.73417 6.47815 3.58579 6.58754 3.4764C6.69694 3.367 6.84531 3.30554 7.00002 3.30554C7.15473 3.30554 7.3031 3.367 7.4125 3.4764C7.52189 3.58579 7.58335 3.73417 7.58335 3.88888V4.27776C7.58134 4.43185 7.51923 4.57905 7.41027 4.68801C7.30131 4.79698 7.1541 4.85908 7.00002 4.8611ZM7.00002 10.6945C6.84594 10.6925 6.69873 10.6304 6.58977 10.5214C6.48081 10.4124 6.4187 10.2652 6.41669 10.1111V6.22225C6.41669 6.06754 6.47815 5.91917 6.58754 5.80977C6.69694 5.70037 6.84531 5.63892 7.00002 5.63892C7.15473 5.63892 7.3031 5.70037 7.4125 5.80977C7.52189 5.91917 7.58335 6.06754 7.58335 6.22225V10.1111C7.58134 10.2652 7.51923 10.4124 7.41027 10.5214C7.30131 10.6304 7.1541 10.6925 7.00002 10.6945Z"
                                fill="currentColor"
                            />
                        </svg>
                        <svg
                            v-else-if="slotProps.message.severity === 'success'"
                            width="14"
                            height="14"
                            viewBox="0 0 14 14"
                            fill="none"
                            xmlns="http://www.w3.org/2000/svg"
                            class="p-icon p-toast-message-icon null"
                            aria-hidden="true"
                            data-pc-section="messageicon"
                        >
                            <path
                                d="M4.86199 11.5948C4.78717 11.5923 4.71366 11.5745 4.64596 11.5426C4.57826 11.5107 4.51779 11.4652 4.46827 11.4091L0.753985 7.69483C0.683167 7.64891 0.623706 7.58751 0.580092 7.51525C0.536478 7.44299 0.509851 7.36177 0.502221 7.27771C0.49459 7.19366 0.506156 7.10897 0.536046 7.03004C0.565935 6.95111 0.613367 6.88 0.674759 6.82208C0.736151 6.76416 0.8099 6.72095 0.890436 6.69571C0.970973 6.67046 1.05619 6.66385 1.13966 6.67635C1.22313 6.68886 1.30266 6.72017 1.37226 6.76792C1.44186 6.81567 1.4997 6.8786 1.54141 6.95197L4.86199 10.2503L12.6397 2.49483C12.7444 2.42694 12.8689 2.39617 12.9932 2.40745C13.1174 2.41873 13.2343 2.47141 13.3251 2.55705C13.4159 2.64268 13.4753 2.75632 13.4938 2.87973C13.5123 3.00315 13.4888 3.1292 13.4271 3.23768L5.2557 11.4091C5.20618 11.4652 5.14571 11.5107 5.07801 11.5426C5.01031 11.5745 4.9368 11.5923 4.86199 11.5948Z"
                                fill="currentColor"
                            />
                        </svg>
                        <svg
                            v-else
                            width="14"
                            height="14"
                            viewBox="0 0 14 14"
                            fill="none"
                            xmlns="http://www.w3.org/2000/svg"
                            class="p-icon p-toast-message-icon"
                            aria-hidden="true"
                            data-pc-section="messageicon"
                        >
                            <path
                                fill-rule="evenodd"
                                clip-rule="evenodd"
                                d="M7 14C5.61553 14 4.26215 13.5895 3.11101 12.8203C1.95987 12.0511 1.06266 10.9579 0.532846 9.67879C0.00303296 8.3997 -0.13559 6.99224 0.134506 5.63437C0.404603 4.2765 1.07129 3.02922 2.05026 2.05026C3.02922 1.07129 4.2765 0.404603 5.63437 0.134506C6.99224 -0.13559 8.3997 0.00303296 9.67879 0.532846C10.9579 1.06266 12.0511 1.95987 12.8203 3.11101C13.5895 4.26215 14 5.61553 14 7C14 8.85652 13.2625 10.637 11.9497 11.9497C10.637 13.2625 8.85652 14 7 14ZM7 1.16667C5.84628 1.16667 4.71846 1.50879 3.75918 2.14976C2.79989 2.79074 2.05222 3.70178 1.61071 4.76768C1.16919 5.83358 1.05367 7.00647 1.27876 8.13803C1.50384 9.26958 2.05941 10.309 2.87521 11.1248C3.69102 11.9406 4.73042 12.4962 5.86198 12.7212C6.99353 12.9463 8.16642 12.8308 9.23232 12.3893C10.2982 11.9478 11.2093 11.2001 11.8502 10.2408C12.4912 9.28154 12.8333 8.15373 12.8333 7C12.8333 5.45291 12.2188 3.96918 11.1248 2.87521C10.0308 1.78125 8.5471 1.16667 7 1.16667ZM4.66662 9.91668C4.58998 9.91704 4.51404 9.90209 4.44325 9.87271C4.37246 9.84333 4.30826 9.8001 4.2544 9.74557C4.14516 9.6362 4.0838 9.48793 4.0838 9.33335C4.0838 9.17876 4.14516 9.0305 4.2544 8.92113L6.17553 7L4.25443 5.07891C4.15139 4.96832 4.09529 4.82207 4.09796 4.67094C4.10063 4.51982 4.16185 4.37563 4.26872 4.26876C4.3756 4.16188 4.51979 4.10066 4.67091 4.09799C4.82204 4.09532 4.96829 4.15142 5.07887 4.25446L6.99997 6.17556L8.92106 4.25446C9.03164 4.15142 9.1779 4.09532 9.32903 4.09799C9.48015 4.10066 9.62434 4.16188 9.73121 4.26876C9.83809 4.37563 9.89931 4.51982 9.90198 4.67094C9.90464 4.82207 9.84855 4.96832 9.74551 5.07891L7.82441 7L9.74554 8.92113C9.85478 9.0305 9.91614 9.17876 9.91614 9.33335C9.91614 9.48793 9.85478 9.6362 9.74554 9.74557C9.69168 9.8001 9.62748 9.84333 9.55669 9.87271C9.4859 9.90209 9.40996 9.91704 9.33332 9.91668C9.25668 9.91704 9.18073 9.90209 9.10995 9.87271C9.03916 9.84333 8.97495 9.8001 8.9211 9.74557L6.99997 7.82444L5.07884 9.74557C5.02499 9.8001 4.96078 9.84333 4.88999 9.87271C4.81921 9.90209 4.74326 9.91704 4.66662 9.91668Z"
                                fill="currentColor"
                            />
                        </svg>
                        <div class="p-toast-message-text ml-2" data-pc-section="messagetext">
                            <span class="p-toast-summary" data-pc-section="summary">
                                {{ slotProps.message.summary }}
                            </span>
                        </div>
                        <Button
                            :title="t('messages.details')"
                            :aria-label="t('messages.details')"
                            icon="pi pi-ellipsis-h"
                            :severity="slotProps.message.severity"
                            variant="text"
                            outlined
                            rounded
                            @click="setDetails(slotProps.message.detail)"
                        />
                    </div>
                </div>
            </template>
        </Toast>
        <MessageDetailDialog />
        <ConfirmDialog />
        <router-view />
        <VueQueryDevtools />
    </div>

    <footer class="flex justify-between items-center p-4">
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
        <div class="flex items-right">
            <a
                href="#"
                class="mr-4"
                @click="showImprint"
            >
                {{ t('navigations.imprint') }}
            </a>
        </div>
    </footer>
</template>

<style scoped>
</style>
