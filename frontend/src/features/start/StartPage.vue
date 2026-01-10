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
        <div class="flex items-center gap-6">
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
                <p class="text-sm text-adaptive-secondary">
                    {{ t('dashboard.welcome_message') }}
                </p>
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
            <div v-else-if="isError" class="p-4 bg-red-50 border border-red-200 rounded">
                <p class="text-red-700">
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
                <Card class="dashboard-card dashboard-card-primary clickable" :onclick="() => navigateTo('event-list')">
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
                            <div class="stat-icon stat-icon-primary">
                                <i class="pi pi-calendar text-2xl" />
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Cup Count Card -->
                <Card class="dashboard-card dashboard-card-warning clickable" :onclick="() => navigateTo('cup-list')">
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
                            <div class="stat-icon stat-icon-warning">
                                <i class="pi pi-trophy text-2xl" />
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Person Count Card -->
                <Card
                    class="dashboard-card"
                    :class="[{ 'dashboard-card-primary': authStore.isAuthenticated, 'clickable': authStore.isAuthenticated }]"
                    :onclick="authStore.isAuthenticated ? () => navigateTo('person-list') : undefined"
                >
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
                            <div class="stat-icon" :class="authStore.isAuthenticated ? 'stat-icon-primary' : ''">
                                <i class="pi pi-users text-2xl" />
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Organisation Count Card -->
                <Card
                    class="dashboard-card"
                    :class="[{ 'dashboard-card-primary': authStore.isAuthenticated, 'clickable': authStore.isAuthenticated }]"
                    :onclick="authStore.isAuthenticated ? () => navigateTo('organisation-list') : undefined"
                >
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
                            <div class="stat-icon" :class="authStore.isAuthenticated ? 'stat-icon-primary' : ''">
                                <i class="pi pi-building text-2xl" />
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Split Time Count Card -->
                <Card class="dashboard-card dashboard-card-info">
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
                            <div class="stat-icon stat-icon-info">
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
        <Card v-if="errorStore.errorCount === 0" class="text-center py-8">
            <template #content>
                <i class="pi pi-check-circle text-6xl text-green-500 mb-4" />
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
    padding: 1.5rem 0;
    border-bottom: 1px solid rgb(var(--border-color));
}

.hero-logo {
    filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
    transition: transform 0.3s ease;
    flex-shrink: 0;
}

.hero-logo:hover {
    transform: scale(1.05);
}

/* Dashboard Cards */
.dashboard-card {
    transition: all 0.3s ease;
    border-radius: 12px;
    border: 1px solid transparent;
}

.dashboard-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.dashboard-card.clickable {
    cursor: pointer;
}

/* Card Variants */
.dashboard-card-primary {
    border-color: rgba(34, 197, 94, 0.2);
    background: linear-gradient(135deg, rgba(34, 197, 94, 0.05) 0%, transparent 100%);
}

.dashboard-card-primary:hover {
    border-color: rgba(34, 197, 94, 0.4);
    background: linear-gradient(135deg, rgba(34, 197, 94, 0.1) 0%, transparent 100%);
}

.dashboard-card-warning {
    border-color: rgba(251, 146, 60, 0.2);
    background: linear-gradient(135deg, rgba(251, 146, 60, 0.05) 0%, transparent 100%);
}

.dashboard-card-warning:hover {
    border-color: rgba(251, 146, 60, 0.4);
    background: linear-gradient(135deg, rgba(251, 146, 60, 0.1) 0%, transparent 100%);
}

.dashboard-card-info {
    border-color: rgba(59, 130, 246, 0.2);
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.05) 0%, transparent 100%);
}

.dashboard-card-info:hover {
    border-color: rgba(59, 130, 246, 0.4);
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, transparent 100%);
}

/* Stat Icons */
.stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgb(var(--bg-secondary));
    transition: all 0.3s ease;
}

.stat-icon-primary {
    background: rgba(34, 197, 94, 0.1);
    color: rgb(34, 197, 94);
}

.dashboard-card-primary:hover .stat-icon-primary {
    background: rgba(34, 197, 94, 0.2);
    transform: scale(1.1);
}

.stat-icon-warning {
    background: rgba(251, 146, 60, 0.1);
    color: rgb(251, 146, 60);
}

.dashboard-card-warning:hover .stat-icon-warning {
    background: rgba(251, 146, 60, 0.2);
    transform: scale(1.1);
}

.stat-icon-info {
    background: rgba(59, 130, 246, 0.1);
    color: rgb(59, 130, 246);
}

.dashboard-card-info:hover .stat-icon-info {
    background: rgba(59, 130, 246, 0.2);
    transform: scale(1.1);
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
        border-bottom-color: rgb(var(--border-color));
    }

    .stat-icon-primary {
        background: rgba(34, 197, 94, 0.15);
    }

    .stat-icon-warning {
        background: rgba(251, 146, 60, 0.15);
    }

    .stat-icon-info {
        background: rgba(59, 130, 246, 0.15);
    }
}
</style>
