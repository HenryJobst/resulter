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
import { eventService, EventService } from '@/features/event/services/event.service'

const { t } = useI18n()
const router = useRouter()

// State
const selectedAnalysisType = ref<AnalysisType | null>(null)
const selectedScope = ref<AnalysisScope>('event')
const selectedEvent = ref<SportEvent | null>(null)
const selectedResultList = ref<ResultList | null>(null)

// Available analysis types
const analysisTypes = computed<AnalysisTypeInfo[]>(() => [
    {
        key: 'mental-resilience',
        titleKey: 'labels.mental_resilience_analysis',
        descriptionKey: 'messages.mri_description',
        icon: 'pi pi-chart-line',
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
        key: 'cheat-detection',
        titleKey: 'labels.anomaly_detection_analysis',
        descriptionKey: 'messages.anomaly_detection_description',
        icon: 'pi pi-exclamation-triangle',
        enabled: true,
    },
])

// Fetch all events for selection
const eventsQuery = useQuery({
    queryKey: ['events'],
    queryFn: () => eventService.getAll(t),
})

// Events list for dropdown (sorted by date, newest first)
const events = computed(() => {
    if (!eventsQuery.data.value)
        return []
    const eventList = (eventsQuery.data.value.content || []).filter(e => e.hasSplitTimes)
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
}
</script>

<template>
    <div class="analysis-hub p-4">
        <!-- Page Title -->
        <div class="mb-4">
            <h1 class="text-3xl font-bold">
                {{ t('labels.analysis_hub') }}
            </h1>
            <p class="text-gray-600 mt-2">
                {{ t('messages.select_analysis_type_and_configure') }}
            </p>
        </div>

        <!-- Analysis Type Selection -->
        <div class="mb-6">
            <h2 class="text-xl font-semibold mb-3">
                {{ t('labels.select_analysis') }}
            </h2>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <Card
                    v-for="analysisType in analysisTypes"
                    :key="analysisType.key"
                    class="cursor-pointer transition-all hover:shadow-lg" :class="[
                        selectedAnalysisType === analysisType.key ? 'ring-2 ring-blue-500' : '',
                        !analysisType.enabled ? 'opacity-50 cursor-not-allowed' : '',
                    ]"
                    @click="analysisType.enabled && selectAnalysisType(analysisType.key)"
                >
                    <template #title>
                        <div class="flex items-center gap-2">
                            <i :class="analysisType.icon" class="text-2xl" />
                            <span>{{ t(analysisType.titleKey) }}</span>
                        </div>
                    </template>
                    <template #content>
                        <p class="text-sm text-gray-600">
                            {{ t(analysisType.descriptionKey) }}
                        </p>
                        <div v-if="!analysisType.enabled" class="mt-2">
                            <span class="text-xs bg-yellow-100 text-yellow-800 px-2 py-1 rounded">
                                {{ t('labels.coming_soon') }}
                            </span>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- Configuration Panel -->
        <Card v-if="selectedAnalysisType" class="mb-6">
            <template #title>
                {{ t('labels.configure_analysis') }}
            </template>
            <template #content>
                <!-- Scope Selection -->
                <div class="mb-4">
                    <label class="block text-sm font-medium mb-2">
                        {{ t('labels.analysis_scope') }}
                    </label>
                    <div class="flex gap-4">
                        <div class="flex items-center">
                            <input
                                id="scope-event"
                                v-model="selectedScope"
                                type="radio"
                                value="event"
                                class="mr-2"
                            >
                            <label for="scope-event">{{ t('labels.scope_event') }}</label>
                        </div>
                        <div class="flex items-center opacity-50">
                            <input
                                id="scope-cup"
                                type="radio"
                                value="cup"
                                disabled
                                class="mr-2"
                            >
                            <label for="scope-cup">
                                {{ t('labels.scope_cup') }}
                                <span class="text-xs ml-1">({{ t('labels.coming_soon') }})</span>
                            </label>
                        </div>
                        <div class="flex items-center opacity-50">
                            <input
                                id="scope-year"
                                type="radio"
                                value="year"
                                disabled
                                class="mr-2"
                            >
                            <label for="scope-year">
                                {{ t('labels.scope_year') }}
                                <span class="text-xs ml-1">({{ t('labels.coming_soon') }})</span>
                            </label>
                        </div>
                    </div>
                </div>

                <!-- Event Scope Configuration -->
                <div v-if="selectedScope === 'event'" class="space-y-4">
                    <!-- Event Selection -->
                    <div>
                        <label class="block text-sm font-medium mb-2">
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
                                    <div class="text-sm text-gray-600">
                                        {{ new Date(slotProps.option.createTime).toLocaleString() }}
                                    </div>
                                </div>
                            </template>
                        </Select>
                    </div>

                    <!-- Start Analysis Button -->
                    <div class="mt-4">
                        <Button
                            :label="t('labels.start_analysis')"
                            icon="pi pi-play"
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
    max-width: 1200px;
    margin: 0 auto;
}
</style>
