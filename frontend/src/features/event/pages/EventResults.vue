<script lang="ts" setup>
import type { TreeNode } from 'primevue/treenode'
import type { Ref } from 'vue'
import type { Locale } from 'vue-i18n'
import type { ClassResult } from '@/features/event/model/class_result'
import type { CupScoreList } from '@/features/event/model/cup_score_list'
import type { ResultList } from '@/features/event/model/result_list'
import type { ResultListIdPersonResults } from '@/features/event/model/result_list_id_person_results'
import { useQueries, useQuery, useQueryClient } from '@tanstack/vue-query'
import moment from 'moment/min/moment-with-locales'
import Button from 'primevue/button'
import Panel from 'primevue/panel'
import Tree from 'primevue/tree'
import { computed, nextTick, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/features/auth/store/auth.store'
import { courseService } from '@/features/course/services/course.service'
import { EventService, eventService } from '@/features/event/services/event.service'
import EventCertificateStatsTable from '@/features/event/widgets/EventCertificateStatsTable.vue'
import EventResultTable from '@/features/event/widgets/EventResultTable.vue'
import { raceService } from '@/features/race/services/race.service'

const props = defineProps<{ id: string }>()

const { t, locale } = useI18n()

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

// State für Tree-Expansion
const expandedKeys = ref<Record<string, boolean>>({})

// Query-Parameter für Deep-Linking
const deepLinkParams = computed(() => ({
    resultListId: route.query.resultListId ? Number(route.query.resultListId) : undefined,
    classShortName: route.query.classShortName as string | undefined,
    personId: route.query.personId ? Number(route.query.personId) : undefined,
}))

const eventResultsQuery = useQuery({
    queryKey: ['eventResults', props.id],
    queryFn: () => EventService.getResultsById(props.id, t),
})

const eventId = computed(() => {
    const resultLists = eventResultsQuery.data.value?.resultLists
    return Array.isArray(resultLists) && resultLists.length > 0 ? resultLists[0]!.eventId : undefined
})

const enabled = computed(() => {
    const resultLists = eventResultsQuery.data.value?.resultLists
    return !!(resultLists && resultLists[0]?.eventId)
})

const eventQuery = useQuery({
    queryKey: ['events', eventId.value],
    queryFn: () => {
        return eventService.getById(eventId.value!, t)
    },
    enabled,
})

const event = computed(() => {
    return eventQuery.data.value
})

const eventCertificateStatsQuery = useQuery({
    queryKey: ['eventCertificateStats', eventId.value, authStore.isAdmin],
    queryFn: () => EventService.getCertificateStats(eventId.value, t),
    enabled: authStore.isAdmin && !!eventId.value,
    retry: false,
})

const courseQuery = useQuery({
    queryKey: ['courses'],
    queryFn: () => courseService.getAllUnpaged(t),
})

const raceQuery = useQuery({
    queryKey: ['races'],
    queryFn: () => raceService.getAllUnpaged(t),
})

const cupPointsQueries = useQueries({
    queries: computed(() => {
        return (
            eventResultsQuery.data.value?.resultLists?.map(resultList => ({
                queryKey: ['cupScoreLists', resultList.id],
                queryFn: () => EventService.getCupScores(resultList.id, t),
                enabled: !!resultList, // Nur aktivieren, wenn `resultList` vorhanden ist
            })) || []
        )
    }),
})

const cupPointsData = computed(() => cupPointsQueries.value.map(query => query.data))
const cupPointsLoading = computed(() => cupPointsQueries.value.some(query => query.isLoading))

const queryClient = useQueryClient()

function invalidateCupPointsQuery(resultListId: number) {
    queryClient.invalidateQueries({ queryKey: ['cupScoreLists', resultListId] })
}

function formatCreateTime(date: Date | string, locale: Ref<Locale>) {
    return computed(() => {
        moment.locale(locale.value)
        return moment(date).format('L')
    })
}

function getResultListLabel(resultList: ResultList) {
    if (!raceQuery.data?.value || !Array.isArray(raceQuery.data.value)) {
        return ''
    }

    const names = raceQuery.data?.value
        .filter(r => r.id === resultList.raceId)
        .map(r => r.name)
    let name = raceQuery.data?.value.find(r => r.id === resultList.raceId)?.name
    const manyRacesExists = names?.length ?? 0 > 1
    if (!name || manyRacesExists) {
        const raceNumber = resultList.classResults
            .flatMap(c => c.personResults)
            .flatMap(pr => pr.raceNumber)
            .reduce(a => a)
            .toString()
        if (raceNumber !== '0') {
            const nameAddition = t('labels.race_number', {
                raceNumber,
            })
            if (name) {
                name = `${name} - ${nameAddition}`
            }
            else {
                name = nameAddition
            }
        }
        else {
            name = t('labels.overall')
        }
    }
    return `${(name ? `${name}, ` : '') + t('labels.created')} ${formatCreateTime(resultList.createTime, locale).value}`
}

function createResultListTreeNodes(
    resultLists: ResultList[] | undefined,
    cupScoreLists: (CupScoreList[] | null | undefined)[],
): TreeNode[] {
    if (!resultLists)
        return []

    const treeNodes: TreeNode[] = []
    for (let i = 0; i < resultLists.length; i++) {
        const resultList = resultLists[i]
        if (!resultList)
            continue
        const certificateEnabled = resultList.isCertificateAvailable
        const cupScoreEnabled = resultList.isCupScoreAvailable
        const resultListCupScoreLists = cupScoreLists ? cupScoreLists[i] : undefined
        const resultListCompleteCupScoreLists = resultListCupScoreLists
            ? resultListCupScoreLists.filter(x => x.status === 'COMPLETE')
            : undefined

        // Extract race number for analysis button visibility
        const raceNumber = resultList.classResults
            .flatMap(c => c.personResults)
            .flatMap(pr => pr.raceNumber)
            .reduce(a => a)

        treeNodes.push({
            key: resultList.id.toString(),
            label: getResultListLabel(resultList),
            data: {
                cupScoreEnabled,
                raceNumber,
            },
            children: [
                {
                    key: `${resultList.id.toString()}-table`,
                    data: createClassResultTreeNodes(
                        resultList.id,
                        resultList.classResults,
                        certificateEnabled,
                        cupScoreEnabled,
                        resultListCompleteCupScoreLists,
                    ),
                    type: 'tree',
                    leaf: false,
                },
            ],
        })
    }
    return treeNodes
}

function getClassResultLabel(a: ClassResult) {
    let courseData = ''
    if (a.courseId != null) {
        const controls = courseControlsColumn(a)
        courseData = ` - ${courseLengthColumn(a)} ${t('labels.length_abbreviation')} - ${courseClimbColumn(a)} ${t('labels.climb_abbreviation')}`
        if (controls !== '') {
            courseData += ` - ${controls} ${t('labels.control_abbreviation')}`
        }
    }
    return `${a.name} (${a.personResults.length})${courseData}`
}

function getPersonResults(
    resultListId: number,
    classResult: ClassResult,
    certificateEnabled: boolean | undefined,
    cupScoreEnabled: boolean | undefined,
    cupScoreLists: CupScoreList[] | undefined,
): ResultListIdPersonResults {
    return {
        resultListId,
        classResultShortName: classResult.shortName,
        personResults: classResult.personResults,
        certificateEnabled,
        cupScoreEnabled,
        cupScoreLists,
    }
}

function filterCupScoresByClassResult(
    cupScoreLists: CupScoreList[] | undefined,
    targetClassResultShortName: string,
): CupScoreList[] | undefined {
    if (!cupScoreLists) {
        return undefined
    }
    return cupScoreLists
        .map((cupScoreList) => {
            const filteredCupScores = cupScoreList.cupScores.filter((cupScore) => {
                return cupScore.classShortName === targetClassResultShortName
            })
            return {
                ...cupScoreList,
                cupScores: filteredCupScores,
            }
        })
        .filter(cupScoreList => cupScoreList.cupScores.length > 0) // Entferne Einträge ohne passende CupScores
}

function createClassResultTreeNodes(
    resultListId: number,
    classResults: ClassResult[] | undefined,
    certificateEnabled: boolean | undefined,
    cupScoreEnabled: boolean | undefined,
    cupScoreLists: CupScoreList[] | undefined,
): TreeNode[] {
    if (!classResults)
        return []

    return classResults.map(
        (classResult): TreeNode => ({
            key: `${classResult.shortName}-key`,
            label: getClassResultLabel(classResult),
            children: [
                {
                    key: `${classResult.shortName}-table`,
                    data: getPersonResults(
                        resultListId,
                        classResult,
                        certificateEnabled,
                        cupScoreEnabled,
                        filterCupScoresByClassResult(cupScoreLists, classResult.shortName),
                    ),
                    type: 'dataTable',
                    leaf: true,
                },
            ],
        }),
    )
}

const treeNodes = computed(() => {
    if (eventResultsQuery.isFetched && !cupPointsLoading.value) {
        return createResultListTreeNodes(
            eventResultsQuery.data.value?.resultLists,
            cupPointsData.value,
        )
    }

    return undefined
})

/**
 * Reaktive Map für ausstehende Nodes
 * key: Node-Key, value: Array von resolve-Funktionen
 */
const pendingNodes = ref<Record<string, (() => void)[]>>({})

/**
 * Scroll zur Person und Highlight
 */
async function scrollToPerson(personId: number) {
    console.log('[DeepLink] Starting scroll to person:', personId)

    await nextTick()
    await new Promise(r => setTimeout(r, 200)) // kurze Zeit, DOM vollständig

    const elementId = `person-result-${personId}`
    const element = document.getElementById(elementId)

    if (!element) {
        console.warn('[DeepLink] Element not found:', elementId)
        const allPersonElements = document.querySelectorAll('[id^="person-result-"]')
        console.log('[DeepLink] Available person elements:', Array.from(allPersonElements).map(el => el.id))
        return
    }

    element.scrollIntoView({ behavior: 'smooth', block: 'center' })
    element.classList.add('highlight-person-result')
    setTimeout(() => element.classList.remove('highlight-person-result'), 2000)
}

/**
 * Rekursiv Node anhand Key finden
 */
function findNodeByKey(nodes: any[] | undefined, key: string): any | null {
    if (!nodes || nodes.length === 0) {
        return null
    }
    for (const node of nodes) {
        if (node.key === key)
            return node
        if (node.children) {
            const found = findNodeByKey(node.children, key)
            if (found)
                return found
        }
    }
    return null
}

/**
 * Node-Expansion Callback für PrimeVue Tree
 * @param event Node-Event von PrimeVue Tree
 */
function onNodeExpand(event: any) {
    const key = event.node.key
    console.log('[onNodeExpand] Node expanded:', key)

    if (pendingNodes.value[key]) {
        pendingNodes.value[key].forEach(resolve => resolve())
        delete pendingNodes.value[key]
    }
}

/**
 * Wartet auf Node (event-basiert)
 */
function waitForNode(key: string): Promise<void> {
    return new Promise((resolve) => {
        if (treeNodes.value && findNodeByKey(treeNodes.value, key)) {
            console.log('[waitForNode] Node already exists:', key)
            resolve()
            return
        }

        if (!pendingNodes.value[key])
            pendingNodes.value[key] = []
        pendingNodes.value[key].push(resolve)
    })
}

/**
 * Prüft, ob Root Node existiert
 */
function hasRootNode(resultListId: number): boolean {
    return !!findNodeByKey(treeNodes.value, resultListId.toString())
}

/**
 * Sequenzielles Expandieren aller Tree-Ebenen
 */
async function expandTreeToClass(resultListId: number, classShortName: string) {
    const path = [
        resultListId.toString(),
        `${resultListId}-table`,
        `${classShortName}-key`,
        `${classShortName}-table`,
    ]

    for (const key of path) {
        console.log('[expandTreeToClass] Waiting for node:', key)
        await waitForNode(key)

        expandedKeys.value = { ...expandedKeys.value, [key]: true }
        await nextTick()
        console.log('[expandTreeToClass] Expanded:', key)
    }
}

/**
 * Deep-Link Handler
 */
async function handleDeepLink() {
    const { resultListId, classShortName, personId } = deepLinkParams.value
    if (!resultListId || !classShortName)
        return

    console.log('[DeepLink] Handling deep link:', { resultListId, classShortName, personId })

    await expandTreeToClass(resultListId, classShortName)

    if (personId) {
        await scrollToPerson(personId)
    }
}

/**
 * Watcher für Tree Nodes
 */
const stopDeepLinkWatch: (() => void) | undefined = watch(
    () => treeNodes.value,
    () => {
        const resultListId = deepLinkParams.value.resultListId
        if (!resultListId || !hasRootNode(resultListId))
            return

        console.log('[DeepLink] Root node ready, handling deep link')
        handleDeepLink()
        stopDeepLinkWatch?.()
    },
    { deep: true, immediate: true, flush: 'post' },
)

function findCourse(slotProps: any) {
    if (slotProps.courseId && courseQuery.data?.value && Array.isArray(courseQuery.data.value)) {
        return courseQuery.data.value.find(c => c.id === slotProps.courseId)
    }

    return null
}

function courseLengthColumn(slotProps: any): string {
    const course = findCourse(slotProps)
    if (course)
        return (course.length / 1000.0).toFixed(1)

    return ''
}

function courseClimbColumn(slotProps: any): string {
    const course = findCourse(slotProps)
    if (course && course.climb != null)
        return course.climb.toFixed(0)

    return ''
}

function courseControlsColumn(slotProps: any): string {
    const course = findCourse(slotProps)
    if (course && course.controls) {
        return course.controls.toFixed(0)
    }

    return ''
}

function calculate(result_list_id: number) {
    EventService.calculate(result_list_id, t)
    invalidateCupPointsQuery(result_list_id)
}

function navigateToList() {
    router.replace({ name: `event-list` })
}

function navigateToSplitTimeAnalysis(resultListId: number) {
    // Find the result list to generate label
    const resultLists = eventResultsQuery.data.value?.resultLists || []
    const sortedLists = [...resultLists].sort((a, b) => {
        const dateA = new Date(a.createTime).getTime()
        const dateB = new Date(b.createTime).getTime()
        return dateA - dateB
    })
    const resultListIndex = sortedLists.findIndex(list => list.id === resultListId)
    const resultListLabel = sortedLists.length > 1
        ? `${t('labels.result_list')} #${resultListIndex + 1}`
        : t('labels.result_list')

    router.push({
        name: 'split-time-analysis',
        params: { resultListId: resultListId.toString() },
        query: {
            eventName: eventQuery.data.value?.name || '',
            resultListLabel,
        },
    })
}

function navigateToSplitTimeTableAnalysis(resultListId: number) {
    // Find the result list to generate label
    const resultLists = eventResultsQuery.data.value?.resultLists || []
    const sortedLists = [...resultLists].sort((a, b) => {
        const dateA = new Date(a.createTime).getTime()
        const dateB = new Date(b.createTime).getTime()
        return dateA - dateB
    })
    const resultListIndex = sortedLists.findIndex(list => list.id === resultListId)
    const resultListLabel = sortedLists.length > 1
        ? `${t('labels.result_list')} #${resultListIndex + 1}`
        : t('labels.result_list')

    router.push({
        name: 'split-time-table-analysis',
        query: {
            resultListId: resultListId.toString(),
            eventName: eventQuery.data.value?.name || '',
            resultListLabel,
        },
    })
}

function navigateToMentalResilienceAnalysis(resultListId: number) {
    // Find the result list to generate label
    const resultLists = eventResultsQuery.data.value?.resultLists || []
    const sortedLists = [...resultLists].sort((a, b) => {
        const dateA = new Date(a.createTime).getTime()
        const dateB = new Date(b.createTime).getTime()
        return dateA - dateB
    })
    const resultListIndex = sortedLists.findIndex(list => list.id === resultListId)
    const resultListLabel = sortedLists.length > 1
        ? `${t('labels.result_list')} #${resultListIndex + 1}`
        : t('labels.result_list')

    router.push({
        name: 'mental-resilience-analysis',
        query: {
            scope: 'event',
            resultListId: resultListId.toString(),
            eventName: eventQuery.data.value?.name || '',
            resultListLabel,
        },
    })
}

function navigateToAnomalyDetectionAnalysis(resultListId: number) {
    // Find the result list to generate label
    const resultLists = eventResultsQuery.data.value?.resultLists || []
    const sortedLists = [...resultLists].sort((a, b) => {
        const dateA = new Date(a.createTime).getTime()
        const dateB = new Date(b.createTime).getTime()
        return dateA - dateB
    })
    const resultListIndex = sortedLists.findIndex(list => list.id === resultListId)
    const resultListLabel = sortedLists.length > 1
        ? `${t('labels.result_list')} #${resultListIndex + 1}`
        : t('labels.result_list')

    router.push({
        name: 'cheat-detection-analysis',
        query: {
            scope: 'event',
            resultListId: resultListId.toString(),
            eventName: eventQuery.data.value?.name || '',
            resultListLabel,
        },
    })
}

function navigateToHangingDetectionAnalysis(resultListId: number) {
    // Find the result list to generate label
    const resultLists = eventResultsQuery.data.value?.resultLists || []
    const sortedLists = [...resultLists].sort((a, b) => {
        const dateA = new Date(a.createTime).getTime()
        const dateB = new Date(b.createTime).getTime()
        return dateA - dateB
    })
    const resultListIndex = sortedLists.findIndex(list => list.id === resultListId)
    const resultListLabel = sortedLists.length > 1
        ? `${t('labels.result_list')} #${resultListIndex + 1}`
        : t('labels.result_list')

    router.push({
        name: 'hanging-detection-analysis',
        query: {
            scope: 'event',
            resultListId: resultListId.toString(),
            eventName: eventQuery.data.value?.name || '',
            resultListLabel,
        },
    })
}
</script>

<template>
    <Button
        v-tooltip="t('labels.back')"
        icon="pi pi-arrow-left"
        class="ml-2"
        :aria-label="t('labels.back')"
        severity="secondary"
        type="reset"
        outlined
        raised
        rounded
        @click="navigateToList"
    />
    <!-- Button v-if="authStore.isAdmin" :label="t('labels.calculate')" @click="calculate()" / -->
    <span v-if="eventResultsQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
    <div v-else-if="eventResultsQuery.data && eventId" class="card flex justify-content-start">
        <div class="flex flex-col grow w-full">
            <h1 class="mt-3 font-extrabold">
                {{ event?.name }} - {{ t('labels.results', 2) }}
            </h1>
            <Tree
                v-model:expanded-keys="expandedKeys"
                :value="treeNodes"
                class="flex flex-col w-full"
                @node-expand="onNodeExpand"
            >
                <template #default="slotProps">
                    <div class="flex flex-row justify-content-between w-full">
                        <div class="flex align-items-center font-bold">
                            {{ slotProps?.node?.label }}
                        </div>
                        <Button
                            v-if="authStore.isAdmin && slotProps?.node?.data?.cupScoreEnabled"
                            v-tooltip="t('labels.calculate')"
                            icon="pi pi-calculator"
                            class="ml-5"
                            :aria-label="t('labels.calculate')"
                            outlined
                            raised
                            rounded
                            @click="calculate(parseInt(slotProps?.node?.key!))"
                        />
                        <Button
                            v-if="slotProps?.node?.data?.raceNumber !== 0"
                            v-tooltip="t('labels.split_time_table')"
                            icon="pi pi-table"
                            class="ml-2"
                            :aria-label="t('labels.split_time_table')"
                            outlined
                            raised
                            rounded
                            @click="navigateToSplitTimeTableAnalysis(parseInt(slotProps?.node?.key!))"
                        />
                        <Button
                            v-if="slotProps?.node?.data?.raceNumber !== 0"
                            v-tooltip="t('labels.split_time_analysis_ranking')"
                            icon="pi pi-chart-bar"
                            class="ml-2"
                            :aria-label="t('labels.split_time_analysis_ranking')"
                            outlined
                            raised
                            rounded
                            @click="navigateToSplitTimeAnalysis(parseInt(slotProps?.node?.key!))"
                        />
                        <Button
                            v-if="slotProps?.node?.data?.raceNumber !== 0"
                            v-tooltip="t('labels.mental_resilience_analysis')"
                            icon="pi pi-chart-line"
                            class="ml-2"
                            :aria-label="t('labels.mental_resilience_analysis')"
                            outlined
                            raised
                            rounded
                            @click="navigateToMentalResilienceAnalysis(parseInt(slotProps?.node?.key!))"
                        />
                        <Button
                            v-if="authStore.isAdmin && slotProps?.node?.data?.raceNumber !== 0"
                            v-tooltip="t('labels.anomaly_detection_analysis')"
                            icon="pi pi-exclamation-triangle"
                            class="ml-2"
                            :aria-label="t('labels.anomaly_detection_analysis')"
                            outlined
                            raised
                            rounded
                            @click="navigateToAnomalyDetectionAnalysis(parseInt(slotProps?.node?.key!))"
                        />
                        <Button
                            v-if="authStore.isAdmin && slotProps?.node?.data?.raceNumber !== 0"
                            v-tooltip="t('labels.hanging_detection_analysis')"
                            icon="pi pi-users"
                            class="ml-2"
                            :aria-label="t('labels.hanging_detection_analysis')"
                            outlined
                            raised
                            rounded
                            @click="navigateToHangingDetectionAnalysis(parseInt(slotProps?.node?.key!))"
                        />
                    </div>
                </template>
                <!-- suppress VueUnrecognizedSlot -->
                <template #tree="slotProps">
                    <Tree :value="slotProps?.node?.data">
                        <template #default="mySlotProps">
                            <b>{{ mySlotProps?.node?.label }}</b>
                        </template>
                        <!-- suppress VueUnrecognizedSlot -->
                        <template #dataTable="mySlotProps">
                            <EventResultTable
                                v-if="eventId"
                                :data="mySlotProps?.node?.data"
                                :event-id="eventId"
                            />
                        </template>
                    </Tree>
                </template>
                <!-- suppress VueUnrecognizedSlot -->
                <template #dataTable="slotProps">
                    <EventResultTable
                        v-if="eventId"
                        :data="slotProps?.node?.data"
                        :event-id="eventId"
                    />
                </template>
            </Tree>
            <div
                v-if="authStore.isAdmin && eventCertificateStatsQuery.data"
                class="mt-2 font-italic"
            >
                <Panel
                    v-if="eventCertificateStatsQuery.data.value?.stats.length ?? 0 > 0"
                    :header="
                        t('labels.certificate_stats', {
                            count: eventCertificateStatsQuery.data.value?.stats.length ?? 0,
                        })
                    "
                    header-class="mt-2 text-lg font-bold"
                    toggleable
                    collapsed
                >
                    <EventCertificateStatsTable :data="eventCertificateStatsQuery.data.value" />
                </Panel>
            </div>
        </div>
    </div>
</template>

<style scoped>
h1 {
    margin-bottom: 1rem;
}

:deep(.highlight-person-result) {
    background-color: rgba(var(--primary-500), 0.2) !important;
    transition: background-color 0.3s ease-out;
}
</style>
