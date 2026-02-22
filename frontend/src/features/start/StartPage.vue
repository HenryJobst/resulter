<script setup lang="ts">
import Button from 'primevue/button'
import Card from 'primevue/card'
import Skeleton from 'primevue/skeleton'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/auth/store/auth.store'
import { useErrorStore } from '@/features/common/stores/useErrorStore'
import { useMessageDetailStore } from '@/features/common/stores/useMessageDetailStore'
import { useNavigation } from '@/features/generic/composables/useNavigation.ts'
import { formatDateAndTime } from '@/features/generic/services/GenericFunctions'
import { useDashboardStatistics } from '@/features/start/services/dashboard.service'
import { BackendException, getDetail } from '@/utils/HandleError'

const { t, locale } = useI18n() // same as `useI18n({ useScope: 'global' })`

const authStore = useAuthStore()
const errorStore = useErrorStore()
const messageDetailStore = useMessageDetailStore()
const { navigateTo } = useNavigation()

// Fetch dashboard statistics
const { data: statistics, isLoading, isError, refetch } = useDashboardStatistics()

async function showErrorDetail(id: number) {
    const error = errorStore.getError(id)
    if (error) {
        const details = await getDetail(error.originalError, t)
        if (details) {
            messageDetailStore.show(details)
        }
        else {
            messageDetailStore.hide()
        }
    }
}
</script>

<template>
    <!-- Hero Section -->
    <div class="hero-section mb-6">
        <div class="hero-content flex flex-col md:flex-row md:items-center md:justify-between gap-6">
            <div class="hero-main flex items-center gap-6">
                <img
                    alt="Resulter Logo"
                    class="hero-logo"
                    src="@/assets/Logo_Resulter_400px.webp"
                    width="120"
                    height="119"
                >
                <div>
                    <h1 class="text-3xl font-bold text-adaptive mb-2">
                        {{ t('pages.start') }}
                    </h1>
                    <p class="text-sm text-adaptive-secondary mb-3">
                        {{ t('dashboard.welcome_message') }}
                    </p>
                    <div class="hero-badges flex flex-wrap gap-2" aria-label="Dashboard highlights">
                        <div
                            class="hero-badge"
                            role="button"
                            tabindex="0"
                            @click="navigateTo('event-list')"
                            @keydown.enter.prevent="navigateTo('event-list')"
                            @keydown.space.prevent="navigateTo('event-list')"
                        >
                            <span class="hero-badge-label">{{ t('dashboard.stats.events') }}</span>
                            <span class="hero-badge-value">{{ statistics?.eventCount ?? '—' }}</span>
                        </div>
                        <div
                            class="hero-badge"
                            role="button"
                            tabindex="0"
                            @click="navigateTo('cup-list')"
                            @keydown.enter.prevent="navigateTo('cup-list')"
                            @keydown.space.prevent="navigateTo('cup-list')"
                        >
                            <span class="hero-badge-label">{{ t('dashboard.stats.cups') }}</span>
                            <span class="hero-badge-value">{{ statistics?.cupCount ?? '—' }}</span>
                        </div>
                        <div class="hero-badge">
                            <span class="hero-badge-label">{{ t('dashboard.stats.races') }}</span>
                            <span class="hero-badge-value">{{ statistics?.raceCount ?? '—' }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="hero-ambient hidden md:flex items-center gap-3" aria-hidden="true">
                <span class="hero-dot hero-dot--lg" />
                <span class="hero-dot hero-dot--md" />
                <span class="hero-dot hero-dot--sm" />
            </div>
        </div>
    </div>

    <!-- Dashboard Statistics Section -->
    <div class="statistics-section">
        <div class="flex-1">
            <!-- Loading State -->
            <div v-if="isLoading" class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
                <Card v-for="i in 7" :key="i" class="dashboard-card">
                    <template #content>
                        <Skeleton width="100%" height="2rem" class="mb-2" />
                        <Skeleton width="60%" height="3rem" />
                    </template>
                </Card>
            </div>

            <!-- Error State -->
            <div v-else-if="isError" class="p-4 bg-red-50 dark:bg-red-950/35 border border-red-200 dark:border-red-900 rounded">
                <p class="text-red-700 dark:text-red-300">
                    {{ t('dashboard.error_loading') }}
                </p>
                <Button
                    :label="t('labels.reload')"
                    icon="pi pi-refresh"
                    class="mt-2"
                    size="small"
                    @click="refetch"
                />
            </div>

            <!-- Statistics Cards -->
            <div v-else-if="statistics" class="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
                <!-- Event Count Card -->
                <div
                    class="dashboard-card-click-target clickable"
                    role="button"
                    tabindex="0"
                    @click="navigateTo('event-list')"
                    @keydown.enter.prevent="navigateTo('event-list')"
                    @keydown.space.prevent="navigateTo('event-list')"
                >
                    <Card class="dashboard-card">
                        <template #content>
                            <div class="flex items-center justify-between">
                                <div class="flex flex-col gap-2 mr-3">
                                    <div class="text-sm font-medium text-adaptive-secondary">
                                        {{ t('dashboard.stats.events') }}
                                    </div>
                                    <div class="text-4xl font-bold text-adaptive">
                                        {{ statistics.eventCount }}
                                    </div>
                                </div>
                                <div class="stat-icon">
                                    <i class="pi pi-calendar text-2xl" />
                                </div>
                            </div>
                        </template>
                    </Card>
                </div>

                <!-- Cup Count Card -->
                <div
                    class="dashboard-card-click-target clickable"
                    role="button"
                    tabindex="0"
                    @click="navigateTo('cup-list')"
                    @keydown.enter.prevent="navigateTo('cup-list')"
                    @keydown.space.prevent="navigateTo('cup-list')"
                >
                    <Card class="dashboard-card">
                        <template #content>
                            <div class="flex items-center justify-between">
                                <div class="flex flex-col gap-2 mr-3">
                                    <div class="text-sm font-medium text-adaptive-secondary">
                                        {{ t('dashboard.stats.cups') }}
                                    </div>
                                    <div class="text-4xl font-bold text-adaptive">
                                        {{ statistics.cupCount }}
                                    </div>
                                </div>
                                <div class="stat-icon">
                                    <i class="pi pi-trophy text-2xl" />
                                </div>
                            </div>
                        </template>
                    </Card>
                </div>

                <!-- Person Count Card -->
                <div
                    class="dashboard-card-click-target"
                    :class="[{ clickable: authStore.isAuthenticated }]"
                    :role="authStore.isAuthenticated ? 'button' : undefined"
                    :tabindex="authStore.isAuthenticated ? 0 : undefined"
                    @click="authStore.isAuthenticated ? navigateTo('person-list') : undefined"
                    @keydown.enter.prevent="authStore.isAuthenticated ? navigateTo('person-list') : undefined"
                    @keydown.space.prevent="authStore.isAuthenticated ? navigateTo('person-list') : undefined"
                >
                    <Card class="dashboard-card">
                        <template #content>
                            <div class="flex items-center justify-between">
                                <div class="flex flex-col gap-2 mr-3">
                                    <div class="text-sm font-medium text-adaptive-secondary">
                                        {{ t('dashboard.stats.persons') }}
                                    </div>
                                    <div class="text-4xl font-bold text-adaptive">
                                        {{ statistics.personCount }}
                                    </div>
                                </div>
                                <div class="stat-icon">
                                    <i class="pi pi-users text-2xl" />
                                </div>
                            </div>
                        </template>
                    </Card>
                </div>

                <!-- Organisation Count Card -->
                <div
                    class="dashboard-card-click-target"
                    :class="[{ clickable: authStore.isAuthenticated }]"
                    :role="authStore.isAuthenticated ? 'button' : undefined"
                    :tabindex="authStore.isAuthenticated ? 0 : undefined"
                    @click="authStore.isAuthenticated ? navigateTo('organisation-list') : undefined"
                    @keydown.enter.prevent="authStore.isAuthenticated ? navigateTo('organisation-list') : undefined"
                    @keydown.space.prevent="authStore.isAuthenticated ? navigateTo('organisation-list') : undefined"
                >
                    <Card class="dashboard-card">
                        <template #content>
                            <div class="flex items-center justify-between">
                                <div class="flex flex-col gap-2 mr-3">
                                    <div class="text-sm font-medium text-adaptive-secondary">
                                        {{ t('dashboard.stats.organisations') }}
                                    </div>
                                    <div class="text-4xl font-bold text-adaptive">
                                        {{ statistics.organisationCount }}
                                    </div>
                                </div>
                                <div class="stat-icon">
                                    <i class="pi pi-building text-2xl" />
                                </div>
                            </div>
                        </template>
                    </Card>
                </div>

                <!-- Split Time Count Card -->
                <Card class="dashboard-card">
                    <template #content>
                        <div class="flex items-center justify-between">
                            <div class="flex flex-col gap-2 mr-3">
                                <div class="text-sm font-medium text-adaptive-secondary">
                                    {{ t('dashboard.stats.split_times') }}
                                </div>
                                <div class="text-4xl font-bold text-adaptive">
                                    {{ statistics.splitTimeCount.toLocaleString() }}
                                </div>
                            </div>
                            <div class="stat-icon">
                                <i class="pi pi-clock text-2xl" />
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Race Count Card -->
                <Card class="dashboard-card">
                    <template #content>
                        <div class="flex items-center justify-between">
                            <div class="flex flex-col gap-2 mr-3">
                                <div class="text-sm font-medium text-adaptive-secondary">
                                    {{ t('dashboard.stats.races') }}
                                </div>
                                <div class="text-4xl font-bold text-adaptive">
                                    {{ statistics.raceCount }}
                                </div>
                            </div>
                            <div class="stat-icon">
                                <i class="pi pi-flag text-2xl" />
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Certificate Count Card -->
                <Card class="dashboard-card">
                    <template #content>
                        <div class="flex items-center justify-between">
                            <div class="flex flex-col gap-2 mr-3">
                                <div class="text-sm font-medium text-adaptive-secondary">
                                    {{ t('dashboard.stats.certificates') }}
                                </div>
                                <div class="text-4xl font-bold text-adaptive">
                                    {{ statistics.certificateCount }}
                                </div>
                            </div>
                            <div class="stat-icon">
                                <i class="pi pi-file text-2xl" />
                            </div>
                        </div>
                    </template>
                </Card>
            </div>
        </div>
    </div>

    <!-- Messages Section -->
    <div class="messages-section mt-12">
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-2xl font-semibold text-adaptive">
                {{ t('labels.message', 2) }}
            </h2>
            <Button
                v-if="errorStore.errorCount > 0"
                icon="pi pi-trash"
                :label="t('labels.deleteAll')"
                severity="danger"
                outlined
                size="small"
                @click="errorStore.clearErrors"
            />
        </div>

        <!-- No Messages State -->
        <Card v-if="errorStore.errorCount === 0" class="no-messages-card text-center py-8">
            <template #content>
                <i class="pi pi-check-circle text-6xl no-messages-icon mb-4" />
                <p class="text-lg text-adaptive-secondary">
                    {{ t("messages.noMessages") }}
                </p>
            </template>
        </Card>

        <!-- Error Messages -->
        <div v-else class="flex flex-col gap-4">
            <Card v-for="error in errorStore.errors" :key="error.id" class="error-card">
                <template #content>
                    <div class="flex items-start gap-4">
                        <div class="error-icon">
                            <i class="pi pi-exclamation-triangle text-xl" />
                        </div>
                        <div class="flex-1">
                            <div class="flex items-center gap-2 mb-2">
                                <span class="text-sm font-semibold text-red-600">{{ t("labels.error") }}</span>
                                <span class="text-xs text-adaptive-secondary">
                                    {{ formatDateAndTime(error.timestamp ?? new Date(Date.now()), locale) }}
                                </span>
                            </div>
                            <p class="text-sm text-adaptive mb-3">
                                {{
                                    error.originalError instanceof BackendException
                                        ? (error.originalError as BackendException).message
                                        : ((error.originalError as Error).name ?? (error.originalError as Error).message)
                                }}
                            </p>
                            <div class="flex gap-2">
                                <Button
                                    icon="pi pi-eye"
                                    :label="t('labels.detail')"
                                    size="small"
                                    outlined
                                    @click="showErrorDetail(error.id)"
                                />
                                <Button
                                    icon="pi pi-trash"
                                    :label="t('labels.delete')"
                                    severity="danger"
                                    size="small"
                                    outlined
                                    @click="errorStore.removeError(error.id)"
                                />
                            </div>
                        </div>
                    </div>
                </template>
            </Card>
        </div>
    </div>
</template>

<style scoped>
/* Hero Section */
.hero-section {
    position: relative;
    overflow: hidden;
    padding: 1.5rem;
    border: 1px solid rgba(251, 146, 60, 0.16);
    border-radius: 18px;
    background:
        radial-gradient(circle at 90% 10%, rgba(251, 146, 60, 0.2) 0%, rgba(251, 146, 60, 0) 42%),
        linear-gradient(135deg, rgba(251, 146, 60, 0.07) 0%, rgba(251, 146, 60, 0.015) 45%, rgba(255, 255, 255, 0) 100%);
    border-bottom: 1px solid rgb(var(--border-color));
}

.hero-content {
    position: relative;
    z-index: 1;
}

.hero-section::before {
    content: '';
    position: absolute;
    inset: 0;
    pointer-events: none;
    opacity: 0.32;
    background-image:
        linear-gradient(rgba(251, 146, 60, 0.22) 1px, transparent 1px),
        linear-gradient(90deg, rgba(251, 146, 60, 0.22) 1px, transparent 1px);
    background-size: 24px 24px;
    mask-image: linear-gradient(to bottom right, black 20%, transparent 85%);
}

.hero-logo {
    filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
    transition: transform 0.3s ease;
    flex-shrink: 0;
}

.hero-logo:hover {
    transform: scale(1.05);
}

.hero-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.35rem 0.7rem;
    border-radius: 999px;
    border: 1px solid rgba(251, 146, 60, 0.28);
    background: rgba(251, 146, 60, 0.12);
}

.hero-badge[role='button'] {
    cursor: pointer;
}

.hero-badge[role='button']:focus-visible {
    outline: 2px solid rgba(251, 146, 60, 0.7);
    outline-offset: 2px;
}

.hero-badge-label {
    font-size: 0.75rem;
    color: rgb(var(--text-secondary));
}

.hero-badge-value {
    font-size: 0.85rem;
    font-weight: 700;
    color: rgb(var(--text-primary));
}

.hero-dot {
    display: inline-block;
    border-radius: 999px;
    border: 1px solid rgba(251, 146, 60, 0.45);
    background: rgba(251, 146, 60, 0.2);
}

.hero-dot--lg {
    width: 1.25rem;
    height: 1.25rem;
}

.hero-dot--md {
    width: 0.85rem;
    height: 0.85rem;
}

.hero-dot--sm {
    width: 0.6rem;
    height: 0.6rem;
}

/* Dashboard Cards */
.dashboard-card {
    transition: all 0.3s ease;
    border-radius: 12px;
    border: 1px solid rgba(251, 146, 60, 0.2);
    background: linear-gradient(135deg, rgba(251, 146, 60, 0.05) 0%, transparent 100%);
}

.dashboard-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
    border-color: rgba(251, 146, 60, 0.4);
    background: linear-gradient(135deg, rgba(251, 146, 60, 0.1) 0%, transparent 100%);
}

.dashboard-card.clickable {
    cursor: pointer;
}

.dashboard-card-click-target {
    border-radius: 12px;
}

.dashboard-card-click-target.clickable {
    cursor: pointer;
}

.dashboard-card-click-target[role='button']:focus-visible {
    outline: none;
}

.dashboard-card-click-target[role='button']:focus-visible .dashboard-card {
    outline: 2px solid rgba(251, 146, 60, 0.8);
    outline-offset: 2px;
}

/* Stat Icons */
.stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(251, 146, 60, 0.1);
    color: rgb(251, 146, 60);
    transition: all 0.3s ease;
}

.dashboard-card:hover .stat-icon {
    background: rgba(251, 146, 60, 0.2);
    transform: scale(1.1);
}

/* No Messages Card */
.no-messages-card {
    border: 1px solid rgba(251, 146, 60, 0.2);
    background: linear-gradient(135deg, rgba(251, 146, 60, 0.02) 0%, transparent 100%);
}

.no-messages-icon {
    color: rgb(251, 146, 60);
}

/* Error Cards */
.error-card {
    border-left: 4px solid rgb(239, 68, 68);
    transition: all 0.2s ease;
}

.error-card:hover {
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.15);
}

.error-icon {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: rgba(239, 68, 68, 0.1);
    color: rgb(239, 68, 68);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

/* Dark Mode Adjustments */
@media (prefers-color-scheme: dark) {
    .hero-section {
        border-color: rgba(251, 146, 60, 0.2);
        border-bottom-color: rgb(var(--border-color));
        background:
            radial-gradient(circle at 90% 10%, rgba(251, 146, 60, 0.24) 0%, rgba(251, 146, 60, 0) 46%),
            linear-gradient(135deg, rgba(251, 146, 60, 0.08) 0%, rgba(251, 146, 60, 0.02) 45%, rgba(0, 0, 0, 0) 100%);
    }

    .hero-section::before {
        opacity: 0.2;
    }

    .hero-badge {
        border-color: rgba(251, 146, 60, 0.4);
        background: rgba(251, 146, 60, 0.16);
    }

    .dashboard-card {
        border-color: rgba(251, 146, 60, 0.2);
        background: linear-gradient(135deg, rgba(251, 146, 60, 0.05) 0%, transparent 100%);
    }

    .dashboard-card:hover {
        border-color: rgba(251, 146, 60, 0.4);
        background: linear-gradient(135deg, rgba(251, 146, 60, 0.1) 0%, transparent 100%);
    }

    .stat-icon {
        background: rgba(251, 146, 60, 0.15);
        color: rgb(251, 146, 60);
    }

    .dashboard-card:hover .stat-icon {
        background: rgba(251, 146, 60, 0.25);
    }

    .no-messages-card {
        border-color: rgba(251, 146, 60, 0.2);
        background: linear-gradient(135deg, rgba(251, 146, 60, 0.02) 0%, transparent 100%);
    }

    .no-messages-icon {
        color: rgb(251, 146, 60);
    }
}
</style>
