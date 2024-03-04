<script lang="ts" setup>
import { useQuery } from '@tanstack/vue-query'
import { eventService, EventService } from '@/features/event/services/event.service'
import { type Locale, useI18n } from 'vue-i18n'
import type { ClassResult } from '@/features/event/model/class_result'
import type { TreeNode } from 'primevue/treenode'
import { computed, type Ref } from 'vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import type { ResultList } from '@/features/event/model/result_list'
import Tree from 'primevue/tree'
import { courseService } from '@/features/course/services/course.service'
import { raceService } from '@/features/race/services/race.service'
import Button from 'primevue/button'
import { useRouter } from 'vue-router'
import moment from 'moment/min/moment-with-locales'
import EventResultTable from '@/features/event/widgets/EventResultTable.vue'
import type { ResultListIdPersonResults } from '@/features/event/model/result_list_id_person_results'

const props = defineProps<{ id: string }>()

const { t, locale } = useI18n()

const authStore = useAuthStore()
const router = useRouter()

const eventResultsQuery = useQuery({
  queryKey: ['eventResults', props.id],
  queryFn: () => EventService.getResultsById(props.id, t)
})

const resultList0 = computed(() => {
  return eventResultsQuery.data.value?.resultLists[0]
})

const eventQuery = useQuery({
  queryKey: ['events'],
  queryFn: () => eventService.getAll(t)
})

const event = computed(() => {
  return eventQuery.data.value?.content.find((e) => e.id === resultList0.value?.eventId)
})

const courseQuery = useQuery({
  queryKey: ['courses'],
  queryFn: () => courseService.getAll(t)
})

const raceQuery = useQuery({
  queryKey: ['races'],
  queryFn: () => raceService.getAll(t)
})

const formatCreateTime = (date: Date | string, locale: Ref<Locale>) => {
  return computed(() => {
    moment.locale(locale.value)
    return moment(date).format('L')
  })
}

function getResultListLabel(resultList: ResultList) {
  const name = raceQuery.data.value?.content.find((r) => r.id === resultList.raceId)?.name
  return (
    (name ? name + ', ' : '') +
    t('labels.created') +
    ' ' +
    formatCreateTime(resultList.createTime, locale).value +
    ', ' +
    t(`result_list_state.${resultList.status.toUpperCase()}`)
  )
}

const createResultListTreeNodes = (aList: ResultList[] | undefined): TreeNode[] => {
  if (!aList) {
    return []
  }
  const treeNodes: TreeNode[] = []
  for (let i = 0; i < aList.length; i++) {
    const resultList = aList[i]
    treeNodes.push({
      key: resultList.id.toString(),
      label: getResultListLabel(resultList),
      children: [
        {
          key: `${resultList.id.toString()}-table`,
          data: createClassResultTreeNodes(resultList.id, resultList.classResults),
          type: 'tree',
          leaf: false
        }
      ]
    })
  }
  return treeNodes
}

function getClassResultLabel(a: ClassResult) {
  return (
    a.name +
    ' (' +
    a.personResults.length +
    ')' +
    ' - ' +
    courseLengthColumn(a) +
    ' ' +
    t('labels.length_abbreviation') +
    ' - ' +
    courseClimbColumn(a) +
    ' ' +
    t('labels.climb_abbreviation') +
    ' - ' +
    courseControlsColumn(a) +
    ' ' +
    t('labels.control_abbreviation')
  )
}

const getPersonResults = (resultListId: number, a: ClassResult): ResultListIdPersonResults => {
  return { resultListId: resultListId, personResults: a.personResults }
}

const createClassResultTreeNodes = (
  resultListId: number,
  aList: ClassResult[] | undefined
): TreeNode[] => {
  if (!aList) {
    return []
  }
  const nodes = aList.map(
    (a): TreeNode => ({
      key: a.shortName.toString(),
      label: getClassResultLabel(a),
      children: [
        {
          key: `${a.shortName}-table`,
          data: getPersonResults(resultListId, a),
          type: 'dataTable',
          leaf: true
        }
      ]
    })
  )
  return nodes
}

const treeNodes = computed(() => {
  if (eventResultsQuery.isFetched) {
    return createResultListTreeNodes(eventResultsQuery.data.value?.resultLists)
  }
  return undefined
})

const findCourse = (slotProps: any) => {
  if (slotProps.courseId && courseQuery.data.value) {
    return courseQuery.data.value.content.find((c) => c.id === slotProps.courseId)
  }
  return null
}

const courseLengthColumn = (slotProps: any): string => {
  const course = findCourse(slotProps)
  if (course) {
    return (course.length / 1000.0).toFixed(1)
  }
  return ''
}

const courseClimbColumn = (slotProps: any): string => {
  const course = findCourse(slotProps)
  if (course && course.climb != null) {
    return course.climb.toFixed(0)
  }
  return ''
}

const courseControlsColumn = (slotProps: any): string => {
  const course = findCourse(slotProps)
  if (course) {
    return course.controls.toFixed(0)
  }
  return ''
}

const calculate = (result_list_id: number) => {
  console.log(result_list_id)
  EventService.calculate(result_list_id, t)
}

const navigateToList = () => {
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
  <!--Button v-if="authStore.isAdmin" :label="t('labels.calculate')" @click="calculate()" /-->
  <span v-if="eventResultsQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
  <span v-else-if="eventResultsQuery.status.value === 'error'">
    {{ t('messages.error', { message: eventResultsQuery.error.toLocaleString() }) }}
  </span>
  <div v-else-if="eventResultsQuery.data" class="card flex justify-content-start">
    <div class="flex flex-col flex-grow w-full">
      <h1 class="mt-3 font-extrabold">{{ event?.name }} - {{ t('labels.results', 2) }}</h1>
      <Tree :value="treeNodes" class="flex flex-col w-full">
        <template #default="slotProps">
          <div class="flex flex-row justify-content-between w-full">
            <div class="flex align-items-center font-bold">
              {{ slotProps?.node?.label }}
            </div>
            <Button
              class="ml-5"
              v-if="authStore.isAdmin"
              :label="t('labels.calculate')"
              outlined
              @click="calculate(slotProps?.node?.key)"
            />
          </div>
        </template>
        <!--suppress VueUnrecognizedSlot -->
        <template #tree="slotProps">
          <Tree :value="slotProps?.node?.data">
            <template #default="slotProps">
              <b>{{ slotProps?.node?.label }}</b>
            </template>
            <!--suppress VueUnrecognizedSlot -->
            <template #dataTable="slotProps">
              <EventResultTable :data="slotProps?.node?.data" />
            </template>
          </Tree>
        </template>
        <!--suppress VueUnrecognizedSlot -->
        <template #dataTable="slotProps">
          <EventResultTable :data="slotProps?.node?.data" />
        </template>
      </Tree>
    </div>
  </div>
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}
</style>
