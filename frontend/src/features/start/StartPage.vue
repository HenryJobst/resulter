<script setup lang="ts">
import Button from 'primevue/button'
import Card from 'primevue/card'
import Panel from 'primevue/panel'
import Skeleton from 'primevue/skeleton'
import { useI18n } from 'vue-i18n'
import { useErrorStore } from '@/features/common/stores/useErrorStore'
import { useMessageDetailStore } from '@/features/common/stores/useMessageDetailStore'
import { useNavigation } from '@/features/generic/composables/useNavigation.ts'
import { formatDateAndTime } from '@/features/generic/services/GenericFunctions'
import { useDashboardStatistics } from '@/features/start/services/dashboard.service'
import { BackendException, getDetail } from '@/utils/HandleError'

const { t, locale } = useI18n() // same as `useI18n({ useScope: 'global' })`

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
    <h2>{{ t('pages.start') }}</h2>

    <!-- Logo and Statistics Section -->
    <div class="flex flex-col lg:flex-row gap-8 my-6 items-start">
        <!-- Logo -->
        <img alt="Logo" class="shrink-0" src="@/assets/Logo_Resulter_400px.webp" width="400" height="396">

        <!-- Dashboard Statistics Section -->
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
            <div v-else-if="statistics" class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
                <!-- Event Count Card -->
                <Card class="dashboard-card" :onclick="() => navigateTo('event-list')">
                    <template #content>
                        <div class="flex flex-col">
                            <div class="flex items-center gap-2 mb-2">
                                <i class="pi pi-calendar text-2xl " />
                                <div class="text-sm ">
                                    {{ t('dashboard.stats.events') }}
                                </div>
                            </div>
                            <div class="text-3xl font-bold ">
                                {{ statistics.eventCount }}
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Cup Count Card -->
                <Card class="dashboard-card" :onclick="() => navigateTo('cup-list')">
                    <template #content>
                        <div class="flex flex-col">
                            <div class="flex items-center gap-2 mb-2">
                                <i class="pi pi-trophy text-2xl " />
                                <div class="text-sm ">
                                    {{ t('dashboard.stats.cups') }}
                                </div>
                            </div>
                            <div class="text-3xl font-bold ">
                                {{ statistics.cupCount }}
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Person Count Card -->
                <Card class="dashboard-card">
                    <template #content>
                        <div class="flex flex-col">
                            <div class="flex items-center gap-2 mb-2">
                                <i class="pi pi-users text-2xl " />
                                <div class="text-sm ">
                                    {{ t('dashboard.stats.persons') }}
                                </div>
                            </div>
                            <div class="text-3xl font-bold ">
                                {{ statistics.personCount }}
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Organisation Count Card -->
                <Card class="dashboard-card">
                    <template #content>
                        <div class="flex flex-col">
                            <div class="flex items-center gap-2 mb-2">
                                <i class="pi pi-building text-2xl " />
                                <div class="text-sm ">
                                    {{ t('dashboard.stats.organisations') }}
                                </div>
                            </div>
                            <div class="text-3xl font-bold ">
                                {{ statistics.organisationCount }}
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Split Time Count Card -->
                <Card class="dashboard-card">
                    <template #content>
                        <div class="flex flex-col">
                            <div class="flex items-center gap-2 mb-2">
                                <i class="pi pi-clock text-2xl " />
                                <div class="text-sm ">
                                    {{ t('dashboard.stats.split_times') }}
                                </div>
                            </div>
                            <div class="text-3xl font-bold ">
                                {{ statistics.splitTimeCount.toLocaleString() }}
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Race Count Card -->
                <Card class="dashboard-card">
                    <template #content>
                        <div class="flex flex-col">
                            <div class="flex items-center gap-2 mb-2">
                                <i class="pi pi-flag text-2xl " />
                                <div class="text-sm ">
                                    {{ t('dashboard.stats.races') }}
                                </div>
                            </div>
                            <div class="text-3xl font-bold ">
                                {{ statistics.raceCount }}
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- Certificate Count Card -->
                <Card class="dashboard-card">
                    <template #content>
                        <div class="flex flex-col">
                            <div class="flex items-center gap-2 mb-2">
                                <i class="pi pi-file text-2xl " />
                                <div class="text-sm ">
                                    {{ t('dashboard.stats.certificates') }}
                                </div>
                            </div>
                            <div class="text-3xl font-bold ">
                                {{ statistics.certificateCount }}
                            </div>
                        </div>
                    </template>
                </Card>
            </div>
        </div>
    </div>

    <Panel :header="t('labels.message', 2)" class="my-4" toggleable>
        <template #icons>
            <Button
                v-if="errorStore.errorCount > 0"
                id="deleteAll"
                icon="pi pi-trash"
                severity="secondary"
                outlined
                raised
                rounded
                text
                :title="t('labels.deleteAll')"
                :aria-label="t('labels.deleteAll')"
                @click="errorStore.clearErrors"
            />
        </template>
        <div class="flex flex-row align-items-center" />
        <div v-if="errorStore.errorCount === 0" class="ml-3">
            {{ t("messages.noMessages") }}
        </div>
        <table v-if="errorStore.errorCount > 0" class="ml-3">
            <thead>
                <tr>
                    <th style="width: 6rem; text-align: left">
                        {{ t("labels.type") }}
                    </th>
                    <th style="width: 15rem; text-align: left">
                        {{ t("labels.timestamp") }}
                    </th>
                    <th style="width: 30rem; text-align: left">
                        {{ t("labels.message") }}
                    </th>
                    <th style="width: 4rem; text-align: left">
                        {{ t("labels.action") }}
                    </th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="error in errorStore.errors" :key="error.id">
                    <td>{{ t("labels.error") }}</td>
                    <td>{{ formatDateAndTime(error.timestamp ?? new Date(Date.now()), locale) }}</td>
                    <td>
                        {{
                            error.originalError instanceof BackendException
                                ? (error.originalError as BackendException).message
                                : ((error.originalError as Error).name ?? (error.originalError as Error).message)
                        }}
                    </td>
                    <td>
                        <div class="flex flex-row">
                            <Button
                                icon="pi pi-eye"
                                :title="t('labels.detail')"
                                :aria-label="t('labels.detail')"
                                outlined
                                raised
                                rounded
                                @click="showErrorDetail(error.id)"
                            />
                            <Button
                                class="ml-2"
                                icon="pi pi-trash"
                                :title="t('labels.delete')"
                                :aria-label="t('labels.delete')"
                                outlined
                                raised
                                rounded
                                @click="errorStore.removeError(error.id)"
                            />
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </Panel>
</template>

<style scoped>
.dashboard-card {
    transition: all 0.3s ease;
}

.dashboard-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}
</style>
