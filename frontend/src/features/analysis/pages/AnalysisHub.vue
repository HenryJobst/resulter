<script setup lang="ts">
import type { AnalysisScope, AnalysisType, AnalysisTypeInfo } from '../model/analysis_config'
import type { ResultList } from '@/features/event/model/result_list'
import type { SportEvent } from '@/features/event/model/sportEvent'
import { useQuery } from '@tanstack/vue-query'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Select from 'primevue/select'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/features/auth/store/auth.store'
import { eventService, EventService } from '@/features/event/services/event.service'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()

// State
const selectedAnalysisType = ref<AnalysisType | null>(null)
const selectedScope = ref<AnalysisScope>('event')
const selectedEvent = ref<SportEvent | null>(null)
const selectedResultList = ref<ResultList | null>(null)

// Available analysis types
const analysisTypes = computed<AnalysisTypeInfo[]>(() => {
    const types: AnalysisTypeInfo[] = [
        {
            key: 'split-time-table',
            titleKey: 'labels.split_time_table',
            descriptionKey: 'messages.split_time_table_description',
            icon: 'pi pi-table',
            enabled: true,
        },
        {
            key: 'split-time-ranking',
            titleKey: 'labels.split_time_ranking',
            descriptionKey: 'messages.split_time_description',
            icon: 'pi pi-chart-bar',
            enabled: true,
        },
        {
            key: 'mental-resilience',
            titleKey: 'labels.mental_resilience_analysis',
            descriptionKey: 'messages.mri_description',
            icon: 'pi pi-chart-line',
            enabled: true,
        },
    ]

    // Anomaly detection and hanging detection are only available for ADMIN users
    if (authStore.isAdmin) {
        types.push({
            key: 'cheat-detection',
            titleKey: 'labels.anomaly_detection_analysis',
            descriptionKey: 'messages.anomaly_detection_description',
            icon: 'pi pi-exclamation-triangle',
            enabled: true,
        })
        types.push({
            key: 'hanging-detection',
            titleKey: 'labels.hanging_detection_analysis',
            descriptionKey: 'messages.hanging_detection_description',
            icon: 'pi pi-users',
            enabled: true,
        })
    }

    return types
})

// Fetch all events for selection
const eventsQuery = useQuery({
    queryKey: ['events'],
    queryFn: () => eventService.getAllUnpaged(t),
})

// Events list for dropdown (sorted by date, newest first)
const events = computed(() => {
    if (!eventsQuery.data.value)
        return []
    const eventList = (eventsQuery.data.value || []).filter(e => e.hasSplitTimes)
    return [...eventList].sort((a, b) => {
        const dateA = new Date(a.startTime).getTime()
        const dateB = new Date(b.startTime).getTime()
        return dateB - dateA // Descending order (newest first)
    })
})

// Fetch result lists for selected event
const resultListsQuery = useQuery({
    queryKey: ['eventResults', selectedEvent.value?.id],
    queryFn: () => {
        if (!selectedEvent.value?.id)
            return Promise.resolve(null)
        return EventService.getResultsById(selectedEvent.value.id.toString(), t)
    },
    enabled: computed(() => selectedEvent.value !== null),
})

// Result lists for dropdown with labels
const resultLists = computed(() => {
    if (!resultListsQuery.data.value)
        return []
    const lists = resultListsQuery.data.value.resultLists || []

    // Sort by creation time
    const sortedLists = [...lists].sort((a, b) => {
        const dateA = new Date(a.createTime).getTime()
        const dateB = new Date(b.createTime).getTime()
        return dateA - dateB // Ascending order (oldest first for numbering)
    })

    // Add labels
    return sortedLists.map((list, index) => ({
        ...list,
        label: sortedLists.length > 1
            ? `${t('labels.result_list')} #${index + 1}`
            : t('labels.result_list'),
    }))
})

// Auto-select if only one result list
watch(resultLists, (newLists) => {
    if (newLists.length === 1 && !selectedResultList.value) {
        selectedResultList.value = newLists[0]
    }
})

// Check if configuration is complete
const canStartAnalysis = computed(() => {
    if (!selectedAnalysisType.value)
        return false
    if (selectedScope.value === 'event') {
        return selectedEvent.value !== null && selectedResultList.value !== null
    }
    // Cup and Year scopes not yet implemented
    return false
})

// Select analysis type
function selectAnalysisType(type: AnalysisType) {
    selectedAnalysisType.value = type
    // Reset selections when changing type
    selectedEvent.value = null
    selectedResultList.value = null
}

// Start analysis
function startAnalysis() {
    if (!canStartAnalysis.value || !selectedAnalysisType.value)
        return

    if (!selectedResultList.value || !selectedEvent.value)
        return

    if (selectedAnalysisType.value === 'mental-resilience') {
        router.push({
            name: 'mental-resilience-analysis',
            query: {
                scope: selectedScope.value,
                resultListId: selectedResultList.value.id.toString(),
                eventName: selectedEvent.value.name,
                resultListLabel: selectedResultList.value.label,
            },
        })
    }
    else if (selectedAnalysisType.value === 'split-time-ranking') {
        // Navigate to split time analysis page
        router.push({
            name: 'split-time-analysis',
            params: {
                id: selectedEvent.value.id.toString(),
                resultListId: selectedResultList.value.id.toString(),
            },
            query: {
                eventName: selectedEvent.value.name,
                resultListLabel: selectedResultList.value.label,
            },
        })
    }
    else if (selectedAnalysisType.value === 'split-time-table') {
        // Navigate to split time table analysis page
        router.push({
            name: 'split-time-table-analysis',
            query: {
                resultListId: selectedResultList.value.id.toString(),
                eventName: selectedEvent.value.name,
                resultListLabel: selectedResultList.value.label,
            },
        })
    }
    else if (selectedAnalysisType.value === 'cheat-detection') {
        router.push({
            name: 'cheat-detection-analysis',
            query: {
                scope: selectedScope.value,
                resultListId: selectedResultList.value.id.toString(),
                eventName: selectedEvent.value.name,
                resultListLabel: selectedResultList.value.label,
            },
        })
    }
    else if (selectedAnalysisType.value === 'hanging-detection') {
        router.push({
            name: 'hanging-detection-analysis',
            query: {
                scope: selectedScope.value,
                resultListId: selectedResultList.value.id.toString(),
                eventName: selectedEvent.value.name,
                resultListLabel: selectedResultList.value.label,
            },
        })
    }
}
</script>

<template>
    <div class="analysis-hub p-4">
        <!-- Page Title -->
        <div class="mb-6">
            <h1 class="text-3xl font-bold mb-2">
                {{ t('labels.analysis_hub') }}
            </h1>
            <p class="subtitle-text">
                {{ t('messages.select_analysis_type_and_configure') }}
            </p>
        </div>

        <!-- Analysis Type Selection -->
        <div class="mb-6">
            <h2 class="text-xl font-semibold mb-4">
                {{ t('labels.select_analysis') }}
            </h2>
            <div class="analysis-grid">
                <Card
                    v-for="analysisType in analysisTypes"
                    :key="analysisType.key"
                    class="analysis-card"
                    :class="[
                        selectedAnalysisType === analysisType.key ? 'selected' : '',
                        !analysisType.enabled ? 'disabled' : '',
                    ]"
                    @click="analysisType.enabled && selectAnalysisType(analysisType.key)"
                >
                    <template #title>
                        <div class="flex items-center gap-3">
                            <div class="icon-wrapper">
                                <i :class="analysisType.icon" class="text-xl" />
                            </div>
                            <span class="font-semibold">{{ t(analysisType.titleKey) }}</span>
                        </div>
                    </template>
                    <template #content>
                        <p class="description-text text-sm mt-2">
                            {{ t(analysisType.descriptionKey) }}
                        </p>
                        <div v-if="!analysisType.enabled" class="mt-3">
                            <span class="coming-soon-badge">
                                {{ t('labels.coming_soon') }}
                            </span>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- Configuration Panel -->
        <Card v-if="selectedAnalysisType" class="configuration-card mb-6">
            <template #title>
                <div class="flex items-center gap-2">
                    <i class="pi pi-cog text-xl" />
                    <span class="font-semibold">{{ t('labels.configure_analysis') }}</span>
                </div>
            </template>
            <template #content>
                <!-- Scope Selection -->
                <div class="mb-5">
                    <label class="block text-sm font-medium mb-3">
                        {{ t('labels.analysis_scope') }}
                    </label>
                    <div class="flex gap-6">
                        <div class="flex items-center gap-2">
                            <input
                                id="scope-event"
                                v-model="selectedScope"
                                type="radio"
                                value="event"
                            >
                            <label for="scope-event" class="cursor-pointer">{{ t('labels.scope_event') }}</label>
                        </div>
                        <div
                            v-if="authStore.isAdmin"
                            class="flex items-center gap-2"
                        >
                            <input
                                id="scope-cup"
                                type="radio"
                                value="cup"
                                disabled
                            >
                            <label for="scope-cup" class="cursor-not-allowed disabled-label">
                                {{ t('labels.scope_cup') }}
                                <span class="text-xs ml-1 coming-soon-text">({{ t('labels.coming_soon') }})</span>
                            </label>
                        </div>
                        <div
                            v-if="authStore.isAdmin"
                            class="flex items-center gap-2"
                        >
                            <input
                                id="scope-year"
                                type="radio"
                                value="year"
                                disabled
                            >
                            <label for="scope-year" class="cursor-not-allowed disabled-label">
                                {{ t('labels.scope_year') }}
                                <span class="text-xs ml-1 coming-soon-text">({{ t('labels.coming_soon') }})</span>
                            </label>
                        </div>
                    </div>
                </div>

                <!-- Event Scope Configuration -->
                <div v-if="selectedScope === 'event'" class="event-configuration">
                    <!-- Event Selection -->
                    <div>
                        <label class="block text-sm font-medium mb-2">
                            <i class="pi pi-calendar mr-1" />
                            {{ t('labels.event') }}
                        </label>
                        <Select
                            v-model="selectedEvent"
                            :options="events"
                            option-label="name"
                            :placeholder="t('labels.select_event')"
                            class="w-full"
                            :loading="eventsQuery.isLoading.value"
                            filter
                        />
                    </div>

                    <!-- Result List Selection -->
                    <div v-if="selectedEvent">
                        <label class="block text-sm font-medium mb-2">
                            <i class="pi pi-list mr-1" />
                            {{ t('labels.result_list') }}
                        </label>
                        <Select
                            v-model="selectedResultList"
                            :options="resultLists"
                            option-label="label"
                            :placeholder="t('messages.select_result_list')"
                            class="w-full"
                            :loading="resultListsQuery.isLoading.value"
                        >
                            <template #option="slotProps">
                                <div>
                                    <div class="font-semibold">
                                        {{ slotProps.option.label }}
                                    </div>
                                    <div class="text-sm secondary-text">
                                        {{ new Date(slotProps.option.createTime).toLocaleString() }}
                                    </div>
                                </div>
                            </template>
                        </Select>
                    </div>

                    <!-- Start Analysis Button -->
                    <div class="mt-6 pt-4 border-t border-gray-200">
                        <Button
                            :label="t('labels.start_analysis')"
                            icon="pi pi-play"
                            class="start-button"
                            :disabled="!canStartAnalysis"
                            @click="startAnalysis"
                        />
                    </div>
                </div>
            </template>
        </Card>
    </div>
</template>

<style scoped>
.analysis-hub {
    max-width: 1400px;
    margin: 0 auto;
}

/* Analysis Type Grid */
.analysis-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
    gap: 1.5rem;
}

/* Analysis Cards */
.analysis-card {
    cursor: pointer;
    transition: all 0.2s ease-in-out;
    border-radius: 12px;
    border: 2px solid transparent;
}

.analysis-card:not(.disabled):hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.analysis-card.selected {
    border-color: rgb(59, 130, 246);
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.05) 0%, rgba(59, 130, 246, 0.01) 100%);
}

.analysis-card.disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* Icon Wrapper */
.icon-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    border-radius: 10px;
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(59, 130, 246, 0.05) 100%);
    border: 1px solid rgba(59, 130, 246, 0.2);
    color: rgb(59, 130, 246);
}

.analysis-card.selected .icon-wrapper {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(59, 130, 246, 0.08) 100%);
    border-color: rgba(59, 130, 246, 0.3);
}

/* Coming Soon Badge */
.coming-soon-badge {
    display: inline-flex;
    align-items: center;
    padding: 0.25rem 0.75rem;
    border-radius: 6px;
    background: linear-gradient(135deg, rgba(251, 191, 36, 0.12) 0%, rgba(251, 191, 36, 0.05) 100%);
    border: 1px solid rgba(251, 191, 36, 0.3);
    color: rgb(161, 98, 7);
    font-size: 0.75rem;
    font-weight: 500;
}

/* Configuration Card */
.configuration-card {
    border-radius: 12px;
}

/* Start Button */
.start-button {
    padding: 0.75rem 2rem;
    font-size: 1rem;
    font-weight: 600;
}

/* Text Colors */
.subtitle-text {
    color: rgb(75, 85, 99);
}

.description-text {
    color: rgb(75, 85, 99);
}

.secondary-text {
    color: rgb(107, 114, 128);
}

/* Labels */
label {
    font-weight: 500;
    color: rgb(55, 65, 81);
}

label.disabled-label {
    opacity: 0.6;
}

.coming-soon-text {
    color: rgb(107, 114, 128);
}

/* Event Configuration */
.event-configuration > div {
    margin-bottom: 1.25rem;
}

.event-configuration > div:last-child {
    margin-bottom: 0;
}

/* Radio Buttons */
input[type="radio"] {
    width: 1.125rem;
    height: 1.125rem;
    cursor: pointer;
}

input[type="radio"]:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

/* Responsive */
@media (max-width: 768px) {
    .analysis-grid {
        grid-template-columns: 1fr;
    }

    .analysis-hub {
        padding: 1rem;
    }
}

/* Dark Mode Support */
@media (prefers-color-scheme: dark) {
    .analysis-card.selected {
        background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(59, 130, 246, 0.02) 100%);
    }

    .icon-wrapper {
        background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(59, 130, 246, 0.08) 100%);
        border-color: rgba(59, 130, 246, 0.3);
    }

    .coming-soon-badge {
        background: linear-gradient(135deg, rgba(251, 191, 36, 0.15) 0%, rgba(251, 191, 36, 0.08) 100%);
        border-color: rgba(251, 191, 36, 0.35);
        color: rgb(252, 211, 77);
    }

    /* Text Colors in Dark Mode */
    .subtitle-text {
        color: rgb(156, 163, 175);
    }

    .description-text {
        color: rgb(156, 163, 175);
    }

    .secondary-text {
        color: rgb(156, 163, 175);
    }

    label {
        color: rgb(209, 213, 219);
    }

    label.disabled-label {
        opacity: 0.7;
    }

    .coming-soon-text {
        color: rgb(156, 163, 175);
    }

    .border-gray-200 {
        border-color: rgb(55, 65, 81) !important;
    }
}
</style>
