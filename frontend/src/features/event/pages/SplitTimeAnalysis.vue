<script lang="ts" setup>
import type { PersonKey } from '@/features/person/model/person_key'
import { useQuery } from '@tanstack/vue-query'
import Accordion from 'primevue/accordion'
import AccordionContent from 'primevue/accordioncontent'
import AccordionHeader from 'primevue/accordionheader'
import AccordionPanel from 'primevue/accordionpanel'
import Button from 'primevue/button'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import MultiSelect from 'primevue/multiselect'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { EventService } from '@/features/event/services/event.service'

const props = defineProps<{
    id: string
    resultListId: string
}>()

const { t } = useI18n()
const router = useRouter()

const mergeBidirectional = ref(false)
const filterPersonIds = ref<number[]>([])
const filterIntersection = ref(false)
const visibleSegmentCount = ref(50) // Show first 50 segments initially
const SEGMENT_INCREMENT = 50

const personsQuery = useQuery({
    queryKey: ['personsForResultList', props.resultListId],
    queryFn: () => EventService.getPersonsForResultList(
        Number.parseInt(props.resultListId),
        t,
    ),
})

const splitTimeQueryRanking = useQuery({
    queryKey: ['splitTimeAnalysisRanking', props.resultListId, mergeBidirectional, filterPersonIds, filterIntersection],
    queryFn: () => EventService.getSplitTimeAnalysisRanking(
        Number.parseInt(props.resultListId),
        mergeBidirectional.value,
        filterPersonIds.value,
        filterIntersection.value,
        t,
    ),
})

// Create a map for fast person lookup by ID
const personMap = computed(() => {
    if (!personsQuery.data.value)
        return new Map<number, PersonKey>()
    return new Map(personsQuery.data.value.map(p => [p.id, p]))
})

// Helper function to get person name by ID
function getPersonName(personId: number): string {
    const person = personMap.value.get(personId)
    return person ? `${person.familyName}, ${person.givenName}` : 'Unknown'
}

// Calculate overall ranking when intersection filter is active
const overallRanking = computed(() => {
    if (!filterIntersection.value || filterPersonIds.value.length < 2 || !splitTimeQueryRanking.data.value || splitTimeQueryRanking.data.value.length === 0) {
        return []
    }

    const analysis = splitTimeQueryRanking.data.value[0]
    if (!analysis || !analysis.controlSegments || analysis.controlSegments.length === 0) {
        return []
    }

    // Calculate total time for each filtered person across all segments
    const personTotals = new Map<number, { totalSeconds: number, segmentCount: number }>()

    // Initialize with filtered persons
    filterPersonIds.value.forEach((personId) => {
        personTotals.set(personId, { totalSeconds: 0, segmentCount: 0 })
    })

    // Sum up split times across all segments
    analysis.controlSegments.forEach((segment) => {
        segment.runnerSplits.forEach((split) => {
            if (personTotals.has(split.personId)) {
                const current = personTotals.get(split.personId)!
                personTotals.set(split.personId, {
                    totalSeconds: current.totalSeconds + split.splitTimeSeconds,
                    segmentCount: current.segmentCount + 1,
                })
            }
        })
    })

    // Convert to array and sort by total time
    const ranking = Array.from(personTotals.entries())
        .map(([personId, data]) => ({
            personId,
            personName: getPersonName(personId),
            totalSeconds: data.totalSeconds,
            segmentCount: data.segmentCount,
            totalTime: formatSeconds(data.totalSeconds),
        }))
        .sort((a, b) => a.totalSeconds - b.totalSeconds)

    // Add position and time behind leader
    const leaderTime = ranking.length > 0 ? ranking[0].totalSeconds : 0
    return ranking.map((entry, index) => ({
        ...entry,
        position: index + 1,
        timeBehind: entry.totalSeconds === leaderTime ? '' : `+${formatSeconds(entry.totalSeconds - leaderTime)}`,
    }))
})

// Helper function to format seconds to MM:SS
function formatSeconds(seconds: number): string {
    const minutes = Math.floor(seconds / 60)
    const remainingSeconds = Math.floor(seconds % 60)
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

const visibleSegments = computed(() => {
    if (!splitTimeQueryRanking.data.value || splitTimeQueryRanking.data.value.length === 0)
        return []
    return splitTimeQueryRanking.data.value[0].controlSegments.slice(0, visibleSegmentCount.value)
})

const hasMoreSegments = computed(() => {
    if (!splitTimeQueryRanking.data.value || splitTimeQueryRanking.data.value.length === 0)
        return false
    return splitTimeQueryRanking.data.value[0].controlSegments.length > visibleSegmentCount.value
})

function loadMoreSegments() {
    visibleSegmentCount.value += SEGMENT_INCREMENT
}

function removePersonFilter(personId: number) {
    filterPersonIds.value = filterPersonIds.value.filter(id => id !== personId)
}

function navigateBack() {
    router.back()
}

// Reset visible count when data changes
watch([mergeBidirectional, filterPersonIds, filterIntersection], () => {
    visibleSegmentCount.value = SEGMENT_INCREMENT
})
</script>

<template>
    <div class="split-time-analysis">
        <Button
            v-tooltip="t('labels.back')"
            icon="pi pi-arrow-left"
            :label="t('labels.back')"
            class="mb-3"
            :aria-label="t('labels.back')"
            severity="secondary"
            outlined
            raised
            rounded
            @click="navigateBack"
        />

        <h1 class="mt-3 font-extrabold">
            {{ t('labels.split_time_analysis') }}
        </h1>

        <div class="controls mb-3 card">
            <div class="field-checkbox mb-2">
                <Checkbox
                    v-model="mergeBidirectional"
                    input-id="merge"
                    :binary="true"
                />
                <label for="merge" class="ml-2">{{ t('labels.merge_bidirectional') }}</label>
            </div>

            <div class="filter-section mt-3">
                <label for="personFilter" class="mb-2 block">{{ t('labels.filter_by_name') }}</label>
                <span v-if="personsQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
                <MultiSelect
                    v-else-if="personsQuery.data?.value"
                    id="personFilter"
                    v-model="filterPersonIds"
                    :options="personsQuery.data.value"
                    data-key="id"
                    filter
                    option-value="id"
                    :placeholder="t('messages.select')"
                    class="w-full"
                    display="chip"
                >
                    <template #option="slotProps">
                        <div>{{ slotProps.option.familyName }}, {{ slotProps.option.givenName }}</div>
                    </template>
                    <template #chip="slotProps">
                        <div class="inline-flex items-center gap-2 px-3 py-1 bg-primary text-primary-contrast rounded-full">
                            <span>{{ personsQuery.data.value?.find((p: PersonKey) => p.id === slotProps.value)?.familyName }}, {{ personsQuery.data.value?.find((p: PersonKey) => p.id === slotProps.value)?.givenName }}</span>
                            <span class="pi pi-times cursor-pointer hover:bg-primary-emphasis rounded-full p-1" @click.stop="removePersonFilter(slotProps.value)" />
                        </div>
                    </template>
                </MultiSelect>
            </div>

            <div v-if="filterPersonIds.length > 1" class="field-checkbox mt-3">
                <Checkbox
                    v-model="filterIntersection"
                    input-id="intersection"
                    :binary="true"
                />
                <label for="intersection" class="ml-2">{{ t('labels.filter_intersection') }}</label>
            </div>
        </div>

        <div v-if="splitTimeQueryRanking.isLoading.value" class="card">
            {{ t('messages.loading') }}
        </div>

        <div v-else-if="splitTimeQueryRanking.data.value" class="card">
            <!-- Overall ranking when intersection filter is active -->
            <div v-if="overallRanking.length > 0" class="mb-4">
                <h2 class="font-bold text-xl mb-3 text-primary">
                    {{ t('labels.overall_ranking') }}
                </h2>
                <p class="text-sm text-gray-600 mb-3">
                    {{ t('messages.overall_ranking_description', { count: overallRanking[0]?.segmentCount || 0 }) }}
                </p>
                <DataTable
                    :value="overallRanking"
                    striped-rows
                    size="small"
                    class="mb-4"
                >
                    <Column field="position" :header="t('labels.position')" style="width: 10%" />
                    <Column field="personName" :header="t('labels.name')" style="width: 40%" />
                    <Column field="totalTime" :header="t('labels.total_time')" style="width: 25%" />
                    <Column field="timeBehind" :header="t('labels.time_behind')" style="width: 25%" />
                </DataTable>
            </div>

            <div v-if="splitTimeQueryRanking.data.value.length > 0">
                <div
                    v-for="analysis in splitTimeQueryRanking.data.value"
                    :key="analysis.classResultShortName"
                    class="class-section mb-4"
                >
                    <h2 class="class-title font-bold mb-3">
                        {{ analysis.classResultShortName }}
                        <span class="text-sm text-gray-500 ml-2">
                            ({{ visibleSegments.length }} / {{ analysis.controlSegments.length }} {{ t('labels.segments') }})
                        </span>
                    </h2>
                    <Accordion v-if="visibleSegments.length > 0" :multiple="true" lazy>
                        <AccordionPanel
                            v-for="segment in visibleSegments"
                            :key="segment.segmentLabel"
                            :value="segment.segmentLabel"
                        >
                            <AccordionHeader>
                                <div class="flex w-full items-center">
                                    <span class="font-semibold text-left w-48">{{ segment.segmentLabel }}</span>
                                    <span class="text-gray-600 text-left w-40">
                                        ({{ segment.runnerSplits.length }} {{ t('labels.runners') }})
                                    </span>
                                    <span class="text-sm text-gray-500 italic text-left flex-1">
                                        {{ segment.classes.length > 0 ? segment.classes.join(', ') : '' }}
                                    </span>
                                </div>
                            </AccordionHeader>
                            <AccordionContent>
                                <DataTable
                                    :value="segment.runnerSplits"
                                    striped-rows
                                    :rows="50"
                                    :paginator="segment.runnerSplits.length > 50"
                                    :lazy="false"
                                    :scrollable="false"
                                    size="small"
                                >
                                    <Column field="position" :header="t('labels.position')" style="width: 8%" />
                                    <Column header="" style="width: 4%">
                                        <template #body="slotProps">
                                            <i v-if="segment.bidirectional" :class="slotProps.data.reversed ? 'pi pi-arrow-left' : 'pi pi-arrow-right'" />
                                        </template>
                                    </Column>
                                    <Column :header="t('labels.name')" style="width: 31%">
                                        <template #body="slotProps">
                                            {{ getPersonName(slotProps.data.personId) }}
                                        </template>
                                    </Column>
                                    <Column field="classResultShortName" :header="t('labels.class')" style="width: 12%" />
                                    <Column field="splitTime" :header="t('labels.split_time')" style="width: 20%" />
                                    <Column field="timeBehind" :header="t('labels.time_behind')" style="width: 25%" />
                                </DataTable>
                                <div v-if="segment.runnerSplits.length === 100" class="p-2 text-sm text-gray-600 italic">
                                    {{ t('messages.top_100_shown') }}
                                </div>
                            </AccordionContent>
                        </AccordionPanel>
                    </Accordion>
                    <div v-if="hasMoreSegments" class="mt-4 text-center">
                        <Button
                            :label="`${t('labels.load_more')} (${analysis.controlSegments.length - visibleSegmentCount} ${t('labels.remaining')})`"
                            icon="pi pi-angle-down"
                            severity="secondary"
                            outlined
                            @click="loadMoreSegments"
                        />
                    </div>
                    <div v-else-if="visibleSegments.length === 0" class="p-4">
                        {{ t('messages.no_split_times') }}
                    </div>
                </div>
            </div>
            <div v-else class="p-4">
                {{ t('messages.no_split_times') }}
            </div>
        </div>

        <div v-else class="card">
            {{ t('messages.no_data') }}
        </div>
    </div>
</template>

<style scoped>
.split-time-analysis {
    padding: 1rem;
}

.controls {
    padding: 1rem;
}

.filter-section {
    display: flex;
    flex-direction: column;
}

.class-section {
    border-top: 1px solid #e0e0e0;
    padding-top: 1rem;
}

.class-section:first-child {
    border-top: none;
    padding-top: 0;
}

.class-title {
    font-size: 1.25rem;
    color: var(--p-primary-color);
}
</style>
