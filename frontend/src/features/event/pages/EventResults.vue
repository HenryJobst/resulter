<script lang="ts" setup>
import type { ClassResult } from '@/features/event/model/class_result'
import type { CupScoreList } from '@/features/event/model/cup_score_list'
import type { ResultList } from '@/features/event/model/result_list'
import type { ResultListIdPersonResults } from '@/features/event/model/result_list_id_person_results'
import type { TreeNode } from 'primevue/treenode'
import type { Ref } from 'vue'
import type { Locale } from 'vue-i18n'
import { courseService } from '@/features/course/services/course.service'
import { EventService, eventService } from '@/features/event/services/event.service'
import EventCertificateStatsTable from '@/features/event/widgets/EventCertificateStatsTable.vue'
import EventResultTable from '@/features/event/widgets/EventResultTable.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { raceService } from '@/features/race/services/race.service'
import { useQueries, useQuery, useQueryClient } from '@tanstack/vue-query'
import moment from 'moment/min/moment-with-locales'
import Button from 'primevue/button'
import Panel from 'primevue/panel'
import Tree from 'primevue/tree'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const props = defineProps<{ id: string }>()

const { t, locale } = useI18n()

const authStore = useAuthStore()
const router = useRouter()

const eventResultsQuery = useQuery({
    queryKey: ['eventResults', props.id],
    queryFn: () => EventService.getResultsById(props.id, t),
})

const eventId = computed(() => {
    return eventResultsQuery.data.value?.resultLists[0].eventId
})

const enabled = computed(() => !!eventResultsQuery.data.value?.resultLists[0].eventId)

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
        const certificateEnabled: boolean
            = (event.value?.certificate ?? false) !== false && (resultLists.length === 1 || i === 0)
        const cupScoreEnabled: boolean = resultLists.length === 1 || i > 0
        const resultListCupScoreLists = cupScoreLists ? cupScoreLists[i] : undefined
        const resultListCompleteCupScoreLists = resultListCupScoreLists
            ? resultListCupScoreLists.filter(x => x.status === 'COMPLETE')
            : undefined
        treeNodes.push({
            key: resultList.id.toString(),
            label: getResultListLabel(resultList),
            data: {
                cupScoreEnabled,
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
            key: classResult.shortName.toString(),
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
    if (course)
        return course.controls.toFixed(0)

    return ''
}

function calculate(result_list_id: number) {
    console.log(result_list_id)
    EventService.calculate(result_list_id, t)
    invalidateCupPointsQuery(result_list_id)
}

function navigateToList() {
    router.replace({ name: `event-list` })
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
