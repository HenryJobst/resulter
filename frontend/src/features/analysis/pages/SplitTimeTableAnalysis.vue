<script setup lang="ts">
import type { ErrorSeverity } from '@/features/analysis/model/split_time_table'
import { useQuery } from '@tanstack/vue-query'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'
import Select from 'primevue/select'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { EventService } from '@/features/event/services/event.service'

// Props
const props = defineProps<{
    resultListId?: number
    eventName?: string
    resultListLabel?: string
}>()

// I18n
const { t } = useI18n()
const router = useRouter()

// State
const groupBy = ref<string>('class')
const selectedGroupId = ref<string | null>(null)
const showHelpDialog = ref(false)

// Options query
const optionsQuery = useQuery({
    queryKey: ['splitTableOptions', props.resultListId],
    queryFn: () => {
        if (!props.resultListId)
            return Promise.resolve(null)
        return EventService.getSplitTableOptions(props.resultListId, t)
    },
    enabled: computed(() => !!props.resultListId),
    staleTime: 1000 * 60 * 5, // 5 minutes
})

// Table query
const tableQuery = useQuery({
    queryKey: ['splitTable', props.resultListId, groupBy, selectedGroupId],
    queryFn: () => {
        if (!props.resultListId || !selectedGroupId.value)
            return Promise.resolve(null)

        return EventService.getSplitTimeTable(
            props.resultListId,
            groupBy.value,
            selectedGroupId.value,
            t,
        )
    },
    enabled: computed(() => !!props.resultListId && !!selectedGroupId.value),
    staleTime: 1000 * 60 * 5, // 5 minutes
})

// Available group options based on groupBy
const availableGroups = computed(() => {
    if (!optionsQuery.data.value)
        return []

    if (groupBy.value === 'class') {
        return optionsQuery.data.value.classes.map(c => ({
            label: `${c.className} (${c.runnerCount})`,
            value: c.className,
        }))
    }
    else {
        return optionsQuery.data.value.courses.map(c => ({
            label: `${c.courseName} - ${c.classNames.join(', ')} (${c.runnerCount})`,
            value: c.courseId.toString(),
        }))
    }
})

// Sorted table rows by finish time
const sortedRows = computed(() => {
    if (!tableQuery.data.value?.rows)
        return []

    const rows = [...tableQuery.data.value.rows]

    // Separate competing and AK (not competing) runners
    const competingRunners = rows.filter(r => !r.notCompeting)
    const akRunners = rows.filter(r => r.notCompeting)

    // Sort both groups by finish time
    const sortByFinishTime = (a: any, b: any) => {
        const timeA = a.finishTime ?? Number.MAX_VALUE
        const timeB = b.finishTime ?? Number.MAX_VALUE
        return timeA - timeB
    }

    competingRunners.sort(sortByFinishTime)
    akRunners.sort(sortByFinishTime)

    // Competing runners first, then AK runners
    return [...competingRunners, ...akRunners]
})

// Control codes without "S" (start)
const displayControlCodes = computed(() => {
    if (!tableQuery.data.value?.controlCodes)
        return []

    return tableQuery.data.value.controlCodes.filter(code => code !== 'S')
})

// Auto-select first option when groups change
watch(availableGroups, (groups) => {
    if (groups.length > 0 && !selectedGroupId.value)
        selectedGroupId.value = groups[0].value
}, { immediate: true })

// Reset selection when groupBy changes
watch(groupBy, () => {
    selectedGroupId.value = null
})

// Debug: Watch table query state
watch(() => tableQuery.data.value, (newData) => {
    console.log('Table query data changed:', {
        hasData: !!newData,
        isLoading: tableQuery.isLoading.value,
        isFetching: tableQuery.isFetching.value,
        isSuccess: tableQuery.isSuccess.value,
        isError: tableQuery.isError.value,
        rowCount: newData?.rows?.length || 0,
        controlCount: newData?.controlCodes?.length || 0,
        samplePersonNames: newData?.rows?.slice(0, 3).map(r => r.personName) || [],
    })
}, { immediate: true })

// Helper functions
function formatTime(seconds: number | null): string {
    if (seconds === null)
        return '—'

    const totalSeconds = Math.floor(seconds)
    const hours = Math.floor(totalSeconds / 3600)
    const minutes = Math.floor((totalSeconds % 3600) / 60)
    const secs = totalSeconds % 60

    if (hours > 0)
        return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`

    return `${minutes}:${secs.toString().padStart(2, '0')}`
}

function formatTimeDiff(seconds: number | null): string {
    if (seconds === null || seconds === 0)
        return '—'

    const totalSeconds = Math.floor(seconds)
    const minutes = Math.floor(totalSeconds / 60)
    const secs = totalSeconds % 60

    return `+${minutes}:${secs.toString().padStart(2, '0')}`
}

function getErrorColor(severity: ErrorSeverity): string {
    switch (severity) {
        case 'SEVERE': return 'bg-red-200'
        case 'HIGH': return 'bg-red-100'
        case 'MEDIUM': return 'bg-orange-100'
        case 'LOW': return 'bg-orange-50'
        default: return ''
    }
}

function getCellBackgroundClass(cell: any): string {
    if (cell.isError)
        return getErrorColor(cell.errorSeverity)

    return ''
}

function getTimeClass(isBest: boolean): string {
    return isBest ? 'font-bold' : ''
}

function getColumnHeader(controlCode: string, index: number): string {
    if (controlCode === 'F')
        return t('analysis.splitTimeTable.finish')

    return `${index + 1} (${controlCode})`
}

const isMobile = ref(false)

function checkScreenSize() {
    isMobile.value = window.innerWidth < 768
}

onMounted(() => {
    checkScreenSize()
    window.addEventListener('resize', checkScreenSize)
})

onUnmounted(() => {
    window.removeEventListener('resize', checkScreenSize)
})
</script>

<template>
    <div class="split-time-table-analysis">
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
                        <h2 class="text-2xl font-bold">
                            {{ t('analysis.splitTimeTable.title') }}
                        </h2>
                        <p
                            v-if="props.eventName || props.resultListLabel"
                            class="text-gray-600"
                        >
                            {{ props.eventName }} - {{ props.resultListLabel }}
                        </p>
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

        <!-- Selection Card -->
        <Card class="mb-4">
            <template #content>
                <div class="grid grid-cols-2 gap-4">
                    <!-- Group By Select -->
                    <div class="field">
                        <label
                            for="groupBy"
                            class="block font-semibold mb-2"
                        >
                            {{ t('analysis.splitTimeTable.groupBy') }}
                        </label>
                        <Select
                            id="groupBy"
                            v-model="groupBy"
                            :options="[
                                { label: t('analysis.splitTimeTable.byClass'), value: 'class' },
                                { label: t('analysis.splitTimeTable.byCourse'), value: 'course' },
                            ]"
                            option-label="label"
                            option-value="value"
                            class="w-full"
                        />
                    </div>

                    <!-- Select Group -->
                    <div class="field">
                        <label
                            for="selectGroup"
                            class="block font-semibold mb-2"
                        >
                            {{
                                groupBy === 'class'
                                    ? t('analysis.splitTimeTable.selectClass')
                                    : t('analysis.splitTimeTable.selectCourse')
                            }}
                        </label>
                        <Select
                            id="selectGroup"
                            v-model="selectedGroupId"
                            :options="availableGroups"
                            option-label="label"
                            option-value="value"
                            class="w-full"
                            :loading="optionsQuery.isLoading.value"
                            :disabled="optionsQuery.isLoading.value || availableGroups.length === 0"
                        />
                    </div>
                </div>
            </template>
        </Card>

        <!-- Loading State -->
        <div
            v-if="tableQuery.isLoading.value || tableQuery.isFetching.value"
            class="text-center p-4"
        >
            <i class="pi pi-spin pi-spinner text-4xl" />
        </div>

        <!-- Error State -->
        <Message
            v-else-if="tableQuery.isError.value"
            severity="error"
            class="mb-4"
        >
            {{ tableQuery.error?.message || 'Fehler beim Laden der Daten' }}
        </Message>

        <!-- Data loaded -->
        <div v-else-if="tableQuery.isSuccess.value && tableQuery.data.value">
            <!-- Warning Message for Unreliable Data -->
            <Message
                v-if="tableQuery.data.value.metadata?.reliableData === false"
                severity="warn"
                class="mb-4"
            >
                {{ t('analysis.splitTimeTable.unreliableDataWarning') }}
            </Message>

            <!-- Data Table -->
            <DataTable
                :value="sortedRows"
                scrollable
                scroll-height="600px"
                striped-rows
                class="split-time-table"
            >
                <!-- Frozen Column: Position -->
                <Column
                    :header="t('analysis.splitTimeTable.position')"
                    :frozen="!isMobile"
                    style="min-width: 60px; text-align: center"
                >
                    <template #body="slotProps">
                        <div class="font-semibold">
                            {{ slotProps.data.position ?? '—' }}
                        </div>
                    </template>
                </Column>

                <!-- Frozen Column: Runner Name -->
                <Column
                    :header="t('analysis.splitTimeTable.runnerName')"
                    frozen
                    class="runner-name-column"
                    style="min-width: 200px"
                >
                    <template #body="slotProps">
                        {{ slotProps.data.personName }}
                        <span v-if="slotProps.data.notCompeting" class="text-gray-500 ml-2">
                            ({{ t('analysis.splitTimeTable.notCompetingAbbr') }})
                        </span>
                    </template>
                </Column>

                <!-- Frozen Column: Class (if grouped by course) -->
                <Column
                    v-if="groupBy === 'course'"
                    field="className"
                    :header="t('analysis.splitTimeTable.class')"
                    :frozen="!isMobile"
                    style="min-width: 100px"
                />

                <!-- Frozen Column: Total Time (Gesamtzeit) -->
                <Column
                    :header="t('analysis.splitTimeTable.totalTime')"
                    :frozen="!isMobile"
                    style="min-width: 80px"
                >
                    <template #body="slotProps">
                        <div class="font-semibold">
                            {{ formatTime(slotProps.data.finishTime) }}
                        </div>
                    </template>
                </Column>

                <!-- Frozen Column: Time Behind Winner (Rückstand) -->
                <Column
                    :header="t('analysis.splitTimeTable.timeBehind')"
                    :frozen="!isMobile"
                    style="min-width: 80px"
                >
                    <template #body="slotProps">
                        <div v-if="!slotProps.data.notCompeting && slotProps.data.finishTime && tableQuery.data.value?.metadata?.winnerTime">
                            {{ formatTimeDiff(slotProps.data.finishTime - tableQuery.data.value.metadata.winnerTime) }}
                        </div>
                        <div v-else-if="slotProps.data.notCompeting">
                            —
                        </div>
                    </template>
                </Column>

                <!-- Dynamic Columns for each control -->
                <Column
                    v-for="(controlCode, colIndex) in displayControlCodes"
                    :key="`col-${colIndex}`"
                    :header="getColumnHeader(controlCode, colIndex)"
                    style="min-width: 115px; padding: 0.25rem;"
                >
                    <template #body="slotProps">
                        <div
                            v-if="slotProps.data.cells && slotProps.data.cells[colIndex + 1]"
                            class="split-cell"
                            :class="[getCellBackgroundClass(slotProps.data.cells[colIndex + 1])]"
                        >
                            <div
                                class="cumulative-line"
                                :class="[getTimeClass(slotProps.data.cells[colIndex + 1].isBestCumulative)]"
                            >
                                {{ formatTime(slotProps.data.cells[colIndex + 1].cumulativeTime) }}
                                <span
                                    v-if="slotProps.data.cells[colIndex + 1].cumulativePosition"
                                    class="position"
                                    :class="[getTimeClass(slotProps.data.cells[colIndex + 1].isBestCumulative)]"
                                >
                                    ({{ slotProps.data.cells[colIndex + 1].cumulativePosition }})
                                </span>
                            </div>
                            <div
                                class="segment-line"
                                :class="[getTimeClass(slotProps.data.cells[colIndex + 1].isBestSegment)]"
                            >
                                {{ formatTime(slotProps.data.cells[colIndex + 1].segmentTime) }}
                                <span
                                    v-if="slotProps.data.cells[colIndex + 1].segmentPosition"
                                    class="position"
                                    :class="[getTimeClass(slotProps.data.cells[colIndex + 1].isBestSegment)]"
                                >
                                    ({{ slotProps.data.cells[colIndex + 1].segmentPosition }})
                                </span>
                            </div>
                        </div>
                    </template>
                </Column>
            </DataTable>
        </div>

        <!-- No Data Message -->
        <Message
            v-else
            severity="info"
        >
            {{ t('analysis.splitTimeTable.noData') }}
        </Message>

        <!-- Help Dialog -->
        <Dialog
            v-model:visible="showHelpDialog"
            :header="t('labels.help')"
            :style="{ width: '50rem' }"
            :breakpoints="{ '1199px': '75vw', '575px': '90vw' }"
            modal
        >
            <div class="space-y-4">
                <!-- Table Structure -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('analysis.splitTimeTable.help.tableStructure') }}
                    </h3>
                    <p class="mb-2">
                        {{ t('analysis.splitTimeTable.help.tableStructureText') }}
                    </p>
                    <ul class="list-disc ml-6 space-y-1">
                        <li>
                            <strong>{{ t('analysis.splitTimeTable.help.cumulativeTime') }}:</strong>
                            {{ t('analysis.splitTimeTable.help.cumulativeTimeDesc') }}
                        </li>
                        <li>
                            <strong>{{ t('analysis.splitTimeTable.help.segmentTime') }}:</strong>
                            {{ t('analysis.splitTimeTable.help.segmentTimeDesc') }}
                        </li>
                    </ul>
                </div>

                <!-- Best Times -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('analysis.splitTimeTable.help.bestTimes') }}
                    </h3>
                    <p>
                        {{ t('analysis.splitTimeTable.help.bestTimesText') }}
                    </p>
                </div>

                <!-- Not Competing Runners -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('analysis.splitTimeTable.help.notCompetingRunners') }}
                    </h3>
                    <p>
                        {{ t('analysis.splitTimeTable.help.notCompetingRunnersText') }}
                    </p>
                </div>

                <!-- Error Highlighting -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('analysis.splitTimeTable.help.errorHighlighting') }}
                    </h3>
                    <p class="mb-2">
                        {{ t('analysis.splitTimeTable.help.errorHighlightingText') }}
                    </p>
                    <div class="space-y-2">
                        <div class="flex items-center gap-2">
                            <div class="w-20 h-8 bg-orange-50 border border-gray-300 rounded" />
                            <span>{{ t('analysis.splitTimeTable.help.severityLow') }}</span>
                        </div>
                        <div class="flex items-center gap-2">
                            <div class="w-20 h-8 bg-orange-100 border border-gray-300 rounded" />
                            <span>{{ t('analysis.splitTimeTable.help.severityMedium') }}</span>
                        </div>
                        <div class="flex items-center gap-2">
                            <div class="w-20 h-8 bg-red-100 border border-gray-300 rounded" />
                            <span>{{ t('analysis.splitTimeTable.help.severityHigh') }}</span>
                        </div>
                        <div class="flex items-center gap-2">
                            <div class="w-20 h-8 bg-red-200 border border-gray-300 rounded" />
                            <span>{{ t('analysis.splitTimeTable.help.severitySevere') }}</span>
                        </div>
                    </div>
                </div>

                <!-- Error Calculation -->
                <div>
                    <h3 class="text-xl font-semibold mb-2">
                        {{ t('analysis.splitTimeTable.help.errorCalculation') }}
                    </h3>
                    <p>
                        {{ t('analysis.splitTimeTable.help.errorCalculationText') }}
                    </p>
                </div>
            </div>
        </Dialog>
    </div>
</template>

<style scoped>
.split-time-table {
    font-size: 0.75rem;
}

/* Fix frozen columns z-index and background */
/* noinspection CssUnusedSymbol, CssUnresolvedCustomProperty */
.split-time-table :deep(.p-datatable-frozen-column) {
    z-index: 2 !important;
    position: sticky !important;
    border-right: 1px solid var(--p-datatable-border-color, #e5e7eb) !important;
}

/* Remove border from the last frozen column to avoid double border */
/* noinspection CssUnusedSymbol */
.split-time-table :deep(.p-datatable-frozen-column:last-of-type) {
    border-right: none !important;
}

/* Light Mode - Exact colors to match non-frozen columns */
/* Even rows (default) */
/* noinspection CssUnusedSymbol */
.split-time-table :deep(.p-datatable-tbody > tr:not(:hover) .p-datatable-frozen-column) {
    background: #fff7ed !important;
}

/* Odd rows (striped) - overrides even rows */
/* noinspection CssUnusedSymbol */
.split-time-table :deep(.p-datatable-tbody > tr:nth-child(odd):not(:hover) .p-datatable-frozen-column) {
    background: #ffffff !important;
}

/* Hover - visible light gray that differs from both row colors - applies to ALL cells */
.split-time-table :deep(.p-datatable-tbody > tr:hover td) {
    background: #f3f4f6 !important;
}

/* Dark Mode - Exact colors to match non-frozen columns */
@media (prefers-color-scheme: dark) {
    /* noinspection CssUnusedSymbol */
    .split-time-table :deep(.p-datatable-tbody > tr:not(:hover) .p-datatable-frozen-column) {
        background: #020617 !important;
    }

    /* noinspection CssUnusedSymbol */
    .split-time-table :deep(.p-datatable-tbody > tr:nth-child(odd):not(:hover) .p-datatable-frozen-column) {
        background: #0f172a !important;
    }

    /* Hover - applies to ALL cells in dark mode */
    .split-time-table :deep(.p-datatable-tbody > tr:hover td) {
        background: #1f2937 !important;
    }
}

.runner-name-column {
    font-weight: 500;
}

/* Runner name column - no special hover needed, uses global hover rule */

.split-cell {
    padding: 0.15rem 0.3rem;
    min-height: 2.5rem;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.cumulative-line,
.segment-line {
    line-height: 1.3;
}

.cumulative-line {
    color: #111827;
}

.segment-line {
    color: #1f2937;
    font-size: 1em;
}

.position {
    margin-left: 0.2rem;
    color: #374151;
    font-size: 1em;
}

.font-bold {
    font-weight: 700;
}

/* Background colors for errors - Light Mode */
.bg-red-200 {
    background-color: #fecaca;
}

.bg-red-100 {
    background-color: #fee2e2;
}

.bg-orange-100 {
    background-color: #fed7aa;
}

.bg-orange-50 {
    background-color: #fff7ed;
}

/* Dark Mode - better contrast */
@media (prefers-color-scheme: dark) {
    .bg-red-200 {
        background-color: #7f1d1d !important; /* dark red-900 */
    }

    .bg-red-100 {
        background-color: #991b1b !important; /* dark red-800 */
    }

    .bg-orange-100 {
        background-color: #c2410c !important; /* dark orange-700 */
    }

    .bg-orange-50 {
        background-color: #ea580c !important; /* dark orange-600 */
    }

    .cumulative-line {
        color: #f9fafb !important; /* lighter for dark mode */
    }

    .segment-line {
        color: #d1d5db !important; /* lighter gray for dark mode */
    }

    .position {
        color: #9ca3af !important; /* lighter gray for positions */
    }

    /* Make bold times more visible in dark mode */
    .font-bold {
        font-weight: 900 !important; /* extra-bold */
        color: #fbbf24 !important; /* amber-400 for better visibility */
    }
}
</style>
