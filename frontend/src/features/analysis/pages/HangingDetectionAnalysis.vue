<script setup lang="ts">
import type { RunnerHangingProfile } from '../model/runner_hanging_profile'
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
const sortBy = ref<'hi' | 'hangings' | 'name' | 'normalPI'>('hi')
const expandedRows = ref<RunnerHangingProfile[]>([])
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

// Fetch Hanging Detection analysis data
const hangingQuery = useQuery({
    queryKey: ['hangingDetection', props.resultListId, filterPersonIds],
    queryFn: () => {
        if (!props.resultListId)
            return Promise.resolve(null)
        return EventService.getHangingDetectionAnalysis(
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
    if (!hangingQuery.data.value)
        return []

    // Filter out entries without hanging behavior
    // INSUFFICIENT_DATA: no meaningful analysis results
    // NO_HANGING: no hanging detected
    let profiles = hangingQuery.data.value.runnerProfiles
        .filter(p => p.classification !== 'INSUFFICIENT_DATA' && p.classification !== 'NO_HANGING')

    // Apply classification filter
    if (filterClassification.value) {
        profiles = profiles.filter(p => p.classification === filterClassification.value)
    }

    // Sort profiles
    const sorted = [...profiles].sort((a, b) => {
        if (sortBy.value === 'hi') {
            // Sort by average hanging index (lower is worse/more hanging)
            return a.averageHangingIndex - b.averageHangingIndex
        }
        else if (sortBy.value === 'hangings') {
            return b.hangingCount - a.hangingCount
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

// Calculate start positions within each class
computed(() => {
    if (!hangingQuery.data.value)
        return new Map<number, number>()

    const map = new Map<number, number>()

    // Group runners by class
    const classesByName = new Map<string, RunnerHangingProfile[]>()
    for (const profile of hangingQuery.data.value.runnerProfiles) {
        if (!classesByName.has(profile.classResultShortName)) {
            classesByName.set(profile.classResultShortName, [])
        }
        classesByName.get(profile.classResultShortName)!.push(profile)
    }

    // For each class, sort by start time and assign positions
    for (const runners of classesByName.values()) {
        const sorted = [...runners]
            .filter(r => r.startTime !== null)
            .sort((a, b) => (a.startTime ?? 0) - (b.startTime ?? 0))

        sorted.forEach((runner, index) => {
            map.set(runner.personId, index + 1)
        })
    }

    return map
})

// Get start time for a runner and format it as HH:mm or HH:mm:ss
function getStartTime(personId: number): string | null {
    if (!hangingQuery.data.value)
        return null

    const profile = hangingQuery.data.value.runnerProfiles.find(p => p.personId === personId)
    if (!profile || profile.startTime === null)
        return null

    // Convert epoch seconds to Date and format as HH:mm or HH:mm:ss
    const date = new Date(profile.startTime * 1000)
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = date.getSeconds()

    // If seconds are 0, use HH:mm format, otherwise HH:mm:ss
    if (seconds === 0) {
        return `${hours}:${minutes}`
    }
    else {
        const secondsStr = String(seconds).padStart(2, '0')
        return `${hours}:${minutes}:${secondsStr}`
    }
}

// Check if any classes have unreliable data (< 5 runners)
const hasUnreliableClasses = computed(() => {
    if (!hangingQuery.data.value)
        return false
    return hangingQuery.data.value.runnerProfiles.some(p => !p.reliableData)
})

// Classification options
const classificationOptions = [
    { label: t('labels.high_hanging'), value: 'HIGH_HANGING' },
    { label: t('labels.moderate_hanging'), value: 'MODERATE_HANGING' },
    { label: t('labels.no_hanging'), value: 'NO_HANGING' },
]

// Sort options
const sortOptions = [
    { label: t('labels.hanging_index'), value: 'hi' },
    { label: t('labels.hanging_count'), value: 'hangings' },
    { label: t('labels.normal_pi'), value: 'normalPI' },
    { label: t('labels.name'), value: 'name' },
]

// Get classification severity for Tag component
function getClassificationSeverity(classification: string): 'success' | 'warn' | 'danger' | 'info' {
    if (classification === 'HIGH_HANGING')
        return 'danger'
    if (classification === 'MODERATE_HANGING')
        return 'warn'
    if (classification === 'NO_HANGING')
        return 'success'
    return 'info' // INSUFFICIENT_DATA
}

// Format HI value with precision
function formatHI(value: number): string {
    return value.toFixed(3)
}

// Format percentage
function formatPercentage(value: number): string {
    return `${value.toFixed(1)}%`
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
    <div class="hanging-detection-analysis p-4">
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
                            {{ t('labels.hanging_detection_analysis') }}
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
        <div v-if="hangingQuery.isLoading.value" class="text-center p-8">
            <i class="pi pi-spin pi-spinner text-4xl" />
        </div>

        <!-- Content -->
        <div v-else-if="hangingQuery.data.value" class="space-y-4">
            <!-- Statistics Overview -->
            <Card>
                <template #title>
                    {{ t('labels.statistics') }}
                </template>
                <template #content>
                    <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
                        <div class="text-center">
                            <div class="text-3xl font-bold text-blue-600">
                                {{ hangingQuery.data.value.runnerProfiles.filter(p => p.classification !== 'INSUFFICIENT_DATA').length }}
                            </div>
                            <div class="text-sm text-gray-600">
                                {{ t('labels.total_runners') }}
                            </div>
                        </div>
                        <div class="text-center">
                            <div class="text-3xl font-bold text-red-600">
                                {{ hangingQuery.data.value.runnerProfiles.filter(p => p.classification === 'HIGH_HANGING').length }}
                            </div>
                            <div class="text-sm text-gray-600">
                                {{ t('labels.high_hanging') }}
                            </div>
                        </div>
                        <div class="text-center">
                            <div class="text-3xl font-bold text-orange-600">
                                {{ hangingQuery.data.value.runnerProfiles.filter(p => p.classification === 'MODERATE_HANGING').length }}
                            </div>
                            <div class="text-sm text-gray-600">
                                {{ t('labels.moderate_hanging') }}
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
                        <Column :header="t('labels.name')" style="width: 200px">
                            <template #body="slotProps">
                                {{ getPersonName(slotProps.data.personId) }}
                            </template>
                        </Column>
                        <Column field="classResultShortName" :header="t('labels.class')" style="width: 180px">
                            <template #body="slotProps">
                                <div class="flex items-center gap-2">
                                    <span>
                                        {{ slotProps.data.classResultShortName }}
                                        <span
                                            v-if="getStartTime(slotProps.data.personId)"
                                            class="text-gray-500 text-sm"
                                        >
                                            ({{ getStartTime(slotProps.data.personId) }})
                                        </span>
                                    </span>
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
                        <Column field="averageHangingIndex" :header="t('labels.avg_hi')" style="width: 120px">
                            <template #body="slotProps">
                                <span
                                    v-if="slotProps.data.hangingCount > 0"
                                    :class="{
                                        'text-red-600 font-semibold': slotProps.data.averageHangingIndex < 0.75,
                                        'text-orange-600 font-semibold': slotProps.data.averageHangingIndex >= 0.75 && slotProps.data.averageHangingIndex < 0.85,
                                        'text-green-600': slotProps.data.averageHangingIndex >= 0.85,
                                    }"
                                >
                                    {{ formatHI(slotProps.data.averageHangingIndex) }}
                                </span>
                                <span v-else class="text-gray-400">—</span>
                            </template>
                        </Column>
                        <Column field="hangingCount" :header="t('labels.hanging_count')" style="width: 120px" />
                        <Column field="hangingPercentage" :header="t('labels.hanging_percentage')" style="width: 120px">
                            <template #body="slotProps">
                                {{ formatPercentage(slotProps.data.hangingPercentage) }}
                            </template>
                        </Column>
                        <Column field="normalPI" :header="t('labels.normal_pi')" style="width: 120px">
                            <template #body="slotProps">
                                {{ slotProps.data.normalPI.toFixed(3) }}
                            </template>
                        </Column>

                        <!-- Expanded Row Template -->
                        <template #expansion="slotProps">
                            <div class="p-4 bg-gray-50">
                                <h4 class="font-semibold mb-3">
                                    {{ t('labels.hanging_details') }}
                                </h4>
                                <DataTable
                                    :value="slotProps.data.hangingPairs"
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
                                    <Column :header="t('labels.bus_driver')" style="width: 250px">
                                        <template #body="cellProps">
                                            {{ getPersonName(cellProps.data.busDriverId) }}
                                            <span class="text-gray-500 text-sm ml-1">
                                                ({{ cellProps.data.busDriverClassName }}<span
                                                    v-if="getStartTime(cellProps.data.busDriverId)"
                                                >
                                                    - {{ getStartTime(cellProps.data.busDriverId) }}</span>)
                                            </span>
                                        </template>
                                    </Column>
                                    <Column :header="t('labels.time_delta')" style="width: 100px">
                                        <template #body="cellProps">
                                            {{ cellProps.data.timeDeltaSeconds.toFixed(1) }}s
                                        </template>
                                    </Column>
                                    <Column :header="t('labels.hanging_index')" style="width: 100px">
                                        <template #body="cellProps">
                                            <span
                                                :class="{
                                                    'text-red-600 font-semibold': cellProps.data.hangingIndex < 0.75,
                                                    'text-orange-600 font-semibold': cellProps.data.hangingIndex >= 0.75 && cellProps.data.hangingIndex < 0.85,
                                                }"
                                            >
                                                {{ formatHI(cellProps.data.hangingIndex) }}
                                            </span>
                                        </template>
                                    </Column>
                                    <Column :header="t('labels.improvement')" style="width: 100px">
                                        <template #body="cellProps">
                                            {{ formatPercentage(cellProps.data.improvementPercent) }}
                                        </template>
                                    </Column>
                                    <Column :header="t('labels.passenger_time')" style="width: 100px">
                                        <template #body="cellProps">
                                            {{ formatTime(cellProps.data.passengerActualTime) }}
                                        </template>
                                    </Column>
                                    <Column :header="t('labels.bus_driver_time')" style="width: 100px">
                                        <template #body="cellProps">
                                            {{ formatTime(cellProps.data.busDriverActualTime) }}
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
        <Card v-else-if="!hangingQuery.isLoading.value && !hangingQuery.data.value">
            <template #content>
                <div class="text-center p-8 text-gray-500">
                    <i class="pi pi-info-circle text-4xl mb-4" />
                    <p>{{ t('messages.no_hanging_detection_data') }}</p>
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
                <!-- HI Explanation -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('labels.hanging_index') }} (HI)
                    </h3>
                    <p class="mb-2">
                        {{ t('messages.hi_explanation') }}
                    </p>
                    <div class="text-color bg-adaptive p-3 rounded">
                        <p class="font-semibold mb-1">
                            {{ t('labels.calculation') }}:
                        </p>
                        <p class="font-mono text-sm">
                            HI = PI_Segment / PI_Expected (NormalPI)
                        </p>
                        <p class="text-sm mt-2">
                            {{ t('messages.hi_calculation_detail') }}
                        </p>
                    </div>
                </div>

                <!-- Detection Criteria -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('labels.detection_criteria') }}
                    </h3>
                    <p class="mb-2">
                        {{ t('messages.hanging_detection_criteria') }}
                    </p>
                    <ol class="list-decimal list-inside space-y-1 text-sm">
                        <li>{{ t('messages.criteria_temporal_proximity') }}</li>
                        <li>{{ t('messages.criteria_performance_hierarchy') }}</li>
                        <li>{{ t('messages.criteria_performance_improvement') }}</li>
                    </ol>
                </div>

                <!-- Classification Explanation -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('labels.classifications') }}
                    </h3>
                    <div class="space-y-2">
                        <div class="flex items-start">
                            <Tag :value="t('labels.high_hanging')" severity="danger" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.high_hanging') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.high_hanging_explanation') }}
                                </p>
                            </div>
                        </div>
                        <div class="flex items-start">
                            <Tag :value="t('labels.moderate_hanging')" severity="warn" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.moderate_hanging') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.moderate_hanging_explanation') }}
                                </p>
                            </div>
                        </div>
                        <div class="flex items-start">
                            <Tag :value="t('labels.no_hanging')" severity="success" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.no_hanging') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.no_hanging_explanation') }}
                                </p>
                            </div>
                        </div>
                        <div class="flex items-start">
                            <Tag :value="t('labels.insufficient_data')" severity="info" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.insufficient_data') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.insufficient_data_explanation') }}
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
.hanging-detection-analysis {
    max-width: 1400px;
    margin: 0 auto;
}
</style>
