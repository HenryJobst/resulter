<script lang="ts" setup>
import { useQueries, useQuery } from '@tanstack/vue-query'
import { type Locale, useI18n } from 'vue-i18n'
import type { TreeNode } from 'primevue/treenode'
import { type Ref, computed } from 'vue'
import Tree from 'primevue/tree'
import Button from 'primevue/button'
import Panel from 'primevue/panel'
import { useRouter } from 'vue-router'
import moment from 'moment/min/moment-with-locales'
import { prettyPrint } from '@base2/pretty-print-object'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import type { ResultList } from '@/features/event/model/result_list'
import { courseService } from '@/features/course/services/course.service'
import { raceService } from '@/features/race/services/race.service'
import type { ClassResult } from '@/features/event/model/class_result'
import { EventService, eventService } from '@/features/event/services/event.service'
import EventResultTable from '@/features/event/widgets/EventResultTable.vue'
import type { ResultListIdPersonResults } from '@/features/event/model/result_list_id_person_results'
import EventCertificateStatsTable from '@/features/event/widgets/EventCertificateStatsTable.vue'
import type { CupScoreList } from '@/features/event/model/cup_score_list'

const props = defineProps<{ id: string }>()

const { t, locale } = useI18n()

const authStore = useAuthStore()
const router = useRouter()

const eventResultsQuery = useQuery({
    queryKey: ['eventResults', props.id],
    queryFn: () => EventService.getResultsById(props.id, t),
})

const resultList0 = computed(() => {
    return eventResultsQuery.data.value?.resultLists[0]
})

const eventQuery = useQuery({
    queryKey: ['events'],
    queryFn: () => eventService.getAll(t),
})

const event = computed(() => {
    return eventQuery.data.value?.content.find(e => e.id === resultList0.value?.eventId)
})

const eventId = computed(() => {
    return event.value?.id
})

const eventCertificateStatsQuery = useQuery({
    queryKey: ['eventCertificateStats', eventId, authStore.isAdmin],
    queryFn: () => EventService.getCertificateStats(eventId.value, t),
    enabled: authStore.isAdmin,
    retry: false,
})

const courseQuery = useQuery({
    queryKey: ['courses'],
    queryFn: () => courseService.getAll(t),
})

const raceQuery = useQuery({
    queryKey: ['races'],
    queryFn: () => raceService.getAll(t),
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

function formatCreateTime(date: Date | string, locale: Ref<Locale>) {
    return computed(() => {
        moment.locale(locale.value)
        return moment(date).format('L')
    })
}

function getResultListLabel(resultList: ResultList) {
    let name = raceQuery.data.value?.content.find(r => r.id === resultList.raceId)?.name
    if (!name) {
        const raceNumber = resultList.classResults
            .flatMap(c => c.personResults)
            .flatMap(pr => pr.raceNumber)
            .reduce(a => a)
            .toString()
        if (raceNumber !== '0') {
            name = t('labels.race_number', {
                raceNumber,
            })
        }
        else {
            name = t('labels.overall')
        }
    }
    return `${(name ? `${name}, ` : '') + t('labels.created')} ${
        formatCreateTime(resultList.createTime, locale).value
    }, ${t(`result_list_state.${resultList.status.toUpperCase()}`)}`
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
        const certificateEnabled: boolean
            = (eventQuery.data.value?.content.find(e => e.id === resultList.eventId)?.certificate
            ?? false) !== false
            && (resultLists.length === 1 || i === 0)
        const resultListCupScoreLists = cupScoreLists ? cupScoreLists[0] : undefined
        const resultListCompleteCupScoreLists = resultListCupScoreLists
            ? resultListCupScoreLists.filter(x => x.status === 'COMPLETE')
            : undefined
        treeNodes.push({
            key: resultList.id.toString(),
            label: getResultListLabel(resultList),
            children: [
                {
                    key: `${resultList.id.toString()}-table`,
                    data: createClassResultTreeNodes(
                        resultList.id,
                        resultList.classResults,
                        certificateEnabled,
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
        courseData = ` - ${courseLengthColumn(a)} ${t('labels.length_abbreviation')} - ${courseClimbColumn(a)} ${t(
            'labels.climb_abbreviation',
        )} - ${courseControlsColumn(a)} ${t('labels.control_abbreviation')}`
    }
    return `${a.name} (${a.personResults.length})${courseData}`
}

function getPersonResults(
    resultListId: number,
    classResult: ClassResult,
    certificateEnabled: boolean | undefined,
    cupScoreLists: CupScoreList[] | undefined,
): ResultListIdPersonResults {
    return {
        resultListId,
        classResultShortName: classResult.shortName,
        personResults: classResult.personResults,
        certificateEnabled,
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
    cupScoreLists: CupScoreList[] | undefined,
): TreeNode[] {
    if (!classResults)
        return []

    return classResults.map(
        (classResult): TreeNode => ({
            key: classResult.shortName.toString(),
            label: getClassResultLabel(classResult),
            children: [
                {
                    key: `${classResult.shortName}-table`,
                    data: getPersonResults(
                        resultListId,
                        classResult,
                        certificateEnabled,
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

function findCourse(slotProps: any) {
    if (slotProps.courseId && courseQuery.data.value)
        return courseQuery.data.value.content.find(c => c.id === slotProps.courseId)

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
    if (course)
        return course.controls.toFixed(0)

    return ''
}

function calculate(result_list_id: number) {
    console.log(result_list_id)
    const result = EventService.calculate(result_list_id, t)
    console.log(prettyPrint(result))
}

function navigateToList() {
    router.replace({ name: `event-list` })
}
</script>

<template>
    <Button
        class="ml-2"
        severity="secondary"
        type="reset"
        :label="t('labels.back')"
        outlined
        @click="navigateToList"
    />
    <!-- Button v-if="authStore.isAdmin" :label="t('labels.calculate')" @click="calculate()" / -->
    <span v-if="eventResultsQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
    <span v-else-if="eventResultsQuery.status.value === 'error'">
        {{ t('messages.error', { message: eventResultsQuery.error.toLocaleString() }) }}
    </span>
    <div v-else-if="eventResultsQuery.data" class="card flex justify-content-start">
        <div class="flex flex-col flex-grow w-full">
            <h1 class="mt-3 font-extrabold">
                {{ event?.name }} - {{ t('labels.results', 2) }}
            </h1>
            <Tree :value="treeNodes" class="flex flex-col w-full">
                <template #default="slotProps">
                    <div class="flex flex-row justify-content-between w-full">
                        <div class="flex align-items-center font-bold">
                            {{ slotProps?.node?.label }}
                        </div>
                        <Button
                            v-if="authStore.isAdmin"
                            class="ml-5"
                            :label="t('labels.calculate')"
                            outlined
                            @click="calculate(parseInt(slotProps?.node?.key!))"
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
</style>
