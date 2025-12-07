<script setup lang="ts">
import type { RunnerMentalProfile } from '../model/runner_mental_profile'
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
const sortBy = ref<'mri' | 'mistakes' | 'name' | 'normalPI'>('mri')
const expandedRows = ref<RunnerMentalProfile[]>([])
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

// Fetch MRI analysis data
const mriQuery = useQuery({
    queryKey: ['mentalResilience', props.resultListId, filterPersonIds],
    queryFn: () => {
        if (!props.resultListId)
            return Promise.resolve(null)
        return EventService.getMentalResilienceAnalysis(
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
    if (!mriQuery.data.value)
        return []

    let profiles = mriQuery.data.value.runnerProfiles

    // Apply classification filter
    if (filterClassification.value) {
        profiles = profiles.filter(p => p.classification === filterClassification.value)
    }

    // Sort profiles
    const sorted = [...profiles].sort((a, b) => {
        if (sortBy.value === 'mri') {
            return a.averageMRI - b.averageMRI
        }
        else if (sortBy.value === 'mistakes') {
            return b.mistakeCount - a.mistakeCount
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

// Statistics
const statistics = computed(() => mriQuery.data.value?.statistics)

// Check if any classes have unreliable data (< 5 runners)
const hasUnreliableClasses = computed(() => {
    if (!mriQuery.data.value)
        return false
    return mriQuery.data.value.runnerProfiles.some(p => !p.reliableData)
})

// Classification options
const classificationOptions = [
    { label: t('labels.panic'), value: 'panic' },
    { label: t('labels.ice_man'), value: 'ice_man' },
    { label: t('labels.resigner'), value: 'resigner' },
    { label: t('labels.chain_error'), value: 'chain_error' },
]

// Sort options
const sortOptions = [
    { label: t('labels.mri'), value: 'mri' },
    { label: t('labels.mistake_count'), value: 'mistakes' },
    { label: t('labels.normal_pi'), value: 'normalPI' },
    { label: t('labels.name'), value: 'name' },
]

// Get severity color for Tag component
function getSeverityColor(severity: string): 'success' | 'warn' | 'danger' {
    if (severity === 'moderate')
        return 'success'
    if (severity === 'major')
        return 'warn'
    return 'danger'
}

// Get classification severity for Tag component
function getClassificationSeverity(classification: string): 'success' | 'warn' | 'danger' | 'info' {
    if (classification === 'panic')
        return 'danger'
    if (classification === 'ice_man')
        return 'success'
    if (classification === 'chain_error')
        return 'info'
    return 'warn' // resigner
}

// Format MRI value with sign
function formatMRI(value: number): string {
    const sign = value >= 0 ? '+' : ''
    return `${sign}${value.toFixed(3)}`
}

// Remove person filter
function removePersonFilter(personId: number) {
    filterPersonIds.value = filterPersonIds.value.filter(id => id !== personId)
}
</script>

<template>
    <div class="mental-resilience-analysis p-4">
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
                            {{ t('labels.mental_resilience_analysis') }}
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
        <div v-if="mriQuery.isLoading.value" class="text-center p-8">
            <i class="pi pi-spin pi-spinner text-4xl" />
        </div>

        <!-- Content -->
        <div v-else-if="mriQuery.data.value" class="space-y-4">
            <!-- Statistics Overview -->
            <Card>
                <template #title>
                    {{ t('labels.statistics') }}
                </template>
                <template #content>
                    <div v-if="statistics" class="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <div class="text-center">
                            <div class="text-3xl font-bold text-blue-600">
                                {{ statistics.totalRunners }}
                            </div>
                            <div class="text-sm text-gray-600">
                                {{ t('labels.total_runners') }}
                            </div>
                        </div>
                        <div class="text-center">
                            <div class="text-3xl font-bold text-orange-600">
                                {{ statistics.runnersWithMistakes }}
                            </div>
                            <div class="text-sm text-gray-600">
                                {{ t('labels.runners_with_mistakes') }}
                            </div>
                        </div>
                        <div class="text-center">
                            <div class="text-3xl font-bold text-red-600">
                                {{ statistics.totalMistakes }}
                            </div>
                            <div class="text-sm text-gray-600">
                                {{ t('labels.total_mistakes') }}
                            </div>
                        </div>
                        <div class="text-center">
                            <div class="text-3xl font-bold text-purple-600">
                                {{ statistics.averageMRI?.toFixed(3) ?? 'N/A' }}
                            </div>
                            <div class="text-sm text-gray-600">
                                {{ t('labels.average_mri') }}
                            </div>
                        </div>
                    </div>

                    <!-- Distribution -->
                    <div class="mt-6 grid grid-cols-3 gap-4">
                        <div class="text-center p-3 bg-red-50 rounded">
                            <div class="text-2xl font-bold text-red-600">
                                {{ statistics?.panicReactions ?? "-" }}
                            </div>
                            <div class="text-sm text-red-700">
                                {{ t('labels.panic') }}
                            </div>
                        </div>
                        <div class="text-center p-3 bg-green-50 rounded">
                            <div class="text-2xl font-bold text-green-600">
                                {{ statistics?.iceManReactions ?? "-" }}
                            </div>
                            <div class="text-sm text-green-700">
                                {{ t('labels.ice_man') }}
                            </div>
                        </div>
                        <div class="text-center p-3 bg-yellow-50 rounded">
                            <div class="text-2xl font-bold text-yellow-600">
                                {{ statistics?.resignerReactions ?? "-" }}
                            </div>
                            <div class="text-sm text-yellow-700">
                                {{ t('labels.resigner') }}
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
                                    :value="t(`labels.${slotProps.data.classification}`)"
                                    :severity="getClassificationSeverity(slotProps.data.classification)"
                                />
                            </template>
                        </Column>
                        <Column field="averageMRI" :header="t('labels.average_mri')" style="width: 120px">
                            <template #body="slotProps">
                                <span
                                    :class="{
                                        'text-red-600 font-semibold': slotProps.data.averageMRI < -0.05,
                                        'text-green-600 font-semibold': Math.abs(slotProps.data.averageMRI) <= 0.05,
                                        'text-yellow-600 font-semibold': slotProps.data.averageMRI > 0.05,
                                    }"
                                >
                                    {{ formatMRI(slotProps.data.averageMRI) }}
                                </span>
                            </template>
                        </Column>
                        <Column field="mistakeCount" :header="t('labels.mistake_count')" style="width: 120px" />
                        <Column field="normalPI" :header="t('labels.normal_pi')" style="width: 120px">
                            <template #body="slotProps">
                                {{ slotProps.data.normalPI.toFixed(3) }}
                            </template>
                        </Column>

                        <!-- Expanded Row Template -->
                        <template #expansion="slotProps">
                            <div class="p-4 bg-gray-50">
                                <h4 class="font-semibold mb-3">
                                    {{ t('labels.mistake_details') }}
                                </h4>
                                <DataTable
                                    :value="slotProps.data.mistakeReactions"
                                    size="small"
                                >
                                    <Column field="mistakeLegNumber" :header="t('labels.leg')" style="width: 80px" />
                                    <Column field="mistakeSegmentLabel" :header="t('labels.mistake_segment')" />
                                    <Column field="mistakePI" :header="t('labels.mistake_pi')" style="width: 100px">
                                        <template #body="cellProps">
                                            {{ cellProps.data.mistakePI.toFixed(3) }}
                                        </template>
                                    </Column>
                                    <Column :header="t('labels.severity')" style="width: 120px">
                                        <template #body="cellProps">
                                            <Tag
                                                :value="t(`labels.${cellProps.data.mistakeSeverity}`)"
                                                :severity="getSeverityColor(cellProps.data.mistakeSeverity)"
                                            />
                                        </template>
                                    </Column>
                                    <Column field="reactionSegmentLabel" :header="t('labels.reaction_segment')" />
                                    <Column field="reactionPI" :header="t('labels.reaction_pi')" style="width: 100px">
                                        <template #body="cellProps">
                                            {{ cellProps.data.reactionPI.toFixed(3) }}
                                        </template>
                                    </Column>
                                    <Column field="mri" :header="t('labels.mri')" style="width: 100px">
                                        <template #body="cellProps">
                                            <span
                                                :class="{
                                                    'text-red-600 font-semibold': cellProps.data.mri < -0.05,
                                                    'text-green-600 font-semibold': Math.abs(cellProps.data.mri) <= 0.05,
                                                    'text-yellow-600 font-semibold': cellProps.data.mri > 0.05,
                                                }"
                                            >
                                                {{ formatMRI(cellProps.data.mri) }}
                                            </span>
                                        </template>
                                    </Column>
                                    <Column :header="t('labels.classification')" style="width: 140px">
                                        <template #body="cellProps">
                                            <Tag
                                                :value="t(`labels.${cellProps.data.classification}`)"
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
        <Card v-else-if="!mriQuery.isLoading.value && !mriQuery.data.value">
            <template #content>
                <div class="text-center p-8 text-gray-500">
                    <i class="pi pi-info-circle text-4xl mb-4" />
                    <p>{{ t('messages.no_mental_data') }}</p>
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
                <!-- MRI Explanation -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('labels.mri') }} (Mental Resilience Index)
                    </h3>
                    <p class="mb-2">
                        {{ t('messages.mri_explanation') }}
                    </p>
                    <div class="text-color bg-adaptive p-3 rounded">
                        <p class="font-semibold mb-1">
                            {{ t('labels.calculation') }}:
                        </p>
                        <p class="font-mono text-sm">
                            MRI = PI(n+1) - Normal PI
                        </p>
                        <p class="text-sm mt-2">
                            {{ t('messages.mri_calculation_detail') }}
                        </p>
                    </div>
                    <div class="mt-2">
                        <p class="font-semibold">
                            {{ t('labels.value_range') }}:
                        </p>
                        <ul class="list-disc ml-5 text-sm">
                            <li><strong>&lt; -0.05:</strong> {{ t('messages.mri_negative') }}</li>
                            <li><strong>-0.05 bis +0.05:</strong> {{ t('messages.mri_neutral') }}</li>
                            <li><strong>&gt; +0.05:</strong> {{ t('messages.mri_positive') }}</li>
                        </ul>
                    </div>
                </div>

                <!-- PI Explanation -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('labels.performance_index') }} (PI)
                    </h3>
                    <p class="mb-2">
                        {{ t('messages.pi_explanation') }}
                    </p>
                    <div class="text-color bg-adaptive p-3 rounded">
                        <p class="font-semibold mb-1">
                            {{ t('labels.calculation') }}:
                        </p>
                        <p class="font-mono text-sm">
                            PI = {{ t('messages.pi_reference_time_note').split('=')[0].trim() }}
                        </p>
                        <p class="text-sm mt-2">
                            {{ t('messages.pi_reference_time_note') }}
                        </p>
                    </div>
                    <div class="mt-2">
                        <p class="font-semibold">
                            {{ t('labels.value_range') }}:
                        </p>
                        <ul class="list-disc ml-5 text-sm">
                            <li><strong>&lt; 1.0</strong> {{ t('messages.pi_elite') }}</li>
                            <li><strong>1.0:</strong> {{ t('messages.pi_perfect') }}</li>
                            <li><strong>&gt; 1.0:</strong> {{ t('messages.pi_value_gt_1') }}</li>
                        </ul>
                    </div>
                </div>

                <!-- Mistake and Reaction PI Explanation -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('labels.mistake_reaction_pi') }}
                    </h3>
                    <div class="space-y-3">
                        <div>
                            <p class="font-semibold">
                                {{ t('labels.mistake_pi') }}:
                            </p>
                            <p class="text-sm">
                                {{ t('messages.mistake_pi_explanation') }}
                            </p>
                        </div>
                        <div>
                            <p class="font-semibold">
                                {{ t('labels.reaction_pi') }}:
                            </p>
                            <p class="text-sm">
                                {{ t('messages.reaction_pi_explanation') }}
                            </p>
                        </div>
                    </div>
                </div>

                <!-- Mistake Detection Criteria -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('messages.mistake_detection_title') }}
                    </h3>
                    <p class="mb-2">
                        {{ t('messages.mistake_detection_intro') }}
                    </p>
                    <div class="text-color bg-adaptive p-3 rounded space-y-2">
                        <div>
                            <p class="font-semibold">
                                1. {{ t('messages.mistake_detection_relative') }}:
                            </p>
                            <p class="text-sm">
                                {{ t('messages.mistake_detection_relative_detail') }}
                            </p>
                        </div>
                        <div>
                            <p class="font-semibold">
                                2. {{ t('messages.mistake_detection_absolute') }}:
                            </p>
                            <p class="text-sm">
                                {{ t('messages.mistake_detection_absolute_detail') }}
                            </p>
                        </div>
                    </div>
                    <p class="text-sm mt-2">
                        {{ t('messages.mistake_detection_explanation') }}
                    </p>
                </div>

                <!-- Mistake Severity Classifications -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('labels.mistake_severity') }}
                    </h3>
                    <p class="mb-2">
                        {{ t('messages.severity_explanation') }}
                    </p>
                    <div class="space-y-2">
                        <div class="flex items-start">
                            <Tag :value="t('labels.moderate')" severity="success" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.moderate') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.moderate_severity_explanation') }}
                                </p>
                            </div>
                        </div>
                        <div class="flex items-start">
                            <Tag :value="t('labels.major')" severity="warn" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.major') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.major_severity_explanation') }}
                                </p>
                            </div>
                        </div>
                        <div class="flex items-start">
                            <Tag :value="t('labels.severe')" severity="danger" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.severe') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.severe_severity_explanation') }}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Mental Classifications Explanation -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('labels.mental_classifications') }}
                    </h3>
                    <div class="space-y-2">
                        <div class="flex items-start">
                            <Tag :value="t('labels.panic')" severity="danger" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.panic') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.panic_explanation') }}
                                </p>
                            </div>
                        </div>
                        <div class="flex items-start">
                            <Tag :value="t('labels.ice_man')" severity="success" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.ice_man') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.ice_man_explanation') }}
                                </p>
                            </div>
                        </div>
                        <div class="flex items-start">
                            <Tag :value="t('labels.resigner')" severity="warn" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.resigner') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.resigner_explanation') }}
                                </p>
                            </div>
                        </div>
                        <div class="flex items-start">
                            <Tag :value="t('labels.chain_error')" severity="info" class="mr-2" />
                            <div>
                                <p class="font-semibold">
                                    {{ t('labels.chain_error') }}:
                                </p>
                                <p class="text-sm">
                                    {{ t('messages.chain_error_explanation') }}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Average MRI Explanation -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('labels.average_mri') }}
                    </h3>
                    <p class="mb-2">
                        {{ t('messages.average_mri_explanation') }}
                    </p>
                    <div class="text-color bg-adaptive p-3 rounded">
                        <p class="font-semibold mb-1">
                            {{ t('labels.calculation') }}:
                        </p>
                        <p class="font-mono text-sm">
                            {{ t('messages.average_mri_calculation') }}
                        </p>
                    </div>
                    <div class="mt-2">
                        <p class="font-semibold">
                            {{ t('messages.average_mri_interpretation') }}:
                        </p>
                        <ul class="list-disc ml-5 text-sm">
                            <li><strong>{{ t('messages.average_mri_negative') }}:</strong> {{ t('messages.average_mri_negative_detail') }}</li>
                            <li><strong>{{ t('messages.average_mri_neutral') }}:</strong> {{ t('messages.average_mri_neutral_detail') }}</li>
                            <li><strong>{{ t('messages.average_mri_positive') }}:</strong> {{ t('messages.average_mri_positive_detail') }}</li>
                        </ul>
                    </div>
                </div>
            </div>
        </Dialog>
    </div>
</template>

<style scoped>
.mental-resilience-analysis {
    max-width: 1400px;
    margin: 0 auto;
}
</style>
