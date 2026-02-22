<script setup lang="ts">
import Button from 'primevue/button'
import Select from 'primevue/select'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/features/auth/store/auth.store'
import { getFlagClass, SUPPORT_LOCALES } from '@/i18n'

const props = defineProps<{
    sidebarCollapsed: boolean
    currentLocale: string
}>()

const emit = defineEmits<{
    'toggleSidebar': []
    'update:currentLocale': [value: string]
}>()

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const mobileNavOpen = ref(false)

const localLocale = computed({
    get: () => props.currentLocale,
    set: (value) => {
        emit('update:currentLocale', value)
    },
})

function navigateTo(routeName: string) {
    mobileNavOpen.value = false
    router.push({ name: routeName, params: { locale: props.currentLocale } })
}

function isActive(routeName: string) {
    return route.name === routeName
}

function isAnalysisActive() {
    const analysisRoutes = [
        'analysis-hub',
        'split-time-analysis',
        'split-time-table-analysis',
        'mental-resilience-analysis',
        'cheat-detection-analysis',
        'hanging-detection-analysis',
    ]
    return analysisRoutes.includes(route.name as string)
}
</script>

<template>
    <header class="top-navbar sticky top-0 z-50 border-b border-adaptive shadow-sm">
        <div class="flex items-center justify-between h-16 px-4">
            <!-- Left: Sidebar Toggle + Logo + Navigation -->
            <div class="flex items-center gap-2">
                <Button
                    v-if="authStore.isAdmin"
                    icon="pi pi-bars"
                    :aria-label="t('labels.toggle_sidebar')"
                    severity="secondary"
                    text
                    rounded
                    @click="emit('toggleSidebar')"
                />
                <router-link :to="{ name: 'start-page', params: { locale: currentLocale } }" class="flex items-center">
                    <img
                        :alt="t('labels.logo')"
                        src="@/assets/Logo_Resulter_60px.webp"
                        width="48"
                        height="47"
                        type="image/webp"
                        fetchpriority="high"
                        class="transition-smooth hover:scale-105"
                    >
                </router-link>

                <Button
                    icon="pi pi-list"
                    class="md:hidden"
                    severity="secondary"
                    text
                    rounded
                    :aria-label="t('labels.open_navigation_menu')"
                    :aria-expanded="mobileNavOpen"
                    aria-controls="mobile-main-nav"
                    @click="mobileNavOpen = !mobileNavOpen"
                />

                <!-- Main Navigation direkt nach Logo -->
                <nav class="desktop-nav hidden md:flex items-center gap-2 ml-4">
                    <Button
                        :label="t('navigations.start')"
                        icon="pi pi-home"
                        :severity="isActive('start-page') ? 'primary' : 'secondary'"
                        :text="!isActive('start-page')"
                        rounded
                        @click="navigateTo('start-page')"
                    />
                    <Button
                        :label="t('navigations.cups')"
                        icon="pi pi-trophy"
                        :severity="isActive('cup-list') ? 'primary' : 'secondary'"
                        :text="!isActive('cup-list')"
                        rounded
                        @click="navigateTo('cup-list')"
                    />
                    <Button
                        :label="t('navigations.events')"
                        icon="pi pi-calendar"
                        :severity="isActive('event-list') ? 'primary' : 'secondary'"
                        :text="!isActive('event-list')"
                        rounded
                        @click="navigateTo('event-list')"
                    />
                    <Button
                        :label="t('navigations.analysis')"
                        icon="pi pi-chart-line"
                        :severity="isAnalysisActive() ? 'primary' : 'secondary'"
                        :text="!isAnalysisActive()"
                        rounded
                        @click="navigateTo('analysis-hub')"
                    />
                    <Button
                        :label="t('navigations.about')"
                        icon="pi pi-info-circle"
                        :severity="isActive('about-page') ? 'primary' : 'secondary'"
                        :text="!isActive('about-page')"
                        rounded
                        @click="navigateTo('about-page')"
                    />
                </nav>
            </div>

            <!-- Right: Login/Logout + Language -->
            <div class="flex items-center gap-3">
                <Button
                    v-if="!authStore.isAuthenticated"
                    :label="t('navigations.login')"
                    icon="pi pi-sign-in"
                    severity="secondary"
                    text
                    rounded
                    @click="authStore.login(route.fullPath, currentLocale)"
                />
                <Button
                    v-if="authStore.isAuthenticated"
                    :label="t('navigations.logout')"
                    icon="pi pi-sign-out"
                    severity="secondary"
                    text
                    rounded
                    @click="authStore.logout()"
                />
                <Select
                    v-model="localLocale"
                    :options="SUPPORT_LOCALES"
                    class="w-24"
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

        <nav v-if="mobileNavOpen" id="mobile-main-nav" class="md:hidden border-t border-adaptive bg-adaptive-secondary px-4 py-3">
            <div class="flex flex-col gap-2">
                <Button
                    :label="t('navigations.start')"
                    icon="pi pi-home"
                    :severity="isActive('start-page') ? 'primary' : 'secondary'"
                    :text="!isActive('start-page')"
                    class="w-full justify-start"
                    @click="navigateTo('start-page')"
                />
                <Button
                    :label="t('navigations.cups')"
                    icon="pi pi-trophy"
                    :severity="isActive('cup-list') ? 'primary' : 'secondary'"
                    :text="!isActive('cup-list')"
                    class="w-full justify-start"
                    @click="navigateTo('cup-list')"
                />
                <Button
                    :label="t('navigations.events')"
                    icon="pi pi-calendar"
                    :severity="isActive('event-list') ? 'primary' : 'secondary'"
                    :text="!isActive('event-list')"
                    class="w-full justify-start"
                    @click="navigateTo('event-list')"
                />
                <Button
                    :label="t('navigations.analysis')"
                    icon="pi pi-chart-line"
                    :severity="isAnalysisActive() ? 'primary' : 'secondary'"
                    :text="!isAnalysisActive()"
                    class="w-full justify-start"
                    @click="navigateTo('analysis-hub')"
                />
                <Button
                    :label="t('navigations.about')"
                    icon="pi pi-info-circle"
                    :severity="isActive('about-page') ? 'primary' : 'secondary'"
                    :text="!isActive('about-page')"
                    class="w-full justify-start"
                    @click="navigateTo('about-page')"
                />
            </div>
        </nav>
    </header>
</template>

<style scoped>
.top-navbar {
    background-color: rgb(var(--bg-primary) / 0.88);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
}
</style>
