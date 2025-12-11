<script setup lang="ts">
import type { RunnerAnomalyProfile } from '../model/runner_anomaly_profile'
import type { PersonKey } from '@/features/person/model/person_key'
import { useQuery } from '@tanstack/vue-query'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'
import MultiSelect from 'primevue/multiselect'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { EventService } from '@/features/event/services/event.service'

const props = withDefaults(defineProps<{
    scope?: 'event' | 'cup' | 'year'
    resultListId?: number
    cupId?: number
    years?: number[]
    eventName?: string
    resultListLabel?: string
}>(), {
    scope: 'event',
})

const { t } = useI18n()
const router = useRouter()

// State
const filterPersonIds = ref<number[]>([])
const filterClassification = ref<string | null>(null)
const sortBy = ref<'ai' | 'suspicions' | 'name' | 'normalPI'>('ai')
const expandedRows = ref<RunnerAnomalyProfile[]>([])
const showHelpDialog = ref(false)

// Fetch persons for filtering
const personsQuery = useQuery({
    queryKey: ['personsForResultList', props.resultListId],
    queryFn: () => {
        if (!props.resultListId)
            return Promise.resolve([])
        return EventService.getPersonsForResultList(props.resultListId, t)
    },
    enabled: computed(() => props.resultListId !== undefined),
})

// Fetch Cheat Detection analysis data
const anomalyQuery = useQuery({
    queryKey: ['anomalyDetection', props.resultListId, filterPersonIds],
    queryFn: () => {
        if (!props.resultListId)
            return Promise.resolve(null)
        return EventService.getAnomalyDetectionAnalysis(
            props.resultListId,
            filterPersonIds.value,
            t,
        )
    },
    enabled: computed(() => props.resultListId !== undefined),
})

// Create person map for lookups
const personMap = computed(() => {
    if (!personsQuery.data.value)
        return new Map<number, PersonKey>()
    return new Map(personsQuery.data.value.map(p => [p.id, p]))
})

// Person list with full names for MultiSelect
const personsWithFullName = computed(() => {
    if (!personsQuery.data.value)
        return []
    return personsQuery.data.value.map(p => ({
        ...p,
        fullName: `${p.familyName}, ${p.givenName}`,
    }))
})

// Get person name by ID
function getPersonName(personId: number): string {
    const person = personMap.value.get(personId)
    return person ? `${person.familyName}, ${person.givenName}` : 'Unknown'
}

// Filter and sort runner profiles
const filteredAndSortedProfiles = computed(() => {
    if (!anomalyQuery.data.value)
        return []

    // Filter out NO_DATA entries - they have no meaningful analysis results
    let profiles = anomalyQuery.data.value.runnerProfiles
        .filter(p => p.classification !== 'NO_DATA')

    // Apply classification filter
    if (filterClassification.value) {
        profiles = profiles.filter(p => p.classification === filterClassification.value)
    }

    // Sort profiles
    const sorted = [...profiles].sort((a, b) => {
        if (sortBy.value === 'ai') {
            return a.minimumAnomaliesIndex - b.minimumAnomaliesIndex
        }
        else if (sortBy.value === 'suspicions') {
            return b.anomaliesIndexes.length - a.anomaliesIndexes.length
        }
        else if (sortBy.value === 'normalPI') {
            return a.normalPI - b.normalPI
        }
        else { // 'name'
            const nameA = getPersonName(a.personId)
            const nameB = getPersonName(b.personId)
            return nameA.localeCompare(nameB)
        }
    })

    // Add position
    return sorted.map((profile, index) => ({
        ...profile,
        position: index + 1,
    }))
})

// Check if any classes have unreliable data (< 5 runners)
const hasUnreliableClasses = computed(() => {
    if (!anomalyQuery.data.value)
        return false
    return anomalyQuery.data.value.runnerProfiles.some(p => !p.reliableData)
})

// Classification options
const classificationOptions = [
    { label: t('labels.high_suspicion'), value: 'HIGH_SUSPICION' },
    { label: t('labels.moderate_suspicion'), value: 'MODERATE_SUSPICION' },
    { label: t('labels.no_suspicion'), value: 'NO_SUSPICION' },
]

// Sort options
const sortOptions = [
    { label: t('labels.anomalies_index'), value: 'ai' },
    { label: t('labels.suspicion_count'), value: 'suspicions' },
    { label: t('labels.normal_pi'), value: 'normalPI' },
    { label: t('labels.name'), value: 'name' },
]

// Get classification severity for Tag component
function getClassificationSeverity(classification: string): 'success' | 'warn' | 'danger' | 'info' {
    if (classification === 'HIGH_SUSPICION')
        return 'danger'
    if (classification === 'MODERATE_SUSPICION')
        return 'warn'
    if (classification === 'NO_SUSPICION')
        return 'success'
    return 'info' // NO_DATA
}

// Format AI value with precision
function formatAI(value: number): string {
    return value.toFixed(3)
}

// Format time in seconds to MM:SS format
function formatTime(seconds: number): string {
    const minutes = Math.floor(seconds / 60)
    const secs = Math.floor(seconds % 60)
    return `${minutes}:${secs.toString().padStart(2, '0')}`
}

// Remove person filter
function removePersonFilter(personId: number) {
    filterPersonIds.value = filterPersonIds.value.filter(id => id !== personId)
}
</script>

<template>
    <div class="anomaly-detection-analysis p-4">
        <!-- Header -->
        <div class="mb-4">
            <div class="flex items-center justify-between">
                <div class="flex items-center">
                    <Button
                        icon="pi pi-arrow-left"
                        text
                        @click="router.back()"
                    />
                    <div class="ml-2">
                        <h1 class="text-3xl font-bold">
                            {{ t('labels.anomaly_detection_analysis') }}
                        </h1>
                        <div v-if="eventName" class="text-lg text-gray-600 mt-1">
                            {{ eventName }}
                            <span v-if="resultListLabel" class="ml-2">
                                • {{ resultListLabel }}
                            </span>
                        </div>
                    </div>
                </div>
                <Button
                    icon="pi pi-question-circle"
                    :label="t('labels.help')"
                    outlined
                    @click="showHelpDialog = true"
                />
            </div>
        </div>

        <!-- Loading State -->
        <div v-if="anomalyQuery.isLoading.value" class="text-center p-8">
            <i class="pi pi-spin pi-spinner text-4xl" />
        </div>

        <!-- Content -->
        <div v-else-if="anomalyQuery.data.value" class="space-y-4">
            <!-- Statistics Overview -->
            <Card>
                <template #title>
                    {{ t('labels.statistics') }}
                </template>
                <template #content>
                    <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
                        <div class="text-center">
                            <div class="text-3xl font-bold text-blue-600">
                                {{ anomalyQuery.data.value.runnerProfiles.filter(p => p.classification !== 'NO_DATA').length }}
                            </div>
                            <div class="text-sm text-gray-600">
                                {{ t('labels.total_runners') }}
                            </div>
                        </div>
                        <div class="text-center">
                            <div class="text-3xl font-bold text-red-600">
                                {{ anomalyQuery.data.value.runnerProfiles.filter(p => p.classification === 'HIGH_SUSPICION').length }}
                            </div>
                            <div class="text-sm text-gray-600">
                                {{ t('labels.high_suspicion') }}
                            </div>
                        </div>
                        <div class="text-center">
                            <div class="text-3xl font-bold text-orange-600">
                                {{ anomalyQuery.data.value.runnerProfiles.filter(p => p.classification === 'MODERATE_SUSPICION').length }}
                            </div>
                            <div class="text-sm text-gray-600">
                                {{ t('labels.moderate_suspicion') }}
                            </div>
                        </div>
                    </div>
                </template>
            </Card>

            <!-- Filters -->
            <Card>
                <template #title>
                    {{ t('labels.filters') }}
                </template>
                <template #content>
                    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <!-- Person Filter -->
                        <div>
                            <label class="block text-sm font-medium mb-2">
                                {{ t('labels.filter_by_name') }}
                            </label>
                            <MultiSelect
                                v-model="filterPersonIds"
                                :options="personsWithFullName"
                                option-label="fullName"
                                option-value="id"
                                :placeholder="t('labels.select_persons')"
                                class="w-full"
                                filter
                                display="chip"
                            >
                                <template #chip="slotProps">
                                    <div class="flex items-center">
                                        <span>{{ getPersonName(slotProps.value) }}</span>
                                        <i
                                            class="pi pi-times ml-2 cursor-pointer"
                                            @click.stop="removePersonFilter(slotProps.value)"
                                        />
                                    </div>
                                </template>
                            </MultiSelect>
                        </div>

                        <!-- Classification Filter -->
                        <div>
                            <label class="block text-sm font-medium mb-2">
                                {{ t('labels.filter_by_classification') }}
                            </label>
                            <Select
                                v-model="filterClassification"
                                :options="classificationOptions"
                                option-label="label"
                                option-value="value"
                                :placeholder="t('labels.all')"
                                class="w-full"
                                show-clear
                            />
                        </div>

                        <!-- Sort Options -->
                        <div>
                            <label class="block text-sm font-medium mb-2">
                                {{ t('labels.sort_by') }}
                            </label>
                            <Select
                                v-model="sortBy"
                                :options="sortOptions"
                                option-label="label"
                                option-value="value"
                                class="w-full"
                            />
                        </div>
                    </div>
                </template>
            </Card>

            <!-- Unreliable Data Warning -->
            <Message
                v-if="hasUnreliableClasses"
                severity="warn"
                :closable="false"
                class="mb-4"
            >
                {{ t('messages.unreliable_class_warning') }}
            </Message>

            <!-- Runner Profiles Table -->
            <Card>
                <template #title>
                    {{ t('labels.runner_profiles') }} ({{ filteredAndSortedProfiles.length }})
                </template>
                <template #content>
                    <DataTable
                        v-model:expanded-rows="expandedRows"
                        :value="filteredAndSortedProfiles"
                        striped-rows
                        size="small"
                        :rows="20"
                        paginator
                    >
                        <Column expander style="width: 3rem" />
                        <Column field="position" :header="t('labels.position')" style="width: 100px" />
                        <Column :header="t('labels.name')">
                            <template #body="slotProps">
                                {{ getPersonName(slotProps.data.personId) }}
                            </template>
                        </Column>
                        <Column field="classResultShortName" :header="t('labels.class')" style="width: 120px">
                            <template #body="slotProps">
                                <div class="flex items-center gap-2">
                                    {{ slotProps.data.classResultShortName }}
                                    <Tag
                                        v-if="!slotProps.data.reliableData"
                                        v-tooltip.top="t('messages.low_runner_count_warning')"
                                        severity="warn"
                                        :value="`${slotProps.data.classRunnerCount}`"
                                        rounded
                                    />
                                </div>
                            </template>
                        </Column>
                        <Column :header="t('labels.classification')" style="width: 150px">
                            <template #body="slotProps">
                                <Tag
                                    :value="t(`labels.${slotProps.data.classification.toLowerCase()}`)"
                                    :severity="getClassificationSeverity(slotProps.data.classification)"
                                />
                            </template>
                        </Column>
                        <Column field="minimumAnomaliesIndex" :header="t('labels.min_ai')" style="width: 120px">
                            <template #body="slotProps">
                                <span
                                    :class="{
                                        'text-red-600 font-semibold': slotProps.data.minimumAnomaliesIndex < 0.75,
                                        'text-orange-600 font-semibold': slotProps.data.minimumAnomaliesIndex >= 0.75 && slotProps.data.minimumAnomaliesIndex < 0.85,
                                        'text-green-600': slotProps.data.minimumAnomaliesIndex >= 0.85,
                                    }"
                                >
                                    {{ formatAI(slotProps.data.minimumAnomaliesIndex) }}
                                </span>
                            </template>
                        </Column>
                        <Column field="anomaliesIndexes.length" :header="t('labels.suspicion_count')" style="width: 120px" />
                        <Column field="normalPI" :header="t('labels.normal_pi')" style="width: 120px">
                            <template #body="slotProps">
                                {{ slotProps.data.normalPI.toFixed(3) }}
                            </template>
                        </Column>

                        <!-- Expanded Row Template -->
                        <template #expansion="slotProps">
                            <div class="p-4 bg-gray-50">
                                <h4 class="font-semibold mb-3">
                                    {{ t('labels.anomaly_details') }}
                                </h4>
                                <DataTable
                                    :value="slotProps.data.anomaliesIndexes.filter((a: any) => a.classification !== 'NO_DATA')"
                                    size="small"
                                >
                                    <Column :header="t('labels.leg')" style="width: 80px">
                                        <template #body="cellProps">
                                            {{ cellProps.data.legNumber + 1 }}
                                        </template>
                                    </Column>
                                    <Column :header="t('labels.segment')" style="width: 150px">
                                        <template #body="cellProps">
                                            {{ cellProps.data.fromControl }} → {{ cellProps.data.toControl }}
                                        </template>
                                    </Column>
                                    <Column :header="t('labels.actual_time')" style="width: 100px">
                                        <template #body="cellProps">
                                            {{ formatTime(cellProps.data.actualTimeSeconds) }}
                                        </template>
                                    </Column>
                                    <Column :header="t('labels.reference_time')" style="width: 120px">
                                        <template #body="cellProps">
                                            {{ formatTime(cellProps.data.referenceTimeSeconds) }}
                                        </template>
                                    </Column>
                                    <Column :header="t('labels.classification')" style="width: 140px">
                                        <template #body="cellProps">
                                            <Tag
                                                :value="t(`labels.${cellProps.data.classification.toLowerCase()}`)"
                                                :severity="getClassificationSeverity(cellProps.data.classification)"
                                            />
                                        </template>
                                    </Column>
                                </DataTable>
                            </div>
                        </template>
                    </DataTable>
                </template>
            </Card>
        </div>

        <!-- No Data State -->
        <Card v-else-if="!anomalyQuery.isLoading.value && !anomalyQuery.data.value">
            <template #content>
                <div class="text-center p-8 text-gray-500">
                    <i class="pi pi-info-circle text-4xl mb-4" />
                    <p>{{ t('messages.no_anomaly_detection_data') }}</p>
                </div>
            </template>
        </Card>

        <!-- Help Dialog -->
        <Dialog
            v-model:visible="showHelpDialog"
            :header="t('labels.help')"
            :style="{ width: '50rem' }"
            :breakpoints="{ '1199px': '75vw', '575px': '90vw' }"
            modal
        >
            <div class="space-y-4">
                <!-- AI Explanation -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('labels.anomalies_index') }} (AI)
                    </h3>
                    <p class="mb-2">
                        {{ t('messages.ai_explanation') }}
                    </p>
                    <div class="text-color bg-adaptive p-3 rounded">
                        <p class="font-semibold mb-1">
                            {{ t('labels.calculation') }}:
                        </p>
                        <p class="font-mono text-sm">
                            AI = PI_Real / PI_Expected
                        </p>
                        <p class="text-sm mt-2">
                            {{ t('messages.ai_calculation_detail') }}
                        </p>
                    </div>
                </div>

                <!-- Classification Explanation -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('labels.classifications') }}
                    </h3>
                    <div class="space-y-2">
                        <div class="flex items-start">
                            <Tag :value="t('labels.high_suspicion')" severity="danger" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.high_suspicion') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.high_suspicion_explanation') }}
                                </p>
                            </div>
                        </div>
                        <div class="flex items-start">
                            <Tag :value="t('labels.moderate_suspicion')" severity="warn" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.moderate_suspicion') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.moderate_suspicion_explanation') }}
                                </p>
                            </div>
                        </div>
                        <div class="flex items-start">
                            <Tag :value="t('labels.no_suspicion')" severity="success" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.no_suspicion') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.no_suspicion_explanation') }}
                                </p>
                            </div>
                        </div>
                        <div class="flex items-start">
                            <Tag :value="t('labels.no_data')" severity="info" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.no_data') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.no_data_explanation') }}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </Dialog>
    </div>
</template>

<style scoped>
.anomaly-detection-analysis {
    max-width: 1400px;
    margin: 0 auto;
}
</style>
